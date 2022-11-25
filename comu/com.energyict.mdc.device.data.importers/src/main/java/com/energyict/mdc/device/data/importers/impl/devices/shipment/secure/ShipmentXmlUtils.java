package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.streams.ReusableInputStream;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.Body;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.Shipment;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.exception.ImportFailedException;
import com.energyict.mdc.protocol.LegacyProtocolProperties;

import org.w3._2000._09.xmldsig_.KeyValueType;
import org.w3._2000._09.xmldsig_.RSAKeyValueType;
import org.w3._2000._09.xmldsig_.X509DataType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBElement;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import java.util.Optional;
import java.util.logging.Logger;

public class ShipmentXmlUtils {

    private static final String SIGNATURE_XML_TAG = "Signature";

    private final Logger logger;
    private final Thesaurus thesaurus;

    public ShipmentXmlUtils(Logger logger, Thesaurus thesaurus) {
        this.logger = logger;
        this.thesaurus = thesaurus;
    }

    public void validateFileCertificate(ReusableInputStream inputStreamProvider, Shipment shipment, TrustStore trustStore) throws
            CertificateException,
            InvalidAlgorithmParameterException,
            NoSuchAlgorithmException,
            CertPathValidatorException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Optional<X509Certificate> certificate = findCertificate(shipment, logger, certificateFactory);
        if (certificate.isPresent()) {
            trustStore.validate(certificate.get());
        }
        PublicKey publicKey = getPublicKeyFromShipmentFile(logger, shipment, certificate).orElseThrow(()->new ImportFailedException(MessageSeeds.NO_PUBLIC_KEY_FOUND_FOR_SIGNATURE_VALIDATION));
        verifySignature(inputStreamProvider.stream(), publicKey, logger);
    }

    public void storeMacAddress(Device device, Body.Device xmlDevice) {
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

    private void verifySignature(InputStream inputStream, PublicKey publicKey, Logger logger) throws CertificateException {
        try {
            DOMValidateContext validateContext = new DOMValidateContext(publicKey, getSignatureNode(inputStream));

            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
            XMLSignature signature = fac.unmarshalXMLSignature(validateContext);
            if(!signature.validate(validateContext)) {
                throw new SecureDeviceKeyImporter.ImportFailedException(MessageSeeds.INVALID_SIGNATURE);
            } else {
                log(logger, MessageSeeds.SIGNATURE_OF_THE_SHIPMENT_FILE_VERIFIED_SUCCESSFULLY);
            }
        } catch (Exception e) {
            throw new SecureDeviceKeyImporter.ImportFailedException(MessageSeeds.SIGNATURE_VALIDATION_FAILED);
        }
    }

    private Node getSignatureNode(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
        builder.setNamespaceAware(true);
        Document xmlInput = builder.newDocumentBuilder().parse(inputStream);
        return xmlInput.getElementsByTagNameNS("*", SIGNATURE_XML_TAG).item(0);
    }

    private Optional<X509Certificate> findCertificate(Shipment shipment, Logger logger, CertificateFactory certificateFactory) throws CertificateException {
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

    private Optional<PublicKey> findPublicKey(Shipment shipment, Logger logger) {
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



    private void log(Logger logger, MessageSeeds messageSeed, Object... e) {
        logger.log(messageSeed.getLevel(), thesaurus.getFormat(messageSeed).format(e));
    }

}
