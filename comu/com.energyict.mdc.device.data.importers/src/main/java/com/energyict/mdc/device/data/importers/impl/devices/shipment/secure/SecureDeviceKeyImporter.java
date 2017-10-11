package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.DeviceSecretImporter;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyImportFailedException;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.SymmetricAlgorithm;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.Body;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.NamedEncryptedDataType;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.Shipment;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.WrapKey;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import com.google.common.io.ByteStreams;
import org.w3._2000._09.xmldsig_.KeyValueType;
import org.w3._2000._09.xmldsig_.RSAKeyValueType;
import org.w3._2000._09.xmldsig_.X509DataType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.inject.Provider;
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
import java.io.ByteArrayOutputStream;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toMap;

/**
 * Created by bvn on 7/19/17.
 */
public class SecureDeviceKeyImporter implements FileImporter {
    private static final String SIGNATURE_XML_TAG = "Signature";
    private static final String DEFAULT_SYMMETRIC_ALGORITHM = "AES/CBC/PKCS5PADDING";
    private static final String DEFAULT_ASYMMETRIC_ALGORITHM = "RSA/ECB/PKCS1Padding";

    private final Thesaurus thesaurus;
    private final TrustStore trustStore;
    private CertificateFactory certificateFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
    private final PkiService pkiService;

    public SecureDeviceKeyImporter(Thesaurus thesaurus, TrustStore trustStore, DeviceConfigurationService deviceConfigurationService, DeviceService deviceService, PkiService pkiService) {
        this.thesaurus = thesaurus;
        this.trustStore = trustStore;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceService = deviceService;
        this.pkiService = pkiService;
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
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

    /**
     * We'll need to read the input stream more than once.
     */
    private Provider<InputStream> asReusableInputStream(FileImportOccurrence fileImportOccurrence) throws IOException {
        InputStream inputStream = fileImportOccurrence.getContents();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(100);
        ByteStreams.copy(inputStream, byteArrayOutputStream);
        final byte[] bytes = byteArrayOutputStream.toByteArray();
        return () -> new ByteArrayInputStream(bytes);
    }

    private void doProcess(FileImportOccurrence fileImportOccurrence, Logger logger) throws
            JAXBException,
            CertificateException,
            InvalidAlgorithmParameterException,
            NoSuchAlgorithmException,
            CertPathValidatorException, IOException {
        Provider<InputStream> inputStreamProvider = asReusableInputStream(fileImportOccurrence);
        Shipment shipment = getShipmentFileFomQueueMessage(inputStreamProvider.get());
        certificateFactory = CertificateFactory.getInstance("X.509");
        Optional<X509Certificate> certificate = findCertificate(shipment, logger);
        if (certificate.isPresent()) {
            trustStore.validate(certificate.get());
        }

        PublicKey publicKey = getPublicKeyFromShipmentFile(logger, shipment, certificate).orElseThrow(()->new ImportFailedException(MessageSeeds.NO_PUBLIC_KEY_FOUND_FOR_SIGNATURE_VALIDATION));

        verifySignature(inputStreamProvider.get(), publicKey, logger);
        DeviceCreator deviceCreator = getDeviceCreator(shipment);

        int importDevices = importDevices(shipment, deviceCreator, logger);
        fileImportOccurrence.markSuccess(thesaurus.getFormat(MessageSeeds.IMPORT_COMPLETED).format(importDevices));
    }

    private DeviceCreator getDeviceCreator(Shipment shipment) {
        DeviceCreator deviceCreator = null;
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

    private int importDevices(Shipment shipment, DeviceCreator deviceCreator, Logger logger) {
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
            } catch (Exception e) {
                log(logger, MessageSeeds.IMPORT_FAILED_FOR_DEVICE, deviceName, e);
                throw e;
            }
        }
        return deviceCount;
    }

    private void importDeviceKey(Device device, NamedEncryptedDataType deviceKey, Map<String, WrapKey> wrapKeyMap, Logger logger) {
        String securityAccessorName = deviceKey.getName();
        Optional<KeyAccessorType> keyAccessorTypeOptional = device.getDeviceType()
                .getKeyAccessorTypes()
                .stream()
                .filter(kat -> kat.getName().equals(securityAccessorName))
                .findAny();
        if (!keyAccessorTypeOptional.isPresent()) {
            log(logger, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE_ON_DEVICE_TYPE, device.getName(), securityAccessorName);
        } else {
            final KeyAccessorType keyAccessorType = keyAccessorTypeOptional.get();
            final WrapKey wrapKey = wrapKeyMap.get(deviceKey.getWrapKeyLabel());
            if (wrapKey==null) {
                throw new ImportFailedException(MessageSeeds.WRAP_KEY_NOT_FOUND, securityAccessorName, device.getName(), deviceKey.getWrapKeyLabel());
            }
            DeviceSecretImporter deviceSecretImporter = pkiService.getDeviceSecretImporter(keyAccessorType);
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


            if (device.getKeyAccessor(keyAccessorType).isPresent() && device.getKeyAccessor(keyAccessorType).get().getActualValue().isPresent()) {
                log(logger, MessageSeeds.ACTUAL_VALUE_ALREADY_EXISTS, securityAccessorName, device.getName());
            } else {
                KeyAccessor keyAccessor = device.getKeyAccessor(keyAccessorType).orElseGet(()->device.newKeyAccessor(keyAccessorType));
                SecurityValueWrapper newWrapperValue = deviceSecretImporter.importSecret(encryptedDeviceKey, initializationVector, encryptedSymmetricKey, symmetricAlgorithm, asymmetricAlgorithm);
                keyAccessor.setActualValue(newWrapperValue);
                keyAccessor.save();
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
     * Creates a map to quickly get a WrapKey from its label
     */
    private Map<String, WrapKey> createWrapKeyMap(Shipment shipment) {
        return shipment.getHeader()
                    .getWrapKey()
                    .stream()
                    .collect(toMap(WrapKey::getLabel, Function.identity()));
    }

    private String getSymmetricAlgorithm(NamedEncryptedDataType deviceKey) throws
            KeyImportFailedException {
        if (deviceKey.getEncryptionMethod() != null && deviceKey.getEncryptionMethod().getAlgorithm() != null) {
            return pkiService.getSymmetricAlgorithm(deviceKey.getEncryptionMethod().getAlgorithm())
                    .map(SymmetricAlgorithm::getCipherName)
                    .orElse(DEFAULT_SYMMETRIC_ALGORITHM);
        } else {
            return DEFAULT_SYMMETRIC_ALGORITHM;
        }
    }

    private String getAsymmetricAlgorithm(WrapKey wrapKey) {
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
    }

    private DeviceConfiguration findDefaultDeviceConfig(DeviceType deviceType) {
        DeviceConfiguration defaultDeviceConfiguration = deviceType.getConfigurations()
                .stream()
                .filter(dc -> dc.getName().equals("Default"))
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

    private void log(Logger logger, MessageSeeds messageSeed, Object... e) {
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

    private class ImportFailedException extends RuntimeException {
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
        Device createDevice(DeviceConfiguration deviceConfiguration, String name);
    }

}
