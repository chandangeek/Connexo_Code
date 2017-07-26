package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.TrustStore;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.Shipment;

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
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.URIResolver;
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
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Created by bvn on 7/19/17.
 */
public class SecureDeviceShipmentImporter implements FileImporter {
    private static final String SIGNATURE_TAG = "Signature";

    private final Thesaurus thesaurus;
    private final TrustStore trustStore;
    private CertificateFactory certificateFactory;

    public SecureDeviceShipmentImporter(Thesaurus thesaurus, TrustStore trustStore) {
        this.thesaurus = thesaurus;
        this.trustStore = trustStore;
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
            throw new RuntimeException(thesaurus.getFormat(MessageSeeds.NO_CERTIFICATE_FOUND_IN_SHIPMENT).format());
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

        Optional<PublicKey> publicKeyOptional = getPublicKeyFromShipmentFile(logger, shipment, certificate);
        if (!publicKeyOptional.isPresent()) {
            log(logger, MessageSeeds.NO_PUBLIC_KEY_FOUND_FOR_SIGNATURE_VALIDATION);
            throw new RuntimeException(thesaurus.getFormat(MessageSeeds.NO_PUBLIC_KEY_FOUND_FOR_SIGNATURE_VALIDATION).format());
        }

        verifySignature(inputStreamProvider.get(), publicKeyOptional.get(), logger);



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
                log(logger, MessageSeeds.INVALID_SIGNATURE);
                throw new RuntimeException(thesaurus.getFormat(MessageSeeds.INVALID_SIGNATURE).format());
            } else {
                log(logger, MessageSeeds.SIGNATURE_OF_THE_SHIPMENT_FILE_VERIFIED_SUCCESSFULLY);
            }
        } catch (Exception e) {
            log(logger, MessageSeeds.SIGNATURE_VALIDATION_FAILED, e.getMessage());
            throw new RuntimeException(thesaurus.getFormat(MessageSeeds.SIGNATURE_VALIDATION_FAILED).format(e.getMessage()));
        }
    }

    private Node getSignatureNode(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
        builder.setNamespaceAware(true);
        Document xmlInput = builder.newDocumentBuilder().parse(inputStream);
        return xmlInput.getElementsByTagNameNS("*", SIGNATURE_TAG).item(0);
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

}
