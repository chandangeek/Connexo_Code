/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.KeyImportFailedException;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SymmetricAlgorithm;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.streams.ReusableInputStream;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.ImporterExtension;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.Body;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.NamedAttribute;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.NamedEncryptedDataType;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.Shipment;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.WrapKey;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.exception.ImportFailedException;

import org.xml.sax.SAXException;

import javax.validation.ConstraintViolationException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SecureDeviceImporterAbstract {


    private static final String DEFAULT_SYMMETRIC_ALGORITHM = "AES/CBC/PKCS5PADDING";
    private static final String DEFAULT_ASYMMETRIC_ALGORITHM = "RSA/ECB/PKCS1Padding";

    private final Thesaurus thesaurus;
    private final TrustStore trustStore;
    private final DeviceConfigurationService deviceConfigurationService;
    protected final DeviceService deviceService;
    protected final SecurityManagementService securityManagementService;

    private final Optional<ImporterExtension> importerExtension;

    public SecureDeviceImporterAbstract(Thesaurus thesaurus, TrustStore trustStore,
                                        DeviceConfigurationService deviceConfigurationService, DeviceService deviceService,
                                        SecurityManagementService securityManagementService,
                                        Optional<ImporterExtension> importerExtension) {
        this.thesaurus = thesaurus;
        this.trustStore = trustStore;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceService = deviceService;
        this.securityManagementService = securityManagementService;
        this.importerExtension = importerExtension;
    }

    protected abstract void importDeviceKey(Device device, NamedEncryptedDataType deviceKey, TransportKeys transportKeys, Logger logger) throws  ImportFailedException;

    protected abstract boolean shouldValidateCert();

    protected void processFile(FileImportOccurrence fileImportOccurrence) {
        Logger logger = fileImportOccurrence.getLogger();
        try {
            doProcess(fileImportOccurrence, logger);
        } catch (JAXBException e) {
            Throwable toLog = (e.getLinkedException() != null) ? e.getLinkedException() : e;
            String message = toLog.getLocalizedMessage();
            if ("Content is not allowed in prolog.".equals(message)) {
                log(logger, MessageSeeds.VALIDATION_OF_FILE_FAILED);
                throw new XmlValidationFailedException(thesaurus);
            } else {
                log(logger, MessageSeeds.VALIDATION_OF_FILE_FAILED_WITH_DETAIL, message);
                throw new XmlValidationFailedException(thesaurus, message);
            }
        } catch (ConstraintViolationException | IOException e) {
            log(logger, MessageSeeds.SECURE_DEVICE_IMPORT_FAILED, e.getMessage());
            throw new RuntimeException(thesaurus.getFormat(MessageSeeds.SECURE_DEVICE_IMPORT_FAILED).format(e.getMessage()));
        } catch (CertificateException e) {
            log(logger, MessageSeeds.FAILED_TO_CREATE_CERTIFICATE_FACTORY);
            throw new RuntimeException(thesaurus.getFormat(MessageSeeds.FAILED_TO_CREATE_CERTIFICATE_FACTORY).format(e.getMessage()));
        } catch (CertPathValidatorException e) {
            log(logger, MessageSeeds.SHIPMENT_CERTIFICATE_UNTRUSTED, e.getMessage());
            throw new RuntimeException(thesaurus.getFormat(MessageSeeds.SHIPMENT_CERTIFICATE_UNTRUSTED).format(e.getMessage()));
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
            log(logger, MessageSeeds.FAILED_TO_VERIFY_CERTIFICATE);
            throw new RuntimeException(thesaurus.getFormat(MessageSeeds.FAILED_TO_VERIFY_CERTIFICATE).format(e.getMessage()));
        } catch (ImportFailedException e) {
            log(logger, e.getMessageSeed(), e.getMessageParameters());
            throw new RuntimeException(thesaurus.getFormat(e.getMessageSeed()).format(e.getMessageParameters()));
        }
    }

    private void doProcess(FileImportOccurrence fileImportOccurrence, Logger logger) throws
            JAXBException,
            CertificateException,
            InvalidAlgorithmParameterException,
            NoSuchAlgorithmException,
            CertPathValidatorException, IOException, ImportFailedException {
        ReusableInputStream inputStreamProvider = ReusableInputStream.from(fileImportOccurrence.getContents());
        Shipment shipment = getShipmentFileFomQueueMessage(inputStreamProvider.stream());
        if (shouldValidateCert()) {
            new ShipmentXmlUtils(logger, thesaurus).validateFileCertificate(inputStreamProvider, shipment, trustStore);
        }
        int importDevices = importDevices(shipment, logger);
        fileImportOccurrence.markSuccess(thesaurus.getFormat(MessageSeeds.IMPORT_COMPLETED).format(importDevices));
    }


    private SecureDeviceImporterAbstract.DeviceCreator getDeviceCreator(Shipment shipment) {
        SecureDeviceImporterAbstract.DeviceCreator deviceCreator = null;
        if (Checks.is(shipment.getHeader().getBatchID()).emptyOrOnlyWhiteSpace()) {
            deviceCreator = (deviceConfiguration, serialNumber, name) -> deviceService.newDevice(
                    deviceConfiguration,
                    serialNumber,
                    name,
                    shipment.getHeader().getBatchID(),
                    shipment.getHeader().getDeliveryDate().toGregorianCalendar().toInstant());
        } else {
            deviceCreator = (deviceConfiguration, serialNumber, name) -> deviceService.newDevice(deviceConfiguration,
                    serialNumber,
                    name,
                    shipment.getHeader().getDeliveryDate().toGregorianCalendar().toInstant());
        }
        return deviceCreator;
    }

    protected int importDevices(Shipment shipment, Logger logger)  {
        SecureDeviceImporterAbstract.DeviceCreator deviceCreator = getDeviceCreator(shipment);
        int deviceCount = 0;
        DeviceType deviceType = findDeviceType(shipment);
        TransportKeys transportKeys = new TransportKeys(shipment);

        DeviceConfiguration deviceConfiguration = findDefaultDeviceConfig(deviceType);

        for (Body.Device xmlDevice : shipment.getBody().getDevice()) {
            String deviceName = Checks.is(xmlDevice.getUniqueIdentifier())
                    .emptyOrOnlyWhiteSpace() ? xmlDevice.getSerialNumber() : xmlDevice.getUniqueIdentifier();
            log(logger, MessageSeeds.IMPORTING_DEVICE, deviceName);
            try {
                if (!Checks.is(xmlDevice.getSerialNumber()).emptyOrOnlyWhiteSpace()){
                    if (deviceService.findDevicesBySerialNumber(xmlDevice.getSerialNumber()).size() > 0) {
                        log(logger, MessageSeeds.DEVICE_WITH_SERIAL_NUMBER_ALREADY_EXISTS, xmlDevice.getSerialNumber());
                        continue;
                    }
                }
                if (deviceService.findDeviceByName(deviceName).isPresent()) {
                    log(logger, MessageSeeds.DEVICE_WITH_NAME_ALREADY_EXISTS, deviceName);
                    continue;
                }
                Device device = deviceCreator.createDevice(deviceConfiguration, xmlDevice.getSerialNumber(), deviceName);
                device.setManufacturer(shipment.getHeader().getManufacturer());
                storeCertificationDate(device, shipment);
                new ShipmentXmlUtils(logger, thesaurus).storeMacAddress(device, xmlDevice, logger);
                device.save();
                for (NamedEncryptedDataType deviceKey : xmlDevice.getKey()) {
                    importDeviceKey(device, deviceKey, transportKeys, logger);
                }
                postProcessDevice(device, xmlDevice, shipment, logger);
                log(logger, MessageSeeds.IMPORTED_DEVICE, deviceName);
                deviceCount++;
            } catch (ImportFailedException e) {
                throw e;
            }
            catch (Exception e) {
                log(logger, MessageSeeds.IMPORT_FAILED_FOR_DEVICE, deviceName, e);
                throw new RuntimeException(e);
            }
        }
        return deviceCount;
    }

    protected String getSymmetricAlgorithm(NamedEncryptedDataType deviceKey) throws
            KeyImportFailedException {
        if (deviceKey.getEncryptionMethod() != null && deviceKey.getEncryptionMethod().getAlgorithm() != null) {
            return securityManagementService.getSymmetricAlgorithm(deviceKey.getEncryptionMethod().getAlgorithm())
                    .map(SymmetricAlgorithm::getCipherName)
                    .orElse(DEFAULT_SYMMETRIC_ALGORITHM);
        } else {
            return DEFAULT_SYMMETRIC_ALGORITHM;
        }
    }

    protected String getAsymmetricAlgorithm(WrapKey wrapKey) {
        if (wrapKey.getSymmetricKey().getEncryptionMethod()!=null && wrapKey.getSymmetricKey().getEncryptionMethod().getAlgorithm()!=null) {
            return wrapKey.getSymmetricKey().getEncryptionMethod().getAlgorithm();
        } else {
            return DEFAULT_ASYMMETRIC_ALGORITHM;
        }
    }

    private void storeCertificationDate(Device device, Shipment shipment) {
        if (shipment.getHeader().getCertificationDate() != null) {
            device.setYearOfCertification(shipment.getHeader().getCertificationDate().getYear());
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
        List<NamedAttribute> deviceAttributesList = Stream.concat(xmlDevice.getAttribute().stream(), shipment.getHeader().getAttribute().stream()).collect(Collectors.toList());

        Map<String,String> values = deviceAttributesList
                .stream()
                .collect(Collectors.toMap(NamedAttribute::getName, NamedAttribute::getValue));
        if(importerExtension.isPresent()) {
            importerExtension.get().process(device, values, logger);
            device.save();
        }
    }

    protected DeviceConfiguration findDefaultDeviceConfig(DeviceType deviceType) {
        DeviceConfiguration defaultDeviceConfiguration = deviceType.getConfigurations()
                .stream()
                .filter(dc -> dc.isDefault())
                .findAny()
                .orElseThrow(() -> new ImportFailedException(MessageSeeds.NO_DEFAULT_DEVICE_CONFIG_FOUND));
        if (!defaultDeviceConfiguration.isActive()) {
            throw new ImportFailedException(MessageSeeds.DEFAULT_DEVICE_CONFIG_FOUND_NOT_ACTIVE);
        }
        return defaultDeviceConfiguration;
    }

    protected DeviceType findDeviceType(Shipment shipment) {
        String deviceTypeName = shipment.getHeader().getDeviceType();
        return deviceConfigurationService.findDeviceTypeByName(deviceTypeName)
                .orElseThrow(() -> new ImportFailedException(MessageSeeds.NO_SUCH_DEVICE_TYPE, deviceTypeName));
    }

    protected void log(Logger logger, MessageSeeds messageSeed, Object... e) {
        logger.log(messageSeed.getLevel(), thesaurus.getFormat(messageSeed).format(e));
    }

    Shipment getShipmentFileFomQueueMessage(InputStream shipmentFileInputStream) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Shipment.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(getSchema());
        return (Shipment) unmarshaller.unmarshal(shipmentFileInputStream);
    }

    private Schema getSchema() {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return sf.newSchema(getClass().getClassLoader().getResource("GenericShipmentv1-5.xsd"));
        } catch (SAXException e) {
            throw new SchemaFailedException(thesaurus, e);
        }
    }

    protected Optional<SecurityAccessorType> getSecurityAccessorType(Device device, String securityAccessorName, Logger logger) {
        return device.getDeviceType()
                .getSecurityAccessorTypes()
                .stream()
                .filter(kat -> kat.getName().equals(securityAccessorName))
                .findAny();
    }

    private interface DeviceCreator {
        Device createDevice(DeviceConfiguration deviceConfiguration, String serialNumber, String name);
    }
}
