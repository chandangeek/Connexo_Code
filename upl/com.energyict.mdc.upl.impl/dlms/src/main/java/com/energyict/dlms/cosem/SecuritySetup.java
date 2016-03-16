package com.energyict.dlms.cosem;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
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
    private static final int METHOD_IMPORT_CERTIFICATE = 6;                // Import a signed certificate
    private static final int METHOD_EXPORT_CERTIFICATE = 7;                // Export a certificate
    private static final int METHOD_REMOVE_CERTIFICATE = 8;                // Remove a certificate
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

    public X509Certificate exportCertificate(String serialNumber, String issuer) throws IOException {
        Structure certificateIdentification = new Structure();
        certificateIdentification.addDataType(new TypeEnum(1));     //(1) certificate_identification_serial
        Structure certificateIdentificationBySerial = new Structure();
        certificateIdentificationBySerial.addDataType(OctetString.fromString(serialNumber));
        certificateIdentificationBySerial.addDataType(OctetString.fromString(issuer));
        certificateIdentification.addDataType(certificateIdentificationBySerial);

        byte[] result = invoke(METHOD_EXPORT_CERTIFICATE, certificateIdentification.getBEREncodedByteArray());
        try {
            return parseExportedCertificate(serialNumber, issuer, result);
        } catch (CertificateException e) {
            throw new ProtocolException(e, "Error while parsing exported certificate with serial number '" + serialNumber + "' and issuer '" + issuer + "': " + e.getMessage());
        }
    }

    private X509Certificate parseExportedCertificate(String serialNumber, String issuer, byte[] result) throws IOException, CertificateException {
        OctetString encodedCertificate = new OctetString(result, 0);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(encodedCertificate.getOctetStr());
        return (X509Certificate) certFactory.generateCertificate(in);
    }

    public X509Certificate exportCertificate(int certificateEntity, int certificateType, byte[] systemTitle) throws IOException {
        Structure certificateIdentification = new Structure();
        certificateIdentification.addDataType(new TypeEnum(0));     //(0) certificate_identification_entity
        Structure certificateIdentificationByEntity = new Structure();
        certificateIdentificationByEntity.addDataType(new TypeEnum(certificateEntity));
        certificateIdentificationByEntity.addDataType(new TypeEnum(certificateType));
        certificateIdentificationByEntity.addDataType(OctetString.fromByteArray(systemTitle));
        certificateIdentification.addDataType(certificateIdentificationByEntity);

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
     *                                                                                                                                                                                                                                                                         	(0)    nothing,
     *                                                                                                                                                                                                                                                                         	(1)    all messages to be authenticated,
     *                                                                                                                                                                                                                                                                         	(2)    all messages to be encrypted,
     *                                                                                                                                                                                                                                                                         	(3)    all messages to be authenticated and encrypted.
     *                                                                                                                                                                                                                                                                         </pre>
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
