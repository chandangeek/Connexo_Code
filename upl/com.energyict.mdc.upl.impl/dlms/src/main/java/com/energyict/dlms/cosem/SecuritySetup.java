package com.energyict.dlms.cosem;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.io.NestedIOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.SecurityMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class SecuritySetup extends AbstractCosemObject {

    static final byte[] LN = new byte[]{0, 0, 43, 0, 0, (byte) 255};
    /**
     * Attribute numbers
     */
    private static final int ATTRB_SECURITY_POLICY = 2;
    private static final int ATTRB_SECURITY_SUITE = 3;
    private static final int ATTRB_CLIENT_SYSTEM_TITLE = 4;
    private static final int ATTRB_SERVER_SYSTEM_TITLE = 5;
    private static final int ATTRB_CERTIFICATES = 6;
    /**
     * Methods
     */
    private static final int METHOD_SECURITY_ACTIVATE = 1;                  // Activates and strengthens the security policy
    private static final int METHOD_GLOBAL_KEY_TRANSFER = 2;                // Update one or more global keys
    private static final int METHOD_KEY_AGREEMENT = 3;                    // Agree on new symmetric keys
    private static final int METHOD_GENERATE_KEY_PAIR = 4;                // Generate new EC key pair, for the currently active security suite.
    private static final int METHOD_GENERATE_CERTIFICATE_REQUEST = 5;        // Let the device generate a CSR
    private static final int METHOD_IMPORT_CERTIFICATE = 6;                // Import a signed certificate into the DLMS device
    private static final int METHOD_EXPORT_CERTIFICATE = 7;                // Export a certificate from the DLMS device
    private static final int METHOD_REMOVE_CERTIFICATE = 8;                // Remove a certificate from the DLMS device
    /**
     * Attributes
     */
    private TypeEnum securityPolicy = null;        //Enforces authentication and/or encryption algorithm provided with security_suite.
    private TypeEnum securitySuite = null;            //Specifies authentication, encryption and key wrapping algorithm.
    private OctetString clientSystemTitle = null;    //Carries the current client system title
    private OctetString serverSystemTitle = null;    //Carries the server system title

    public SecuritySetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public SecuritySetup(ProtocolLink protocolLink) {
        super(protocolLink, new ObjectReference(LN));
    }

    public static ObisCode getDefaultObisCode() {
        return ObisCode.fromByteArray(LN);
    }

    protected int getClassId() {
        return DLMSClassId.SECURITY_SETUP.getClassId();
    }

    /**
     * Read the current securityPolicy from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public TypeEnum readSecurityPolicy() throws IOException {
        try {
            this.securityPolicy = new TypeEnum(getLNResponseData(ATTRB_SECURITY_POLICY), 0);
            return this.securityPolicy;
        } catch (IOException e) {
            e.printStackTrace();
            throw new NestedIOException(e, "Couldn't read the securityPolicy." + e.getMessage());
        }
    }

    /**
     * Write the given securityPolicy to the device
     *
     * @param securityPolicy
     * @throws java.io.IOException
     */
    public void writeSecurityPolicy(TypeEnum securityPolicy) throws IOException {
        try {
            write(ATTRB_SECURITY_POLICY, securityPolicy.getBEREncodedByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            throw new NestedIOException(e, "Couldn't write the securityPolicy to the device." + e.getMessage());
        }
    }

    /**
     * @return the current securityPolicy
     * @throws java.io.IOException
     */
    public TypeEnum getSecurityPolicy() throws IOException {
        if (this.securityPolicy == null) {
            return readSecurityPolicy();
        } else {
            return this.securityPolicy;
        }
    }

    /**
     * Read the current securitySuite from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public TypeEnum readSecuritySuite() throws IOException {
        try {
            this.securitySuite = new TypeEnum(getLNResponseData(ATTRB_SECURITY_SUITE), 0);
            return this.securitySuite;
        } catch (IOException e) {
            e.printStackTrace();
            throw new NestedIOException(e, "Couldn't read the securitySuite." + e.getMessage());
        }
    }

    /**
     * @return the current securitySuite
     * @throws java.io.IOException
     */
    public TypeEnum getSecuritySuite() throws IOException {
        if (this.securitySuite == null) {
            return readSecuritySuite();
        } else {
            return this.securitySuite;
        }
    }

    /**
     * Write the given securitySuite to the device
     *
     * @param securitySuite
     * @throws java.io.IOException
     */
    public void writeSecuritySuite(TypeEnum securitySuite) throws IOException {
        try {
            write(ATTRB_SECURITY_SUITE, securitySuite.getBEREncodedByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            throw new NestedIOException(e, "Couldn't write the securitySuite to the device." + e.getMessage());
        }
    }

    public List<CertificateInfo> readCertificates() throws IOException {
        List<CertificateInfo> result = new ArrayList<>();

        Array array = new Array(getLNResponseData(ATTRB_CERTIFICATES), 0, 0);

        for (AbstractDataType abstractDataType : array) {
            if (!abstractDataType.isStructure()) {
                throw new ProtocolException("Error while parsing the certificates attribute: should be an array of structures.");
            }

            Structure structure = abstractDataType.getStructure();
            if (structure.nrOfDataTypes() != 6) {
                throw new ProtocolException("Error while parsing the certificates attribute: the certificate info should consist of 6 elements, received a structure with '" + structure.nrOfDataTypes() + "' elements.");
            }
            CertificateInfo certificateInfo = new CertificateInfo(
                    structure.getDataType(0).getTypeEnum().getValue(),
                    structure.getDataType(1).getTypeEnum().getValue(),
                    structure.getDataType(2).getOctetString().stringValue(),
                    structure.getDataType(3).getOctetString().stringValue(),
                    structure.getDataType(4).getOctetString().stringValue(),
                    structure.getDataType(5).getOctetString().stringValue()
            );

            result.add(certificateInfo);
        }
        return result;
    }

    /**
     * Generate a new EC key pair in the DLMS device.
     * There is maximum one key pair for digital signature, one key pair for key
     * agreement and one key pair for TLS.
     * <p/>
     * (0) digital signature key pair,
     * (1) key agreement key pair,
     * (2) TLS key pair
     */
    public void generateKeyPair(int type) throws IOException {
        invoke(METHOD_GENERATE_KEY_PAIR, new TypeEnum(type).getBEREncodedByteArray());
    }

    /**
     * Generate a 'certificate signing request' for a newly generated key pair.
     * This CSR should then be sent (out of scope) to the CA, which will sign it with its private key.
     * The result is an "end device" certificate that should be imported into the DLMS device, and into our own persisted key store.
     *
     * @return the CSR, formatted as specified in PKCS #10 (RFC 2986).
     */
    public byte[] generateCSR(int type) throws IOException {
        byte[] response = invoke(METHOD_GENERATE_CERTIFICATE_REQUEST, new TypeEnum(type).getBEREncodedByteArray());
        OctetString csr = new OctetString(response, 0);
        return csr.getOctetStr();
    }

    /**
     * Data is formatted as DER encoded X.509 v3 (RFC 5280).
     */
    public void importCertificate(byte[] encodedCertificate) throws IOException {
        invoke(METHOD_IMPORT_CERTIFICATE, OctetString.fromByteArray(encodedCertificate).getBEREncodedByteArray());
    }

    /**
     * Delete a client or CA certificate.
     * Note that the server certificates cannot be deleted, only renewed:
     * <p/>
     * The certificates of the server for digital signature, key agreement and TLS
     * cannot be removed from the server. When update of these certificates is needed the new key
     * pair is generated, new certificate request is generated and new certificate is imported.
     */
    public void deleteCertificate(String serialNumber, String issuer) throws IOException {
        Structure certificateIdentificationBySerial = createCertificateIdentificationBySerial(serialNumber, issuer);
        invoke(METHOD_REMOVE_CERTIFICATE, certificateIdentificationBySerial.getBEREncodedByteArray());
    }

    /**
     * Delete a client or CA certificate.
     * Note that the server certificates cannot be deleted, only renewed:
     * <p/>
     * The certificates of the server for digital signature, key agreement and TLS
     * cannot be removed from the server. When update of these certificates is needed the new key
     * pair is generated, new certificate request is generated and new certificate is imported.
     */
    public void deleteCertificate(int certificateEntity, int certificateType, byte[] systemTitle) throws IOException {
        Structure certificateIdentificationByEntity = createCertificateIdentificationByEntity(certificateEntity, certificateType, OctetString.fromByteArray(systemTitle));
        invoke(METHOD_REMOVE_CERTIFICATE, certificateIdentificationByEntity.getBEREncodedByteArray());
    }

    public X509Certificate exportCertificate(String serialNumber, String issuer) throws IOException {
        Structure certificateIdentification = createCertificateIdentificationBySerial(serialNumber, issuer);

        byte[] result = invoke(METHOD_EXPORT_CERTIFICATE, certificateIdentification.getBEREncodedByteArray());
        try {
            //response data octet-string is formatted as X.509 v3 DER format
            return parseExportedCertificate(result);
        } catch (CertificateException e) {
            throw new ProtocolException(e, "Error while parsing exported certificate with serial number '" + serialNumber + "' and issuer '" + issuer + "': " + e.getMessage());
        }
    }

    private Structure createCertificateIdentificationBySerial(String serialNumber, String issuer) {
        Structure certificateIdentification = new Structure();
        certificateIdentification.addDataType(new TypeEnum(1));     //(1) certificate_identification_serial
        Structure certificateIdentificationBySerial = new Structure();
        certificateIdentificationBySerial.addDataType(OctetString.fromString(serialNumber));
        certificateIdentificationBySerial.addDataType(OctetString.fromString(issuer));
        certificateIdentification.addDataType(certificateIdentificationBySerial);
        return certificateIdentification;
    }

    private X509Certificate parseExportedCertificate(byte[] result) throws IOException, CertificateException {
        OctetString encodedCertificate = new OctetString(result, 0);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(encodedCertificate.getOctetStr());
        return (X509Certificate) certFactory.generateCertificate(in);
    }

    public X509Certificate exportCertificate(int certificateEntity, int certificateType, byte[] systemTitle) throws IOException {
        Structure certificateIdentification = createCertificateIdentificationByEntity(certificateEntity, certificateType, OctetString.fromByteArray(systemTitle));

        byte[] result = invoke(METHOD_EXPORT_CERTIFICATE, certificateIdentification.getBEREncodedByteArray());
        OctetString encodedCertificate = new OctetString(result, 0);
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(encodedCertificate.getOctetStr());
            return (X509Certificate) certFactory.generateCertificate(in);
        } catch (CertificateException e) {
            throw new ProtocolException(e, "Error while parsing exported certificate for entity '" +
                    SecurityMessage.CertificateEntity.fromId(certificateEntity).name() + "', type '" +
                    SecurityMessage.CertificateType.fromId(certificateType).name() + "' and system-title '" +
                    ProtocolTools.getHexStringFromBytes(systemTitle, "") + "': " + e.getMessage());
        }
    }

    private Structure createCertificateIdentificationByEntity(int certificateEntity, int certificateType, OctetString systemTitle) {
        Structure certificateIdentification = new Structure();
        certificateIdentification.addDataType(new TypeEnum(0));     //(0) certificate_identification_entity
        Structure certificateIdentificationByEntity = new Structure();
        certificateIdentificationByEntity.addDataType(new TypeEnum(certificateEntity));
        certificateIdentificationByEntity.addDataType(new TypeEnum(certificateType));
        certificateIdentificationByEntity.addDataType(systemTitle);
        certificateIdentification.addDataType(certificateIdentificationByEntity);
        return certificateIdentification;
    }

    /**
     * @return the current clientSystem title
     * @throws java.io.IOException
     */
    public OctetString readClientSystemTitle() throws IOException {
        try {
            this.clientSystemTitle = new OctetString(getLNResponseData(ATTRB_CLIENT_SYSTEM_TITLE), 0);
            return this.clientSystemTitle;
        } catch (IOException e) {
            e.printStackTrace();
            throw new NestedIOException(e, "Couldn't read the current client system title from the device." + e.getMessage());
        }
    }

    /**
     * @return the serverSystem title
     * @throws java.io.IOException
     */
    public OctetString readServerSystemTitle() throws IOException {
        try {
            this.serverSystemTitle = new OctetString(getLNResponseData(ATTRB_SERVER_SYSTEM_TITLE), 0);
            return this.serverSystemTitle;
        } catch (IOException e) {
            e.printStackTrace();
            throw new NestedIOException(e, "Couldn't read the server system title." + e.getMessage());
        }
    }

    /**
     * Activate the given securityPolicy for this device.
     * <b>NOTE:</b> THE SECURITY POLICY CAN ONLY BE STRENGTHENED
     *
     * @param securityPolicy <pre>Values for securityPolicy:
     *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         	(0)    nothing,
     *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         	(1)    all messages to be authenticated,
     *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         	(2)    all messages to be encrypted,
     *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         	(3)    all messages to be authenticated and encrypted.
     *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         </pre>
     * @return
     * @throws java.io.IOException
     */
    public byte[] activateSecurity(TypeEnum securityPolicy) throws IOException {
        try {
            return invoke(METHOD_SECURITY_ACTIVATE, securityPolicy.getBEREncodedByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            throw new NestedIOException(e, "Could not activate the securityPolicy." + e.getMessage());
        }
    }

    /**
     * Transfer one or more global keys to the device.
     * The global keys must be wrapped with the MasterKey
     *
     * @param keyData
     * @return
     * @throws java.io.IOException
     */
    public byte[] transferGlobalKey(Array keyData) throws IOException {
        try {
            return invoke(METHOD_GLOBAL_KEY_TRANSFER, keyData.getBEREncodedByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            throw new NestedIOException(e, "Could not transfer the globalKey(s)" + e.getMessage());
        }
    }

    /**
     * Used to agree on one or more symmetric keys using the key
     * agreement algorithm as specified by the security suite. In the case of
     * suites 1 and 2 the ECDH key agreement algorithm is used with the
     * Ephemeral Unified Model C(2e, 0s, ECC CDH) scheme.
     */
    public byte[] keyAgreement(Array keyAgreementDatas) throws IOException {
        return invoke(METHOD_KEY_AGREEMENT, keyAgreementDatas.getBEREncodedByteArray());
    }

    public class CertificateInfo {
        SecurityMessage.CertificateEntity certificateEntity;
        SecurityMessage.CertificateType certificateType;
        String serialNumber;
        String issuer;
        String subject;
        String subjectAlternativeName;

        public CertificateInfo(int certificateEntityId, int certificateTypeId, String serialNumber, String issuer, String subject, String subjectAlternativeName) {
            this.certificateEntity = SecurityMessage.CertificateEntity.fromId(certificateEntityId);
            this.certificateType = SecurityMessage.CertificateType.fromId(certificateTypeId);
            this.serialNumber = serialNumber;
            this.issuer = issuer;
            this.subject = subject;
            this.subjectAlternativeName = subjectAlternativeName;
        }

        public SecurityMessage.CertificateEntity getCertificateEntity() {
            return certificateEntity;
        }

        public SecurityMessage.CertificateType getCertificateType() {
            return certificateType;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public String getIssuer() {
            return issuer;
        }
    }
}
