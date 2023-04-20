package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.DeviceSecretImporter;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.TrustStore;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.ImporterExtension;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.NamedEncryptedDataType;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.WrapKey;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.exception.ImportFailedException;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Created by bvn on 7/19/17.
 */
public class SecureDeviceShipmentImporter extends SecureDeviceImporterAbstract implements FileImporter {

    public SecureDeviceShipmentImporter(Thesaurus thesaurus, TrustStore trustStore,
                                        DeviceConfigurationService deviceConfigurationService, DeviceService deviceService,
                                        SecurityManagementService securityManagementService,
                                        Optional<ImporterExtension> importerExtension) {
        super(thesaurus, trustStore, deviceConfigurationService, deviceService, securityManagementService, importerExtension);
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        processFile(fileImportOccurrence);
    }

    @Override
    protected void importDeviceKey(Device device, NamedEncryptedDataType deviceKey, TransportKeys transportKeys, Logger logger) {
        String securityAccessorName = deviceKey.getName();
        SecurityAccessorType securityAccessorType = getSecurityAccessorType(device, securityAccessorName).orElseThrow(() -> new ImportFailedException(MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE_ON_DEVICE_TYPE, device.getName(), securityAccessorName));
        final WrapKey wrapKey = transportKeys.get(deviceKey.getWrapKeyLabel());
        if (wrapKey == null) {
            throw new ImportFailedException(MessageSeeds.WRAP_KEY_NOT_FOUND, securityAccessorName, device.getName(), deviceKey.getWrapKeyLabel());
        }
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
        DeviceSecretImporter deviceSecretImporter = securityManagementService.getDeviceSecretImporter(securityAccessorType);
        Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(securityAccessorType);
        if (securityAccessorOptional.flatMap(SecurityAccessor::getActualValue).isPresent()) {
            log(logger, MessageSeeds.ACTUAL_VALUE_ALREADY_EXISTS, securityAccessorName, device.getName());
        } else {
            SecurityAccessor securityAccessor = securityAccessorOptional.orElseGet(() -> device.newSecurityAccessor(securityAccessorType));
            SecurityValueWrapper newWrapperValue = deviceSecretImporter.importSecret(encryptedDeviceKey, initializationVector, encryptedSymmetricKey, symmetricAlgorithm, asymmetricAlgorithm);
            securityAccessor.setActualValue(newWrapperValue);
            securityAccessor.save();
        }
    }

    @Override
    protected boolean shouldValidateCert() {
        return true;
    }
}
