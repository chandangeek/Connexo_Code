package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.TrustStore;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.Shipment;
import com.energyict.mdc.upl.nls.MessageSeed;

import org.w3._2000._09.xmldsig_.X509DataType;
import org.xml.sax.SAXException;

import javax.validation.ConstraintViolationException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Created by bvn on 7/19/17.
 */
public class SecureDeviceShipmentImporter implements FileImporter {
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
        } catch (ConstraintViolationException e) {
            log(logger, MessageSeeds.SECURE_DEVICE_IMPORT_FAILED);
            throw new RuntimeException(thesaurus.getFormat(MessageSeeds.SECURE_DEVICE_IMPORT_FAILED).format());
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

    private void doProcess(FileImportOccurrence fileImportOccurrence, Logger logger) throws
            JAXBException,
            CertificateException,
            InvalidAlgorithmParameterException,
            NoSuchAlgorithmException,
            CertPathValidatorException
    {
        Shipment shipment = getShipmentFileFomQueueMessage(fileImportOccurrence);
        certificateFactory = CertificateFactory.getInstance("X.509");
        Optional<X509Certificate> certificate = findCertificate(shipment, logger);
        verifyCertificate(logger, certificate);
        verifySignature(shipment, logger);
    }

    private void verifyCertificate(Logger logger, Optional<X509Certificate> certificate) throws
            InvalidAlgorithmParameterException,
            NoSuchAlgorithmException,
            CertificateException,
            CertPathValidatorException {
        if (certificate.isPresent()) {
            trustStore.validate(certificate.get());
        } else {
            log(logger, MessageSeeds.NO_CERTIFICATE_FOUND_IN_SHIPMENT);
            throw new RuntimeException(thesaurus.getFormat(MessageSeeds.NO_CERTIFICATE_FOUND_IN_SHIPMENT).format());
        }
    }

    private void log(Logger logger, MessageSeeds messageSeed, Object... e) {
        logger.log(messageSeed.getLevel(), thesaurus.getFormat(messageSeed).format(e));
    }

    private void verifySignature(Shipment shipment, Logger logger) throws CertificateException {
        if (shipment.getSignature().getKeyInfo() != null) {
            System.out.println("test");
//            if(certificate != null) {
//                List<X509Certificate> trustCertificates = getTrustCertificates();
//                ShipmentCertificateValidator certificateValidator = new ShipmentCertificateValidator(trustCertificates);
//                certificateValidator.validate(certificate);
//                validateByPublicKey(certificate.getPublicKey());
//            } else {
//                PublicKey keyFromFile = findPublicKey(shipment);
//                PublicKey keyFromConfiguration = getSafeVerifySigKey();
//                if(!Arrays.equals(keyFromFile.getEncoded(), keyFromConfiguration.getEncoded())) {
//                    getLogger().log(Level.SEVERE, LoggingMessages.PUBLIC_KEY_NOT_TRUSTED.getMessage());
//                    throw new ValidationFailedException();
//                }
//                validateByPublicKey(keyFromFile);
//            }
//        } else {
//            validateByPublicKey(getSafeVerifySigKey());
        }
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

    Shipment getShipmentFileFomQueueMessage(FileImportOccurrence fileImportOccurrence) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Shipment.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(getSchema());
        return (Shipment) unmarshaller.unmarshal(fileImportOccurrence.getContents());
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
