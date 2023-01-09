package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.DeviceSecretImporter;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.Body;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.NamedEncryptedDataType;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.Shipment;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.WrapKey;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Created by bvn on 7/19/17.
 */
public class SecureDeviceKeyImporter  extends SecureDeviceImporterAbstract implements FileImporter {


    public SecureDeviceKeyImporter(Thesaurus thesaurus, TrustStore trustStore, DeviceConfigurationService deviceConfigurationService, DeviceService deviceService, SecurityManagementService securityManagementService) {
       super(thesaurus, trustStore, deviceConfigurationService, deviceService, securityManagementService, Optional.empty());
    }


    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        processFile(fileImportOccurrence);
    }

    @Override
    protected boolean shouldValidateCert() {
        return true;
    }


    private DeviceCreator getDeviceCreator(Shipment shipment) {
        DeviceCreator deviceCreator = null;
        if (Checks.is(shipment.getHeader().getBatchID()).emptyOrOnlyWhiteSpace()) {
            deviceCreator = (deviceConfiguration, serialNumber, name) -> deviceService.newDevice(
                    deviceConfiguration,
                    serialNumber,
                    name,
                    shipment.getHeader().getBatchID(),
                    shipment.getHeader().getDeliveryDate().toGregorianCalendar().toInstant());
        } else {
            deviceCreator = (deviceConfiguration, serialNumber, name) ->
                    deviceService.newDevice(deviceConfiguration,
                    serialNumber,
                    name,
                    shipment.getHeader().getDeliveryDate().toGregorianCalendar().toInstant());
        }
        return deviceCreator;
    }

    @Override
     protected int importDevices(Shipment shipment, Logger logger) {
        int deviceCount = 0;
        DeviceType deviceType = findDeviceType(shipment);
        TransportKeys wrapKeyMap = new TransportKeys(shipment);

        findDefaultDeviceConfig(deviceType);

        for (Body.Device xmlDevice : shipment.getBody().getDevice()) {
            String deviceName = Checks.is(xmlDevice.getUniqueIdentifier())
                    .emptyOrOnlyWhiteSpace() ? xmlDevice.getSerialNumber() : xmlDevice.getUniqueIdentifier();
            log(logger, MessageSeeds.IMPORTING_DEVICE, deviceName);
            try {
                Optional<Device> existingDevice = deviceService.findDeviceByName(deviceName);
                if (!existingDevice.isPresent()) {
                    log(logger, MessageSeeds.DEVICE_WITH_NAME_DOES_NOT_EXIST, deviceName);
                    continue;
                }
                Device device = existingDevice.get();
                for (NamedEncryptedDataType deviceKey : xmlDevice.getKey()) {
                    importDeviceKey(device, deviceKey, wrapKeyMap, logger);
                }

                postProcessDevice(device, xmlDevice, shipment, logger);
                log(logger, MessageSeeds.IMPORTED_DEVICE, deviceName);
                deviceCount++;
            } catch (Exception e) {
                log(logger, MessageSeeds.IMPORT_FAILED_FOR_DEVICE, deviceName, e);
                throw e;
            }
        }
        return deviceCount;
    }

    @Override
    protected void importDeviceKey(Device device, NamedEncryptedDataType deviceKey, TransportKeys transportKeys, Logger logger) {
        String securityAccessorName = deviceKey.getName();
        Optional<SecurityAccessorType> securityAccessorTypeOptional = device.getDeviceType()
                .getSecurityAccessorTypes()
                .stream()
                .filter(kat -> kat.getName().equals(securityAccessorName))
                .findAny();
        if (!securityAccessorTypeOptional.isPresent()) {
            log(logger, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE_ON_DEVICE_TYPE, device.getName(), securityAccessorName);
        } else {
            final SecurityAccessorType securityAccessorType = securityAccessorTypeOptional.get();
            final WrapKey wrapKey = transportKeys.get(deviceKey.getWrapKeyLabel());
            if (wrapKey==null) {
                throw new ImportFailedException(MessageSeeds.WRAP_KEY_NOT_FOUND, securityAccessorName, device.getName(), deviceKey.getWrapKeyLabel());
            }
            DeviceSecretImporter deviceSecretImporter = securityManagementService.getDeviceSecretImporter(securityAccessorType);
            PublicKey publicKey = getPublicKeyFromWrapKey(wrapKey);
            deviceSecretImporter.verifyPublicKey(publicKey);

            byte[] encryptedSymmetricKey = wrapKey.getSymmetricKey().getCipherData().getCipherValue();
            byte[] encryptedDeviceKey = deviceKey.getCipherData().getCipherValue();
            byte[] initializationVector = new byte[16];
            if (encryptedDeviceKey.length <= 16) {
                throw new ImportFailedException(MessageSeeds.INITIALIZATION_VECTOR_ERROR);
            }
            byte[] cipher = new byte[encryptedDeviceKey.length - 16];
            String symmetricAlgorithm = this.getSymmetricAlgorithm(deviceKey);
            String asymmetricAlgorithm = this.getAsymmetricAlgorithm(wrapKey);
            System.arraycopy(encryptedDeviceKey, 0, initializationVector, 0, 16);
            System.arraycopy(encryptedDeviceKey, 16, cipher, 0, encryptedDeviceKey.length - 16);

            Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(securityAccessorType);
            boolean hasActiveValue = securityAccessorOptional
                    .flatMap(SecurityAccessor::getActualValue)
                    .isPresent();
            boolean hasPassiveValue = securityAccessorOptional
                    .flatMap(SecurityAccessor::getTempValue)
                    .isPresent();
            if (hasActiveValue && hasPassiveValue) {
                log(logger, MessageSeeds.BOTH_VALUES_ALREADY_EXIST, securityAccessorName, device.getName());
            } else {
                SecurityAccessor securityAccessor = securityAccessorOptional.orElseGet(() -> device.newSecurityAccessor(securityAccessorType));
                SecurityValueWrapper newWrapperValue = deviceSecretImporter.importSecret(cipher, initializationVector, encryptedSymmetricKey, symmetricAlgorithm, asymmetricAlgorithm);
                if (hasActiveValue) {
                    securityAccessor.setTempValue(newWrapperValue);
                } else {
                    securityAccessor.setActualValue(newWrapperValue);
                }
                securityAccessor.save();
            }
        }
    }

    private PublicKey getPublicKeyFromWrapKey(WrapKey wrapKey) {
        try {
            BigInteger exponent = new BigInteger(wrapKey.getPublicKey().getExponent());
            BigInteger modulus = new BigInteger(wrapKey.getPublicKey().getModulus());
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(spec);
        } catch (NoSuchAlgorithmException e) {
            throw new ImportFailedException(MessageSeeds.NO_SUCH_ALGORITHM, e);
        } catch (InvalidKeySpecException e) {
            throw new ImportFailedException(MessageSeeds.ILLEGAL_KEY, e);
        }
    }


    /**
     * Hook to allow post processing on created devices by more specialized importers. XMl attributes can be obtained by calling xmlDevice.getAttribute().
     * This method is called from within a transaction. Make sure to call Device.save() to apply changes.
     *
     *
     * @param device The device that has just been created by the importer.
     * @param xmlDevice The XML Node from the shipment file that was used to create the device.
     * @param shipment The complete shipment file, in case any information is required from a larger scope.
     * @param logger logger on which information can be logged while post processing a device, if required.
     */
    protected void postProcessDevice(Device device, Body.Device xmlDevice, Shipment shipment, Logger logger) {
        // default importer has nothing to do here
    }







    static class ImportFailedException extends RuntimeException {
        private final MessageSeeds messageSeeds;
        private final Object[] objects;

        public ImportFailedException(MessageSeeds messageSeeds, Object... objects) {
            this.messageSeeds = messageSeeds;
            this.objects = objects;
        }

        public MessageSeeds getMessageSeed() {
            return messageSeeds;
        }

        public Object[] getMessageParameters() {
            return objects;
        }
    }

    private interface DeviceCreator {
        Device createDevice(DeviceConfiguration deviceConfiguration, String serialNumber, String name);
    }

}
