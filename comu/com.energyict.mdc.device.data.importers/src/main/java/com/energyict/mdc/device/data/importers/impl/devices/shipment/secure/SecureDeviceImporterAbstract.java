/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.KeyImportFailedException;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SymmetricAlgorithm;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.streams.ReusableInputStream;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.ImporterExtension;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.Body;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.NamedAttribute;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.NamedEncryptedDataType;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.Shipment;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.WrapKey;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.exception.ImportFailedException;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import org.w3._2000._09.xmldsig_.KeyValueType;
import org.w3._2000._09.xmldsig_.RSAKeyValueType;
import org.w3._2000._09.xmldsig_.X509DataType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.validation.ConstraintViolationException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public abstract class SecureDeviceImporterAbstract {

    private static final String SIGNATURE_XML_TAG = "Signature";
    private static final String DEFAULT_SYMMETRIC_ALGORITHM = "AES/CBC/PKCS5PADDING";
    private static final String DEFAULT_ASYMMETRIC_ALGORITHM = "RSA/ECB/PKCS1Padding";

    private final Thesaurus thesaurus;
    private final TrustStore trustStore;
    private CertificateFactory certificateFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
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
            throw new RuntimeException(thesaurus.getFormat(MessageSeeds.FAILED_TO_VERIFY_CERTIFICATE).format());
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
            validateFileCertificate(logger, inputStreamProvider, shipment);
        }
        SecureDeviceImporterAbstract.DeviceCreator deviceCreator = getDeviceCreator(shipment);

        int importDevices = importDevices(shipment, deviceCreator, logger);
        fileImportOccurrence.markSuccess(thesaurus.getFormat(MessageSeeds.IMPORT_COMPLETED).format(importDevices));
    }

    protected abstract boolean shouldValidateCert();

    private void validateFileCertificate(Logger logger, ReusableInputStream inputStreamProvider, Shipment shipment) throws
            CertificateException,
            InvalidAlgorithmParameterException,
            NoSuchAlgorithmException,
            CertPathValidatorException {
        certificateFactory = CertificateFactory.getInstance("X.509");
        Optional<X509Certificate> certificate = findCertificate(shipment, logger);
        if (certificate.isPresent()) {
            trustStore.validate(certificate.get());
        }
        PublicKey publicKey = getPublicKeyFromShipmentFile(logger, shipment, certificate).orElseThrow(()->new ImportFailedException(MessageSeeds.NO_PUBLIC_KEY_FOUND_FOR_SIGNATURE_VALIDATION));
        verifySignature(inputStreamProvider.stream(), publicKey, logger);
    }

    private SecureDeviceImporterAbstract.DeviceCreator getDeviceCreator(Shipment shipment) {
        SecureDeviceImporterAbstract.DeviceCreator deviceCreator = null;
        if (Checks.is(shipment.getHeader().getBatchID()).emptyOrOnlyWhiteSpace()) {
            deviceCreator = (deviceConfiguration, name) -> deviceService.newDevice(
                    deviceConfiguration,
                    name,
                    shipment.getHeader().getBatchID(),
                    shipment.getHeader().getDeliveryDate().toGregorianCalendar().toInstant());
        } else {
            deviceCreator = (deviceConfiguration, name) -> deviceService.newDevice(deviceConfiguration,
                    name,
                    shipment.getHeader().getDeliveryDate().toGregorianCalendar().toInstant());
        }
        return deviceCreator;
    }

    private int importDevices(Shipment shipment, SecureDeviceImporterAbstract.DeviceCreator deviceCreator, Logger logger)  {
        int deviceCount = 0;
        DeviceType deviceType = findDeviceType(shipment);
        Map<String, WrapKey> wrapKeyMap = createWrapKeyMap(shipment);

        DeviceConfiguration deviceConfiguration = findDefaultDeviceConfig(deviceType);

        for (Body.Device xmlDevice : shipment.getBody().getDevice()) {
            String deviceName = Checks.is(xmlDevice.getUniqueIdentifier())
                    .emptyOrOnlyWhiteSpace() ? xmlDevice.getSerialNumber() : xmlDevice.getUniqueIdentifier();
            log(logger, MessageSeeds.IMPORTING_DEVICE, deviceName);
            try {
                if (deviceService.findDeviceByName(deviceName).isPresent()) {
                    log(logger, MessageSeeds.DEVICE_WITH_NAME_ALREADY_EXISTS, deviceName);
                    continue;
                }
                Device device = deviceCreator.createDevice(deviceConfiguration, deviceName);
                device.setManufacturer(shipment.getHeader().getManufacturer());
                device.setSerialNumber(xmlDevice.getSerialNumber());
                storeCertificationDate(device, shipment);
                storeMacAddress(device, xmlDevice, logger);
                device.save();
                for (NamedEncryptedDataType deviceKey : xmlDevice.getKey()) {
                    importDeviceKey(device, deviceKey, wrapKeyMap, logger);
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

    protected abstract void importDeviceKey(Device device, NamedEncryptedDataType deviceKey, Map<String, WrapKey> wrapKeyMap, Logger logger) throws HsmBaseException, ImportFailedException;

    /**
     * Creates a map to quickly get a WrapKey from its label
     */
    private Map<String, WrapKey> createWrapKeyMap(Shipment shipment) {
        return shipment.getHeader()
                .getWrapKey()
                .stream()
                .collect(toMap(WrapKey::getLabel, Function.identity()));
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

    private void storeMacAddress(Device device, Body.Device xmlDevice, Logger logger) {
        if (!Checks.is(xmlDevice.getMACAddress()).emptyOrOnlyWhiteSpace()) {
            Optional<DeviceProtocolPluggableClass> pluggableClassOptional = device.getDeviceType().getDeviceProtocolPluggableClass();
            if (pluggableClassOptional.isPresent()) {
                Optional<PropertySpec> mac_address = pluggableClassOptional.get()
                        .getDeviceProtocol()
                        .getPropertySpecs()
                        .stream()
                        .filter(spec -> spec.getName().equalsIgnoreCase("MAC_ADDRESS"))
                        .findAny();
                if (mac_address.isPresent()) {
                    device.setProtocolProperty(mac_address.get().getName(), xmlDevice.getMACAddress());
                } else {
                    Optional<PropertySpec> callHomeId = pluggableClassOptional.get()
                            .getDeviceProtocol()
                            .getPropertySpecs()
                            .stream()
                            .filter(spec -> spec.getName().equalsIgnoreCase(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME))
                            .findAny();
                    if (callHomeId.isPresent()) {
                        device.setProtocolProperty(callHomeId.get().getName(), xmlDevice.getMACAddress());
                    } else {
                        log(logger, MessageSeeds.FAILED_TO_STORE_MAC_ADDRESS, device.getName());
                    }
                }
            }
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

    private DeviceConfiguration findDefaultDeviceConfig(DeviceType deviceType) {
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

    private DeviceType findDeviceType(Shipment shipment) {
        String deviceTypeName = shipment.getHeader().getDeviceType();
        return deviceConfigurationService.findDeviceTypeByName(deviceTypeName)
                .orElseThrow(() -> new ImportFailedException(MessageSeeds.NO_SUCH_DEVICE_TYPE, deviceTypeName));
    }

    private Optional<PublicKey> getPublicKeyFromShipmentFile(Logger logger, Shipment shipment, Optional<X509Certificate> certificate) {
        if (certificate.isPresent()) {
            return Optional.ofNullable(certificate.get().getPublicKey());
        } else {
            Optional<PublicKey> publicKeyOptional = findPublicKey(shipment, logger);
            if (publicKeyOptional.isPresent()) {
                return Optional.of(publicKeyOptional.get());
            }
        }
        return Optional.empty();
    }

    protected void log(Logger logger, MessageSeeds messageSeed, Object... e) {
        logger.log(messageSeed.getLevel(), thesaurus.getFormat(messageSeed).format(e));
    }


    private void verifySignature(InputStream inputStream, PublicKey publicKey, Logger logger) throws CertificateException {
        try {
            DOMValidateContext validateContext = new DOMValidateContext(publicKey, getSignatureNode(inputStream));

            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
            XMLSignature signature = fac.unmarshalXMLSignature(validateContext);
            if(!signature.validate(validateContext)) {
                throw new ImportFailedException(MessageSeeds.INVALID_SIGNATURE);
            } else {
                log(logger, MessageSeeds.SIGNATURE_OF_THE_SHIPMENT_FILE_VERIFIED_SUCCESSFULLY);
            }
        } catch (Exception e) {
            throw new ImportFailedException(MessageSeeds.SIGNATURE_VALIDATION_FAILED);
        }
    }

    private Node getSignatureNode(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
        builder.setNamespaceAware(true);
        Document xmlInput = builder.newDocumentBuilder().parse(inputStream);
        return xmlInput.getElementsByTagNameNS("*", SIGNATURE_XML_TAG).item(0);
    }

    private Optional<X509Certificate> findCertificate(Shipment shipment, Logger logger) throws CertificateException {
        for(Object keyInfoObject : shipment.getSignature().getKeyInfo().getContent()) {
            if(keyInfoObject instanceof JAXBElement && ((JAXBElement) keyInfoObject).getValue() instanceof X509DataType) {
                for(Object x509DataObject : ((X509DataType) ((JAXBElement) keyInfoObject).getValue()).getX509IssuerSerialOrX509SKIOrX509SubjectName()) {
                    if(x509DataObject instanceof JAXBElement && ((JAXBElement) x509DataObject).getName().getLocalPart().equals("X509Certificate")) {
                        InputStream certificateStream = new ByteArrayInputStream((byte[]) ((JAXBElement) x509DataObject).getValue());
                        try {
                            return Optional.of((X509Certificate) certificateFactory.generateCertificate(certificateStream));
                        } catch (CertificateException e) {
                            log(logger, MessageSeeds.FAILED_TO_CREATE_CERTIFICATE, e);
                            return Optional.empty();
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    public Optional<PublicKey> findPublicKey(Shipment shipment, Logger logger) {
        for(Object keyInfoObject : shipment.getSignature().getKeyInfo().getContent()) {
            if(keyInfoObject instanceof JAXBElement && ((JAXBElement) keyInfoObject).getValue() instanceof KeyValueType) {
                for(Object keyObject : ((KeyValueType) ((JAXBElement) keyInfoObject).getValue()).getContent()) {
                    if(keyObject instanceof JAXBElement && ((JAXBElement) keyObject).getValue() instanceof RSAKeyValueType) {
                        RSAKeyValueType rsaKeyValueType = (RSAKeyValueType) ((JAXBElement) keyObject).getValue();
                        try {
                            KeyFactory factory = KeyFactory.getInstance("RSA");
                            BigInteger modulus = new BigInteger(1, rsaKeyValueType.getModulus());
                            BigInteger exponent = new BigInteger(1, rsaKeyValueType.getExponent());
                            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
                            return Optional.of(factory.generatePublic(spec));
                        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                            if(logger != null){
                                log(logger, MessageSeeds.FAILED_TO_CREATE_PUBLIC_KEY, e);
                            }
                            return Optional.empty();
                        }
                    }
                }
            }
        }
        return Optional.empty();
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
        Device createDevice(DeviceConfiguration deviceConfiguration, String name);
    }
}
