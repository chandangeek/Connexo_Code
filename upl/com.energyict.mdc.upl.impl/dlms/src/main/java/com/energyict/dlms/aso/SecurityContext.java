package com.energyict.dlms.aso;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.*;
import com.energyict.dlms.cosem.attributeobjects.dataprotection.ProtectionType;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.GeneralCipheringSecurityProvider;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.encryption.AesGcm;
import com.energyict.encryption.AlgorithmID;
import com.energyict.encryption.BitVector;
import com.energyict.encryption.asymetric.keyagreement.KeyAgreement;
import com.energyict.encryption.asymetric.keyagreement.KeyAgreementImpl;
import com.energyict.encryption.asymetric.signature.ECDSASignatureImpl;
import com.energyict.encryption.asymetric.util.KeyUtils;
import com.energyict.encryption.kdf.KDF;
import com.energyict.encryption.kdf.NIST_SP_800_56_KDF;
import com.energyict.mdw.core.ECCCurve;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.exceptions.CodingException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.DataEncryptionException;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The securityContext manages the different securityLevels for establishing
 * associations and dataTransport
 *
 * @author gna
 */
public class SecurityContext {

    public static final int FRAME_COUNTER_SIZE = 4;
    public static final int LENGTH_INDEX = 1;
    public static final int FRAMECOUNTER_INDEX = 2;
    public static final int FRAMECOUNTER_BYTE_LENGTH = 4;
    public static final int BITS_PER_BYTE = 8;
    public static final int SYSTEM_TITLE_LENGTH = 8;
    public static final int CB_LENGTH = 1;
    public static final int FC_LENGTH = 4;
    private static final int TRANSACTION_ID_LENGTH = 8;
    private static int DLMS_AUTH_TAG_SIZE = 12;    // 12 bytes is specified for DLMS using GCM
    /**
     * Holds the securityLevel for the DataTransport.
     */
    private final SecurityPolicy securityPolicy;
    /**
     * Holds the securityLevel for the Authentication mechanism used during
     * Association Establishment
     */
    private final int authenticationLevel;
    /**
     * The provider containing all the keys that may be used during an
     * Authenticated/Encrypted communication
     */
    private final SecurityProvider securityProvider;
    /**
     * Indicating whether global[0] or dedicated[1] ciphering is used
     */
    private final int cipheringType;
    private final GeneralCipheringKeyType generalCipheringKeyType;
    /**
     * Points to the encryption Method that has to be used for dataTransport.
     * Currently 3 suites defined in the DLMS blue book:
     * - 0 (AES-GCM-128)
     * - 1 (ECDH-ECDSAAES-GCM-128-SHA-256)
     * - 2 (ECDH-ECDSAAES-GCM-256-SHA-384)
     */
    private int securitySuite;
    private long frameCounter;
    private Long responseFrameCounter = null;
    private byte[] systemTitle;
    private byte[] responseSystemTitle;
    private AuthenticationTypes authenticationAlgorithm;
    private boolean lastResponseWasSigned = false;

    /**
     * This state allows us to include the general ciphering key information just once, for the first request.
     * From then on, the used session key is fixed, so there's no need to include the key information again in the next requests.
     */
    private boolean includeGeneralCipheringKeyInformation = true;
    private byte[] transactionId;


    public SecurityContext(int dataTransportSecurityLevel,
                           int associationAuthenticationLevel,
                           int dataTransportEncryptionType, byte[] systemIdentifier,
                           SecurityProvider securityProvider, int cipheringType) {
        this(dataTransportSecurityLevel, associationAuthenticationLevel, dataTransportEncryptionType, systemIdentifier, securityProvider, cipheringType, null);
    }

    /**
     * Creates a new instance of the securityContext.
     * Note: the frameCounter can't always start from zero for security reasons. The FC is used in the
     * initializationVector and this one should be unique.
     *
     * @param dataTransportSecurityLevel     - SecurityLevel during data transport
     * @param associationAuthenticationLevel - SecurityLevel during associationEstablishment
     * @param dataTransportEncryptionType    - Which type of security to use during data transport
     * @param systemIdentifier               - the server his logicalDeviceName, used for the construction of the initializationVector (ex. KAMM1436321499)
     * @param securityProvider               - The securityProvider holding the keys
     * @param cipheringType                  - the cipheringType to use, see {@link CipheringType}
     * @param generalCipheringKeyType        - The key type to be used in case of general ciphering, see {@link GeneralCipheringKeyType}. This can be null if no general ciphering is used.
     */
    public SecurityContext(int dataTransportSecurityLevel,
                           int associationAuthenticationLevel,
                           int dataTransportEncryptionType, byte[] systemIdentifier,
                           SecurityProvider securityProvider, int cipheringType, GeneralCipheringKeyType generalCipheringKeyType) {
        this.securityPolicy = new SecurityPolicy(dataTransportEncryptionType, dataTransportSecurityLevel);
        this.authenticationLevel = associationAuthenticationLevel;
        this.securitySuite = dataTransportEncryptionType;
        this.securityProvider = securityProvider;
        this.cipheringType = cipheringType;
        this.authenticationAlgorithm = AuthenticationTypes.getTypeFor(this.authenticationLevel);
        setFrameCounter(securityProvider.getInitialFrameCounter());
        this.systemTitle = systemIdentifier != null ? systemIdentifier.clone() : null;
        this.responseFrameCounter = null;
        this.generalCipheringKeyType = generalCipheringKeyType;
    }

    /**
     * Creates a new instance of the securityContext.
     * Note: the frameCounter can't always start from zero for security reasons. The FC is used in the
     * initializationVector and this one should be unique.
     *
     * @param dataTransportSecurityLevel     - SecurityLevel during data transport
     * @param associationAuthenticationLevel - SecurityLevel during associationEstablishment
     * @param dataTransportEncryptionType    - Which type of security to use during data transport
     * @param securityProvider               - The securityProvider holding the keys
     * @param cipheringType                  - the cipheringType to use (global [0] or dedicated [1])
     */
    public SecurityContext(int dataTransportSecurityLevel,
                           int associationAuthenticationLevel, int dataTransportEncryptionType,
                           SecurityProvider securityProvider, int cipheringType) {
        this(dataTransportSecurityLevel, associationAuthenticationLevel, dataTransportEncryptionType, null, securityProvider, cipheringType);
    }

    /**
     * Get the security level for dataTransport.
     * This indicates if a frame (be it a request or a response is authenticated and/or encrypted.
     */
    public SecurityPolicy getSecurityPolicy() {
        return securityPolicy;
    }

    /**
     * Get the authentication level used during the Association Establishment
     *
     * @return the authenticationLevel
     */
    public int getAuthenticationLevel() {
        return authenticationLevel;
    }

    /**
     * Get the used {@link com.energyict.dlms.aso.AuthenticationTypes}
     *
     * @return the AuthenticationType
     */
    public AuthenticationTypes getAuthenticationType() {
        return this.authenticationAlgorithm;
    }

    /**
     * Get the securityKeyProvider
     *
     * @return the securityProvider
     */
    public SecurityProvider getSecurityProvider() {
        return securityProvider;
    }

    /**
     * @param plainText - the text to encrypt ...
     * @return the cihperText
     * @throws NoSuchAlgorithmException when the desired Encryption algorithm isn't supported
     */
    public byte[] associationEncryption(byte[] plainText) throws NoSuchAlgorithmException {
        byte[] digest;
        MessageDigest md = MessageDigest.getInstance(this.authenticationAlgorithm.getAlgorithm());
        md.reset();
        digest = md.digest(plainText);
        return digest;
    }

    /**
     * <pre>
     * Constructs a ciphered xDLMS APDU. The globalCiphering-PDU-Tag is NOT included.
     * The returned byteArray will contain the:
     * 	- Length
     * 	- SecurityHeader
     * 	- ciphered APDU
     * 	- (Tag)
     * </pre>
     *
     * @param plainText - the text to encrypt ...
     * @return the cipherText (or the plainText when no security has to be
     * applied)
     */
    public byte[] dataTransportEncryption(byte[] plainText) throws UnsupportedException {
        return dataTransportEncryption(plainText, true);
    }

    /**
     * <pre>
     * Constructs a ciphered xDLMS APDU. The globalCiphering-PDU-Tag is NOT included.
     * The returned byteArray will contain the:
     * 	- Length
     * 	- SecurityHeader
     * 	- ciphered APDU
     * 	- (Tag)
     * </pre>
     *
     * @param plainText             - the text to encrypt ...
     * @param incrementFrameCounter - increment frame counter flag
     * @return the cipherText (or the plainText when no security has to be
     * applied)
     */
    public byte[] dataTransportEncryption(byte[] plainText, boolean incrementFrameCounter) throws UnsupportedException {
        try {
            if (securityPolicy.isRequestPlain()) {
                return plainText;
            } else if (securityPolicy.isRequestAuthenticatedOnly()) {
                return getAuthenticatedRequestBytes(plainText);
            } else if (securityPolicy.isRequestEncryptedOnly()) {
                return getEncryptedRequestBytes(plainText);
            } else if (securityPolicy.isRequestAuthenticatedAndEncrypted()) {
                return getAuthenticatedAndEncryptedRequestBytes(plainText);
            } else {
                throw new UnsupportedException("Unknown securityPolicy: " + this.securityPolicy.getDataTransportSecurityLevel());
            }
        } finally {
            if (incrementFrameCounter) {
                incFrameCounter();
            }
        }
    }

    private byte[] getAuthenticatedAndEncryptedRequestBytes(byte[] plainText) {
        AesGcm aesGcm = new AesGcm(getEncryptionKey(), DLMS_AUTH_TAG_SIZE);

                /*
                 * The additional associatedData (AAD) is a concatenation of:
                 * - the securityControlByte
                 * - the authenticationKey
                 * - (the general ciphering header)
                 */
        byte[] associatedData = ProtocolTools.concatByteArrays(
                new byte[]{getRequestSecurityControlByte()},
                getSecurityProvider().getAuthenticationKey(),
                (cipheringType == CipheringType.GENERAL_CIPHERING.getType()) ? createGeneralCipheringHeader() : new byte[0]
        );

        aesGcm.setAdditionalAuthenticationData(new BitVector(associatedData));
        aesGcm.setInitializationVector(new BitVector(getInitializationVector()));
        aesGcm.setPlainText(new BitVector(plainText));
        aesGcm.encrypt();
        return createSecuredApdu(aesGcm.getCipherText().getValue(), aesGcm.getTag().getValue());
    }

    private byte[] getEncryptedRequestBytes(byte[] plainText) {
        AesGcm aesGcm = new AesGcm(getEncryptionKey(), DLMS_AUTH_TAG_SIZE);

        aesGcm.setInitializationVector(new BitVector(getInitializationVector()));
        aesGcm.setPlainText(new BitVector(plainText));
        aesGcm.encrypt();
        return createSecuredApdu(aesGcm.getCipherText().getValue(), null);
    }

    private byte[] getAuthenticatedRequestBytes(byte[] plainText) {
        AesGcm aesGcm = new AesGcm(getEncryptionKey(), DLMS_AUTH_TAG_SIZE);

                /*
                 * The additional associatedData (AAD) is a concatenation of:
                 * - the securityControlByte
                 * - the authenticationKey
                 * - (the general ciphering header)
                 * - the plainText
                 */
        byte[] associatedData = ProtocolTools.concatByteArrays(
                new byte[]{getRequestSecurityControlByte()},
                getSecurityProvider().getAuthenticationKey(),
                (cipheringType == CipheringType.GENERAL_CIPHERING.getType()) ? createGeneralCipheringHeader() : new byte[0],
                plainText
        );

        aesGcm.setAdditionalAuthenticationData(new BitVector(associatedData));
        aesGcm.setInitializationVector(new BitVector(getInitializationVector()));
        aesGcm.encrypt();
        return createSecuredApdu(plainText, aesGcm.getTag().getValue());
    }

    private byte[] getEncryptionKey() {
        return getEncryptionKey(this.generalCipheringKeyType, false);
    }

    /**
     * The block cipher key used to encrypt/decrypt an APDU.
     * The key to be used depends on the context. (ciphering type, and in case of general ciphering, the {@link GeneralCipheringKeyType}).
     *
     * @param generalCipheringKeyType - in case of general ciphering, this indicates the type of encryption key that should be used.
     *                                If no key type is given, use the one that is configured in EIServer.
     * @param serverSessionKey        true: return the server session key (used to decrypt frames received from the server)
     *                                false: return our (client) session key (used to encrypt frames to send to the server)
     */
    private byte[] getEncryptionKey(GeneralCipheringKeyType generalCipheringKeyType, boolean serverSessionKey) {
        if (this.cipheringType == CipheringType.GENERAL_CIPHERING.getType()) {
            switch (generalCipheringKeyType) {
                case IDENTIFIED_KEY:
                    return getSecurityProvider().getGlobalKey();
                case WRAPPED_KEY:
                    //The wrapped key is stored in the session key field of the security provider
                    if (serverSessionKey) {
                        return getGeneralCipheringSecurityProvider().getServerSessionKey();
                    } else {
                        return getGeneralCipheringSecurityProvider().getSessionKey();
                    }
                case AGREED_KEY:
                    //The agreed key is stored in the session key field of the security provider
                    if (serverSessionKey) {
                        return getGeneralCipheringSecurityProvider().getServerSessionKey();
                    } else {
                        return getGeneralCipheringSecurityProvider().getSessionKey();
                    }
                default:
                    throw DeviceConfigurationException.missingProperty(DlmsSessionProperties.GENERAL_CIPHERING_KEY_TYPE);
            }
        } else {
            return isGlobalCiphering() ? getSecurityProvider().getGlobalKey() : getSecurityProvider().getDedicatedKey();
        }
    }

    /**
     * Constructs a ciphered general-global/general-dedicated xDLMS APDU. The general-globalCiphering-PDU-Tag is NOT included.
     * The returned byteArray will contain the:
     * - Length of system-title
     * - System-title
     * - ciphered APDU
     *
     * @param plainText - the text to encrypt
    * @return the cipherText
    */
    public byte[] dataTransportGeneralGloOrDedEncryption(byte[] plainText) throws IOException {
        ByteArrayOutputStream securedRequestStream = new ByteArrayOutputStream();
        securedRequestStream.write(getSystemTitle().length);
        securedRequestStream.write(getSystemTitle());
        securedRequestStream.write(dataTransportEncryption(plainText));
        return securedRequestStream.toByteArray();
    }

    /**
     * Wrap the given APDU (can already by secured) in a new general-signing APDU.
     * The signature is either 64 bytes or 96 bytes based on the suite that is used.
     */
    public byte[] applyGeneralSigning(byte[] securedRequest) throws UnsupportedException {
        return applyGeneralSigning(securedRequest, null, new byte[]{(byte) 0x00}, new byte[]{(byte) 0x00});
    }

    /**
     * Wrap the given APDU (can already by secured) in a new general-signing APDU.
     * The signature is either 64 bytes or 96 bytes based on the suite that is used.
     */
    public byte[] applyGeneralSigning(byte[] securedRequest, ECCCurve eccCurve, byte[] dateTime, byte[] otherInfo) throws UnsupportedException {
        ECDSASignatureImpl ecdsaSignature;
        if (eccCurve == null) {
            ecdsaSignature = new ECDSASignatureImpl(getECCCurve());
        } else {
            ecdsaSignature = new ECDSASignatureImpl(eccCurve);
        }
        byte[] generalCipheringHeader = createGeneralCipheringHeader();
        PrivateKey clientPrivateSigningKey = getGeneralCipheringSecurityProvider().getClientPrivateSigningKey();

        byte[] signature = ecdsaSignature.sign(ProtocolTools.concatByteArrays(generalCipheringHeader, securedRequest), clientPrivateSigningKey);

        return ProtocolTools.concatByteArrays(
                generalCipheringHeader,
                DLMSUtils.getAXDRLengthEncoding(securedRequest.length),
                securedRequest,
                DLMSUtils.getAXDRLengthEncoding(signature.length),
                signature
        );
    }

    /**
     * Unwrap the given general-signing APDU, return its contents.
     * Note that these contents can still be a ciphered APDU.
     */
    public byte[] unwrapGeneralSigning(byte[] generalSigningAPDU) throws UnsupportedException, ConnectionException {
        int ptr = 0;
        byte[] generalSigningHeader = parseGeneralHeader(generalSigningAPDU, DLMSCOSEMGlobals.GENERAL_SIGNING);
        ptr++;  //Skip tag
        ptr += generalSigningHeader.length;

        int contentLength = DLMSUtils.getAXDRLength(generalSigningAPDU, ptr);
        ptr += DLMSUtils.getAXDRLengthOffset(generalSigningAPDU, ptr);
        byte[] content = ProtocolTools.getSubArray(generalSigningAPDU, ptr, ptr + contentLength);
        ptr += contentLength;

        int signatureLength = DLMSUtils.getAXDRLength(generalSigningAPDU, ptr);
        ptr += DLMSUtils.getAXDRLengthOffset(generalSigningAPDU, ptr);
        byte[] signature = ProtocolTools.getSubArray(generalSigningAPDU, ptr, ptr + signatureLength);
        ptr += signatureLength;

        ECDSASignatureImpl ecdsaSignature = new ECDSASignatureImpl(getECCCurve());
        byte[] input = ProtocolTools.concatByteArrays(generalSigningHeader, content);
        PublicKey publicKey = getGeneralCipheringSecurityProvider().getServerSignatureCertificate().getPublicKey();
        boolean verify = ecdsaSignature.verify(input, signature, publicKey);
        if (!verify) {
            throw ConnectionCommunicationException.signatureVerificationError();
        }
        lastResponseWasSigned = true;
        return content;
    }

    /**
     * Constructs a general-ciphered xDLMS APDU.
     * Structure: transaction-id, client system title, server system title, date-time, other info, key-info, ciphered APDU
     *
     * @param plainText - the text to encrypt
     * @return the cipherText
     */
    public byte[] dataTransportGeneralEncryption(byte[] plainText) throws IOException {

            //Reset it, so our next request will have a newly generated transactionId.
            //It is used in the header of the general-ciphering APDU and the calculation of the authentication tag.
            resetTransactionId();

            switch (this.generalCipheringKeyType) {
                case IDENTIFIED_KEY: {
                    return ProtocolTools.concatByteArrays(
                            createGeneralCipheringHeader(),
                            new byte[]{(byte) 0x01},    //Yes, key info is present
                            new byte[]{(byte) generalCipheringKeyType.getId()}, //key-id
                            new byte[]{(byte) GeneralCipheringKeyType.IdentifiedKeyTypes.GLOBAL_UNICAST_ENCRYPTION_KEY.getId()},
                            dataTransportEncryption(plainText)
                    );
                }

                case WRAPPED_KEY: {
                    if (includeGeneralCipheringKeyInformation) {

                        byte[] wrappedKey = getWrappedKey(true);

                        //Only include the wrapped key information the first request
                        includeGeneralCipheringKeyInformation = false;

                        return ProtocolTools.concatByteArrays(
                                createGeneralCipheringHeader(),
                                new byte[]{(byte) 0x01},    //Yes, key info is present
                                new byte[]{(byte) generalCipheringKeyType.getId()}, //key-id
                                new byte[]{(byte) GeneralCipheringKeyType.WrappedKeyTypes.MASTER_KEY.getId()},
                                new byte[]{(byte) wrappedKey.length},
                                wrappedKey,
                                dataTransportEncryption(plainText)
                        );
                    } else {
                        //Do not include the wrapped key information any more for the next requests
                        return ProtocolTools.concatByteArrays(
                                createGeneralCipheringHeader(),
                                new byte[]{(byte) 0x00},    //Key info is not present
                                dataTransportEncryption(plainText)
                        );
                    }
                }

                case AGREED_KEY: {
                    if (includeGeneralCipheringKeyInformation) {

                        //One-Pass Diffie-Hellman C(1e, 1s, ECC CDH):
                        //We are party U (sender), the meter is party V (receiver).
                        //This means we generate an ephemeral keypair and use its private key combined with
                        //the public static key agreement key of the server to derive a shared secret.
                        //The server side will do the same, using its static key agreement private key and our ephemeral public key.

                        KeyAgreement keyAgreement = new KeyAgreementImpl(getECCCurve());
                        Certificate serverKeyAgreementCertificate = getGeneralCipheringSecurityProvider().getServerKeyAgreementCertificate();
                        if (serverKeyAgreementCertificate == null) {
                            throw DeviceConfigurationException.missingProperty(SecurityPropertySpecName.SERVER_KEY_AGREEMENT_CERTIFICATE.toString());
                        }

                        byte[] sharedSecretZ = keyAgreement.generateSecret(serverKeyAgreementCertificate.getPublicKey());
                        byte[] partyUInfo = getSystemTitle();           //Party U is the sender, us, the client
                        byte[] partyVInfo = getResponseSystemTitle();   //Party V is the receiver, the server, the meter
                        byte[] sessionKey = NIST_SP_800_56_KDF.getInstance().derive(getKeyDerivingHashFunction(), sharedSecretZ, getKeyDerivingEncryptionAlgorithm(), partyUInfo, partyVInfo);
                        getGeneralCipheringSecurityProvider().setSessionKey(sessionKey);

                        PublicKey ephemeralPublicKey = keyAgreement.getEphemeralPublicKey();
                        byte[] ephemeralPublicKeyBytes = KeyUtils.toRawData(getECCCurve(), ephemeralPublicKey);

                        ECDSASignatureImpl ecdsaSignature = new ECDSASignatureImpl(getECCCurve());
                        PrivateKey clientPrivateSigningKey = getGeneralCipheringSecurityProvider().getClientPrivateSigningKey();

                        byte[] signature = ecdsaSignature.sign(ephemeralPublicKeyBytes, clientPrivateSigningKey);

                        //This is a newly generated session key, so reset our frame counter
                        setFrameCounter(1);

                        //Only include the key information the first request
                        includeGeneralCipheringKeyInformation = false;

                        return ProtocolTools.concatByteArrays(
                                createGeneralCipheringHeader(),
                                new byte[]{(byte) 0x01},    //Yes, key info is present
                                new byte[]{(byte) generalCipheringKeyType.getId()}, //key-id
                                new byte[]{(byte) 0x01},    //Length of the AgreedKeyTypes byte is 1
                                new byte[]{(byte) GeneralCipheringKeyType.AgreedKeyTypes.ECC_CDH_1E1S.getId()},
                                DLMSUtils.getAXDRLengthEncoding(ephemeralPublicKeyBytes.length + signature.length),
                                ephemeralPublicKeyBytes,
                                signature,
                                dataTransportEncryption(plainText)
                        );
                    } else {
                        //Do not include the key information any more for the next requests
                        return ProtocolTools.concatByteArrays(
                                createGeneralCipheringHeader(),
                                new byte[]{(byte) 0x00},    //Key info is not present
                                dataTransportEncryption(plainText)
                        );
                    }
                }

                default:
                    throw DeviceConfigurationException.missingProperty(DlmsSessionProperties.GENERAL_CIPHERING_KEY_TYPE);
            }
    }

    public byte[] getWrappedKey(boolean resetFC) {
        byte[] sessionKey = getGeneralCipheringSecurityProvider().getSessionKey();

        if(resetFC){
            //This is a newly generated session key, so reset our frame counter
            setFrameCounter(1);
        }

        byte[] masterKey = getSecurityProvider().getMasterKey();
        return ProtocolTools.aesWrap(sessionKey, masterKey);
    }

    private GeneralCipheringSecurityProvider getGeneralCipheringSecurityProvider() {
        if (!(getSecurityProvider() instanceof GeneralCipheringSecurityProvider)) {
            throw CodingException.protocolImplementationError("General ciphering is not yet supported in the protocol you are using");
        }
        return ((GeneralCipheringSecurityProvider) getSecurityProvider());
    }

    /**
     * Structure: transaction-id, client system title, server system title, date-time, other info, key-info
     */
    private byte[] createGeneralCipheringHeader(byte[] dateTime, byte[] otherInfo) {
        //TODO replace epoch by transaction-id and treat it as invokeid??
        return ProtocolTools.concatByteArrays(
                new byte[]{(byte) TRANSACTION_ID_LENGTH},
                getTransactionId(),
                new byte[]{(byte) getSystemTitle().length},
                getSystemTitle(),
                new byte[]{(byte) getResponseSystemTitle().length},
                getResponseSystemTitle(),
                dateTime,
                otherInfo
        );
    }

    /**
     * Structure: transaction-id, client system title, server system title, date-time, other info, key-info
     */
    private byte[] createGeneralCipheringHeader() {
        return createGeneralCipheringHeader(new byte[]{(byte) 0x00}, new byte[]{(byte) 0x00});
    }


    public byte[] getTransactionId() {
        if (transactionId == null) {
            transactionId = ProtocolTools.getBytesFromLong(System.currentTimeMillis(), TRANSACTION_ID_LENGTH);
        }
        return transactionId;
    }

    private void resetTransactionId() {
        this.transactionId = null;
    }

    public void setTransactionId(byte[] transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Decrypts a received general-ciphered xDLMS APDU.
     * Structure: transaction-id, client system title, server system title, date-time, other info, key-info, ciphered APDU
     */
    public byte[] dataTransportGeneralDecryption(byte[] generalCipheringAPDU) throws ConnectionException, DLMSConnectionException, ProtocolException {
        int ptr = 0;
        byte[] generalCipheringHeader = parseGeneralHeader(generalCipheringAPDU, DLMSCOSEMGlobals.GENERAL_CIPHERING);
        ptr++;  //Skip tag
        ptr += generalCipheringHeader.length;

        GeneralCipheringKeyType serverKeyType = this.generalCipheringKeyType;
        boolean keyInfoIsPresent = (generalCipheringAPDU[ptr++] & 0xFF) != 0;    //0x01: key-info field is present. 0x00: key-info field is omitted.
        if (keyInfoIsPresent) {

            int keyTypeId = generalCipheringAPDU[ptr++] & 0xFF;
            serverKeyType = GeneralCipheringKeyType.fromId(keyTypeId);
            if (serverKeyType == null) {
                throw new ProtocolException("Received an unsupported key type '" + keyTypeId + "' from the meter. Should be 0 (identified-key), 1 (wrapped-key), or 2 (agreed-key)");
            }

            switch (serverKeyType) {
                case IDENTIFIED_KEY: {
                    int keyId = generalCipheringAPDU[ptr++] & 0xFF;
                    if (keyId != GeneralCipheringKeyType.IdentifiedKeyTypes.GLOBAL_UNICAST_ENCRYPTION_KEY.getId()) {
                        throw new ProtocolException("The general ciphering implementation only supports the global unicast encryption key (0) as identified key type. Received type '" + keyId + "' from meter is not supported");
                    }
                }
                break;

                case WRAPPED_KEY: {
                    int kekId = generalCipheringAPDU[ptr++] & 0xFF;
                    if (kekId != GeneralCipheringKeyType.WrappedKeyTypes.MASTER_KEY.getId()) {
                        throw new ProtocolException("The general ciphering implementation only supports master key (0) as wrap key type. Received type '" + kekId + "' from meter is not supported");
                    }

                    int wrappedKeyLength = generalCipheringAPDU[ptr++] & 0xFF;
                    byte[] wrappedKey = ProtocolTools.getSubArray(generalCipheringAPDU, ptr, ptr + wrappedKeyLength);
                    ptr += wrappedKeyLength;

                    byte[] serverSessionKey = ProtocolTools.aesUnwrap(wrappedKey, getSecurityProvider().getMasterKey());
                    handleServerSessionKey(serverSessionKey);
                }
                break;

                case AGREED_KEY: {
                    int keyParametersLength = generalCipheringAPDU[ptr++] & 0xFF;
                    int keyParameters = generalCipheringAPDU[ptr++] & 0xFF;

                    if (GeneralCipheringKeyType.AgreedKeyTypes.ECC_CDH_1E1S.getId() != keyParameters) {
                        throw new UnsupportedException("Unsupported key agreement type: '" + keyParameters + "'. Only type 1 (1e, 1s, ECC CDH) is currently supported");
                    }

                    int keyCipheredDataLength = DLMSUtils.getAXDRLength(generalCipheringAPDU, ptr);
                    ptr += DLMSUtils.getAXDRLengthOffset(keyCipheredDataLength);

                    byte[] keyCipheredData = ProtocolTools.getSubArray(generalCipheringAPDU, ptr, ptr + keyCipheredDataLength);
                    ptr += keyCipheredDataLength;

                    int keySize = KeyUtils.getKeySize(getECCCurve());
                    byte[] serverEphemeralPublicKeyBytes = ProtocolTools.getSubArray(keyCipheredData, 0, keySize);
                    PublicKey serverEphemeralPublicKey = KeyUtils.toECPublicKey(getECCCurve(), serverEphemeralPublicKeyBytes);
                    byte[] signature = ProtocolTools.getSubArray(keyCipheredData, keySize, keyCipheredDataLength);

                    ECDSASignatureImpl ecdsaSignature = new ECDSASignatureImpl(getECCCurve());
                    X509Certificate serverSignatureCertificate = getGeneralCipheringSecurityProvider().getServerSignatureCertificate();
                    if (serverSignatureCertificate == null) {
                        throw DeviceConfigurationException.missingProperty(SecurityPropertySpecName.SERVER_SIGNING_CERTIFICATE.toString());
                    }

                    if (!ecdsaSignature.verify(serverEphemeralPublicKeyBytes, signature, serverSignatureCertificate.getPublicKey())) {
                        throw ConnectionCommunicationException.signatureVerificationError();
                    }

                    PrivateKey clientPrivateKeyAgreementKey = getGeneralCipheringSecurityProvider().getClientPrivateKeyAgreementKey();
                    KeyPair keyAgreementKeyPair = new KeyPair(null, clientPrivateKeyAgreementKey);
                    KeyAgreement keyAgreement = new KeyAgreementImpl(getECCCurve(), keyAgreementKeyPair);

                    byte[] secretZ = keyAgreement.generateSecret(serverEphemeralPublicKey);
                    byte[] partyUInfo = getResponseSystemTitle();   //Party U is the sender, the server, the meter
                    byte[] partyVInfo = getSystemTitle();           //Party V is the receiver, us, the client
                    byte[] serverSessionKey = NIST_SP_800_56_KDF.getInstance().derive(getKeyDerivingHashFunction(), secretZ, getKeyDerivingEncryptionAlgorithm(), partyUInfo, partyVInfo);

                    handleServerSessionKey(serverSessionKey);
                }
                break;

                default:
                    throw DeviceConfigurationException.missingProperty(DlmsSessionProperties.GENERAL_CIPHERING_KEY_TYPE);
            }
        }

        // First byte is reserved for the tag, here we just insert a dummy byte
        // Decryption will start from position 1
        byte[] fullCipherFrame = ProtocolTools.concatByteArrays(new byte[]{(byte) 0x00}, ProtocolTools.getSubArray(generalCipheringAPDU, ptr));

        //Decrypt the frame using the key type that we received from the meter, it can be different from the configured key type in EIServer
        return dataTransportDecryption(fullCipherFrame, serverKeyType, generalCipheringHeader);
    }

    public AlgorithmID getKeyDerivingEncryptionAlgorithm() {
        switch (securitySuite) {
            case 1:
                return AlgorithmID.AES_GCM_128;
            case 2:
                return AlgorithmID.AES_GCM_256;
            default:
                throw DeviceConfigurationException.unsupportedPropertyValue("SecuritySuite", String.valueOf(securitySuite));
        }
    }

    public KDF.HashFunction getKeyDerivingHashFunction() {
        switch (securitySuite) {
            case 1:
                return KDF.HashFunction.SHA256;
            case 2:
                return KDF.HashFunction.SHA384;
            default:
                throw DeviceConfigurationException.unsupportedPropertyValue("SecuritySuite", String.valueOf(securitySuite));
        }
    }

    public ECCCurve getECCCurve() {
        switch (securitySuite) {
            case 1:
                return ECCCurve.P256_SHA256;
            case 2:
                return ECCCurve.P384_SHA384;
            default:
                throw DeviceConfigurationException.unsupportedPropertyValue("SecuritySuite", String.valueOf(securitySuite));
        }
    }

    private void handleServerSessionKey(byte[] serverSessionKey) {
        if (getGeneralCipheringSecurityProvider().getServerSessionKey() == null
                || !Arrays.equals(getGeneralCipheringSecurityProvider().getServerSessionKey(), serverSessionKey)) {

            getGeneralCipheringSecurityProvider().setServerSessionKey(serverSessionKey);

            //New server session key in use, so start using a new responding frame counter
            getSecurityProvider().getRespondingFrameCounterHandler().setRespondingFrameCounter(0);
            responseFrameCounter = 0l;
        }
    }

    /**
     * Parse a general-ciphering or general-signing APDU header.
     * Structure: tag, transaction-id, client system title, server system title, date-time, other info, key-info
     * Returns the header, without the tag.
     */
    private byte[] parseGeneralHeader(byte[] generalAPDU, byte expectedTag) throws ConnectionException {
        int ptr = 0;
        if (generalAPDU[ptr] != expectedTag) {
            throw new ConnectionException("Invalid General Tag :" + generalAPDU[ptr] + ", expected tag: " + expectedTag);
        }
        ptr++;

        int transactionIdLength = generalAPDU[ptr++] & 0xFF;
        byte[] transactionId = ProtocolTools.getSubArray(generalAPDU, ptr, ptr + transactionIdLength);
        ptr += transactionIdLength;

        int serverSystemTitleLength = generalAPDU[ptr++] & 0xFF;
        byte[] serverSystemTitle = ProtocolTools.getSubArray(generalAPDU, ptr, ptr + serverSystemTitleLength);
        ptr += serverSystemTitleLength;

        //If we didn't receive the server system-title in the AARE (for example in case of an inbound event notification frame)
        if (responseSystemTitle == null) {
            setResponseSystemTitle(serverSystemTitle);
        }

        if (!Arrays.equals(serverSystemTitle, getResponseSystemTitle())) {
            throw DataEncryptionException.dataEncryptionException(new ProtocolException("The system-title of the response doesn't correspond to the system-title used during association establishment"));
        }

        int clientSystemTitleLength = generalAPDU[ptr++] & 0xFF;
        byte[] clientSystemTitle = ProtocolTools.getSubArray(generalAPDU, ptr, ptr + clientSystemTitleLength);
        ptr += clientSystemTitleLength;
        if (!Arrays.equals(clientSystemTitle, getSystemTitle())) {
            throw DataEncryptionException.dataEncryptionException(new ProtocolException("The system-title of the client doesn't correspond to the system-title used during association establishment"));
        }

        int dateTimeLength = generalAPDU[ptr++] & 0xFF;
        byte[] dateTime = ProtocolTools.getSubArray(generalAPDU, ptr, ptr + dateTimeLength);
        ptr += dateTimeLength;

        int otherInfoLength = generalAPDU[ptr++] & 0xFF;
        byte[] otherInfo = ProtocolTools.getSubArray(generalAPDU, ptr, ptr + otherInfoLength);
        ptr += otherInfoLength;
        return ProtocolTools.getSubArray(generalAPDU, 1, ptr); //The full header, without the tag
    }

    /**
     * Wrap a given frame (plain or encrypted) with the control byte, the frame counter and (in case of authentication policy) the authentication tag
     */
    public byte[] createSecuredApdu(byte[] frame, byte[] authTag) {
        int offset = 0;
        byte[] securedLength = DLMSUtils.getAXDRLengthEncoding(CB_LENGTH + FC_LENGTH + frame.length + (authTag == null ? 0 : DLMS_AUTH_TAG_SIZE));
        byte[] securedApdu = new byte[securedLength.length + DLMSUtils.getAXDRLength(securedLength, 0)];
        System.arraycopy(securedLength, 0, securedApdu, offset, securedLength.length);
        offset += securedLength.length;
        securedApdu[offset] = getRequestSecurityControlByte();
        offset++;
        System.arraycopy(getFrameCounterInBytes(), 0, securedApdu, offset, getFrameCounterInBytes().length);
        offset += getFrameCounterInBytes().length;
        System.arraycopy(frame, 0, securedApdu, offset, frame.length);
        if (authTag != null) {
            offset += frame.length;
            System.arraycopy(ProtocolUtils.getSubArray2(authTag, 0, DLMS_AUTH_TAG_SIZE), 0, securedApdu, offset, DLMS_AUTH_TAG_SIZE);
        }
        return securedApdu;
    }

    /**
     * Construct the encrypted packet to use as part of the HLS authentication 5 (encryption with GMAC).
     * The authenticationTag is constructed with the associatedData = SecurityControl byte || AuthenticationKey || StoC.
     * The secured packet is constructed as : SecurityControl byte || FrameCounter || (T)
     *
     * @param sToCChallenge to encrypt using GMAC
     * @return the secured APDU
     */
    public byte[] highLevelAuthenticationGMAC(byte[] sToCChallenge) {
        int offset = 0;
        List<byte[]> plainArray = new ArrayList<byte[]>();
        plainArray.add(new byte[]{getHLS5SecurityControlByte()});
        plainArray.add(getSecurityProvider().getAuthenticationKey());
        plainArray.add(sToCChallenge);
        byte[] associatedData = DLMSUtils.concatListOfByteArrays(plainArray);

        AesGcm aesGcm = new AesGcm(getSecurityProvider().getGlobalKey(), DLMS_AUTH_TAG_SIZE);
        aesGcm.setAdditionalAuthenticationData(new BitVector(associatedData));
        aesGcm.setInitializationVector(new BitVector(getInitializationVector()));

        aesGcm.encrypt();

        /*
        * 1 for SecurityControlByte, 4 for frameCounter,
        * 12 for the AuthenticationTag (normally this is
        * 16byte, but the securitySpec said it had to be 12)
        * -> this is a total of 17
        */
        byte[] securedApdu = new byte[1 + FRAMECOUNTER_BYTE_LENGTH + DLMS_AUTH_TAG_SIZE];
        securedApdu[offset++] = getHLS5SecurityControlByte();
        System.arraycopy(getFrameCounterInBytes(), 0, securedApdu, offset, FRAMECOUNTER_BYTE_LENGTH);
        offset += FRAMECOUNTER_BYTE_LENGTH;
        System.arraycopy(ProtocolUtils.getSubArray2(aesGcm.getTag().getValue(), 0, DLMS_AUTH_TAG_SIZE), 0, securedApdu, offset,
                DLMS_AUTH_TAG_SIZE);
        return securedApdu;
    }

    /**
     * Create the encrypted packet with the StoC challenge and the framecounter of the meter.
     * This way you can check if the meter has calculated the same one and both systems are then authenticated
     *
     * @param clientChallenge our challenge we originally send to the meter (CtoS)
     * @param cipheredFrame   the ciphered frame we received from the meter
     * @return the encrypted packet
     */
    public byte[] createHighLevelAuthenticationGMACResponse(byte[] clientChallenge, byte[] cipheredFrame) {
        byte[] fc = ProtocolUtils.getSubArray2(cipheredFrame, 1, FRAME_COUNTER_SIZE);
        int offset = 0;
        List<byte[]> plainArray = new ArrayList<byte[]>();
        plainArray.add(new byte[]{getHLS5SecurityControlByte()});
        plainArray.add(getSecurityProvider().getAuthenticationKey());
        plainArray.add(clientChallenge);
        byte[] associatedData = DLMSUtils.concatListOfByteArrays(plainArray);

        AesGcm aesGcm = new AesGcm(getSecurityProvider().getGlobalKey(), DLMS_AUTH_TAG_SIZE);
        aesGcm.setAdditionalAuthenticationData(new BitVector(associatedData));
        aesGcm.setInitializationVector(new BitVector(ProtocolUtils.concatByteArrays(getResponseSystemTitle(), fc)));

        aesGcm.encrypt();

        /*
        * 1 for SecurityControlByte, 4 for frameCounter,
        * 12 for the AuthenticationTag (normally this is
        * 16byte, but the securitySpec said it had to be 12)
        * -> this is a total of 17
        */
        byte[] securedApdu = new byte[1 + FRAMECOUNTER_BYTE_LENGTH + DLMS_AUTH_TAG_SIZE];
        securedApdu[offset++] = getHLS5SecurityControlByte();
        System.arraycopy(fc, 0, securedApdu, offset, FRAMECOUNTER_BYTE_LENGTH);
        offset += FRAMECOUNTER_BYTE_LENGTH;
        System.arraycopy(ProtocolUtils.getSubArray2(aesGcm.getTag().getValue(), 0, DLMS_AUTH_TAG_SIZE), 0, securedApdu, offset,
                DLMS_AUTH_TAG_SIZE);

        return securedApdu;
    }

    /**
     * The securityControlByte is a byte of the securityHeader that is sent the
     * authenticated message.
     * <pre>
     * Bit 3-0: Security_Suite_Id;
     * Bit 4: 'A' subfield: indicate that the APDU is authenticated; [should be set for HLS]
     * Bit 5: 'E' subfield: indicates that the APDU is encrypted;
     * Bit 6: Key_set subfield 0 = Unicast; 1 = Broadcast,
     * Bit 7: Reserved, must be set to 0.
     * </pre>
     *
     * @return the constructed SecurityControlByte
     */
    public byte getHLS5SecurityControlByte() {
        byte scByte = 0;
        scByte |= (this.securitySuite & 0x0F); // add the securitySuite to bits 0 to 3
        scByte |= (1 << 4); // set the encryption/authentication
        return scByte;
    }

    public int getSecuritySuite() {
        return securitySuite;
    }

    public void setSecuritySuite(int securitySuite) {
        this.securitySuite = securitySuite;
    }

    public byte[] dataTransportDecryption(byte[] cipherFrame) throws ProtocolException, ConnectionException, DLMSConnectionException {
        return dataTransportDecryption(cipherFrame, this.generalCipheringKeyType);
    }

    public byte[] dataTransportDecryption(byte[] cipherFrame, GeneralCipheringKeyType generalCipheringKeyType) throws ProtocolException, ConnectionException, DLMSConnectionException {
        return dataTransportDecryption(cipherFrame, generalCipheringKeyType, new byte[0]);
    }

    /**
     * Decrypts the ciphered APDU.
     *
     * @param cipherFrame             - the text to decrypt ...
     * @param generalCipheringKeyType - in case of general ciphering, this indicates the type of encryption key that should be used.
     *                                The server can respond with a different key type, so we should take that into account here
     * @param generalCipheringHeader  The header of the general ciphering frame (or an empty byte array if no general ciphering is used).
     *                                This header is to be used as additional associated data for the calculation of the authentication tag.
     * @return the plainText
     * @throws ConnectionException when the decryption fails
     */
    public byte[] dataTransportDecryption(byte[] cipherFrame, GeneralCipheringKeyType generalCipheringKeyType, byte[] generalCipheringHeader) throws ProtocolException, ConnectionException, DLMSConnectionException {

        int lengthOffset = DLMSUtils.getAXDRLengthOffset(cipherFrame, LENGTH_INDEX);
        int responseSecurityControlByte = (cipherFrame[LENGTH_INDEX + lengthOffset]) & 0xFF;

        //Check if the security of the response (indicated by its security control byte) is at least the same as the configured security level.
        boolean authenticatedResponse = ProtocolTools.isBitSet(responseSecurityControlByte, 4);
        boolean encryptedResponse = ProtocolTools.isBitSet(responseSecurityControlByte, 5);
        boolean shouldBeAuthenticated = getSecurityPolicy().isResponseAuthenticatedOnly() || getSecurityPolicy().isResponseAuthenticatedAndEncrypted();
        boolean shouldBeEncrypted = getSecurityPolicy().isResponseEncryptedOnly() || getSecurityPolicy().isResponseAuthenticatedAndEncrypted();
        if (!authenticatedResponse && shouldBeAuthenticated) {
            throw new ProtocolException("Received a response that has no authentication tag, while the configured security level states that all responses must be authenticated. Aborting.");
        }
        if (!encryptedResponse && shouldBeEncrypted) {
            throw new ProtocolException("Received an unencrypted response, while the configured security level states that all responses must be encrypted. Aborting.");
        }

        if (!authenticatedResponse && !encryptedResponse) {
            if (XdlmsApduTags.isGlobalCipheringTag(cipherFrame[0]) || XdlmsApduTags.isDedicatedCipheringTag(cipherFrame[0])) {
                //An unsecured global-ciphering or dedicated-ciphering APDU. Unwrap it by stripping of the header.
                cipherFrame = ProtocolTools.getSubArray(cipherFrame, 7);
            }
            return optionallyUnwrapSignedAPDU(cipherFrame);
        } else if (authenticatedResponse && !encryptedResponse) {

            AesGcm aesGcm = new AesGcm(getEncryptionKey(generalCipheringKeyType, true), DLMS_AUTH_TAG_SIZE);

            byte[] aTag = getAuthenticationTag(cipherFrame);
            byte[] apdu = getApdu(cipherFrame, true);

            /*
             * The additional associatedData (AAD) is a concatenation of:
             * - the securityControlByte
             * - the authenticationKey
             * - (the general ciphering header)
             * - the plainText
             */
            byte[] associatedData = ProtocolTools.concatByteArrays(
                    new byte[]{(byte) responseSecurityControlByte},
                    getSecurityProvider().getAuthenticationKey(),
                    (cipheringType == CipheringType.GENERAL_CIPHERING.getType()) ? generalCipheringHeader : new byte[0],
                    apdu
            );

            aesGcm.setAdditionalAuthenticationData(new BitVector(associatedData));
            aesGcm.setInitializationVector(new BitVector(getRespondingInitializationVector()));
            aesGcm.setTag(new BitVector(aTag));

            if (aesGcm.decrypt()) {
                return optionallyUnwrapSignedAPDU(apdu);
            } else {
                throw new ConnectionException("Received an invalid cipher frame.");
            }
        } else if (!authenticatedResponse && encryptedResponse) {

            AesGcm aesGcm = new AesGcm(getEncryptionKey(generalCipheringKeyType, true), DLMS_AUTH_TAG_SIZE);

            byte[] cipheredAPDU = getApdu(cipherFrame, false);
            aesGcm.setInitializationVector(new BitVector(getRespondingInitializationVector()));
            aesGcm.setCipherText(new BitVector(cipheredAPDU));

            if (aesGcm.decrypt()) {
                return optionallyUnwrapSignedAPDU(aesGcm.getPlainText().getValue());
            } else {
                throw new ConnectionException("Received an invalid cipher frame.");
            }
        } else if (authenticatedResponse && encryptedResponse) {

            AesGcm ag128 = new AesGcm(getEncryptionKey(generalCipheringKeyType, true), DLMS_AUTH_TAG_SIZE);

            byte[] aTag = getAuthenticationTag(cipherFrame);
            byte[] cipheredAPDU = getApdu(cipherFrame, true);

            /*
             * The additional associatedData (AAD) is a concatenation of:
             * - the securityControlByte
             * - the authenticationKey
             * - (the general ciphering header)
             */
            byte[] associatedData = ProtocolTools.concatByteArrays(
                    new byte[]{(byte) responseSecurityControlByte},
                    getSecurityProvider().getAuthenticationKey(),
                    (cipheringType == CipheringType.GENERAL_CIPHERING.getType()) ? generalCipheringHeader : new byte[0]
            );

            ag128.setAdditionalAuthenticationData(new BitVector(associatedData));
            ag128.setInitializationVector(new BitVector(getRespondingInitializationVector()));
            ag128.setTag(new BitVector(aTag));
            ag128.setCipherText(new BitVector(cipheredAPDU));

            if (ag128.decrypt()) {
                return optionallyUnwrapSignedAPDU(ag128.getPlainText().getValue());
            } else {
                throw new ConnectionException("Received an invalid cipher frame.");
            }
        } else {
            throw new UnsupportedException("Unknown securityPolicy: " + this.securityPolicy);
        }
    }

    /**
     * The decrypted APDU can still be a general-signing APDU.
     * If so, unwrap it here and return its contents.
     */
    private byte[] optionallyUnwrapSignedAPDU(byte[] possibleGeneralSignedAPDU) throws ConnectionException, ProtocolException {
        byte[] result = possibleGeneralSignedAPDU;
        if (possibleGeneralSignedAPDU[0] == DLMSCOSEMGlobals.GENERAL_SIGNING) {
            result = unwrapGeneralSigning(possibleGeneralSignedAPDU);
        }

        //Check if the response should have been signed, according to the configured security level for responses
        if (getSecurityPolicy().isResponseSigned()) {
            if (!lastResponseWasSigned) {
                throw new ProtocolException("Received an unsigned response, while the configured security level states all responses must be signed. Aborting.");
            }

            //Reset the state so we can check it again for the next response
            lastResponseWasSigned = false;
        }
        return result;
    }

    /**
     * Decrypts the ciphered general-global or general-dedicated APDU
     *
     * @param cipherFrame - the text to decrypt
     * @return the plainText
     */
    public byte[] dataTransportGeneralGloOrDedDecryption(byte[] cipherFrame) throws ProtocolException, DLMSConnectionException, ConnectionException {
        int ptr = 0;
        if (cipherFrame[ptr] != DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING && cipherFrame[ptr] != DLMSCOSEMGlobals.GENERAL_DEDICATED_CIPTHERING) {
            throw new ConnectionException("Invalid General-Global Ciphering-Tag :" + cipherFrame[ptr]);
        }
        ptr++;
        int systemTitleLength = cipherFrame[ptr++];
        byte[] systemTitleBytes = ProtocolTools.getSubArray(cipherFrame, ptr, ptr + systemTitleLength);
        ptr += systemTitleLength;

        //If we didn't receive the server system-title in the AARE (for example in case of an inbound event notification frame)
        if (responseSystemTitle == null) {
            setResponseSystemTitle(systemTitleBytes);
        }

        if (!Arrays.equals(systemTitleBytes, getResponseSystemTitle())) {
            throw DataEncryptionException.dataEncryptionException(new ProtocolException("The system-title of the response doesn't corresponds to the system-title used during association establishment"));
        }
        byte[] fullCipherFrame = ProtocolTools.concatByteArrays(new byte[]{(byte) 0x00}, ProtocolTools.getSubArray(cipherFrame, ptr));  // First byte is reserved for the tag, here we just insert a dummy byte
        return dataTransportDecryption(fullCipherFrame);                                                                                // Decryption will start from position 1
    }

    public byte[] getApdu(byte[] cipherFrame, boolean containsAuthTag) throws DLMSConnectionException {
        int lengthOffset = DLMSUtils.getAXDRLengthOffset(cipherFrame, LENGTH_INDEX);
        byte[] fc = ProtocolUtils.getSubArray2(cipherFrame, FRAMECOUNTER_INDEX + lengthOffset, FRAME_COUNTER_SIZE);
        setResponseFrameCounter(ProtocolUtils.getInt(fc));
        return ProtocolUtils.getSubArray(cipherFrame, FRAMECOUNTER_INDEX + lengthOffset + FRAME_COUNTER_SIZE, containsAuthTag ? (cipherFrame.length - DLMS_AUTH_TAG_SIZE - 1) : (cipherFrame.length - 1));
    }

    public byte[] getAuthenticationTag(byte[] cipherFrame) {
        return ProtocolUtils.getSubArray(cipherFrame, cipherFrame.length - DLMS_AUTH_TAG_SIZE);
    }

    /**
     * The securityControlByte is a byte of the securityHeader that is sent with
     * every encrypted/authenticated request.
     * <pre>
     * Bit 3-0: Security_Suite_Id;
     * Bit 4: 'A' subfield: indicate that the APDU is authenticated;
     * Bit 5: 'E' subfield: indicates that the APDU is encrypted;
     * Bit 6: Key_set subfield 0 = Unicast; 1 = Broadcast,
     * Bit 7: Reserved, must be set to 0.
     * </pre>
     *
     * @return the constructed SecurityControlByte for requests (not for responses)
     */
    public byte getRequestSecurityControlByte() {
        byte scByte = 0;
        scByte |= (this.securitySuite & 0x0F); // add the securitySuite to bits 0 to 3

        //Bit 4 indicates authentication of our requests
        scByte |= (((securityPolicy.isRequestAuthenticatedOnly() || securityPolicy.isRequestAuthenticatedAndEncrypted()) ? 1 : 0) << 4);

        //Bit 5 indicates encryption of our requests
        scByte |= (((securityPolicy.isRequestEncryptedOnly() || securityPolicy.isRequestAuthenticatedAndEncrypted()) ? 1 : 0) << 5);
        return scByte;
    }

    /**
     * NOTE: you should code your own SystemTitle to send to the server
     * <p/>
     * Generate the initializationVector, based on:
     * <p/>
     * <pre>
     * - the SysTitle, which is the ASCII representation of the first 3 chars of the logical device name, concatenated with the hex value of his trailing serialnumber
     * - the hex representation of the frameCounter
     * </pre>
     *
     * @return a byteArray containing the IV of the client
     */
    public byte[] getInitializationVector() {
        if (getSystemTitle() == null) {
            throw DataEncryptionException.dataEncryptionException(new ProtocolException("The AssociationRequest did NOT have a client SystemTitle - Encryption can not be applied!"));
        }
        byte[] fc = getFrameCounterInBytes();
        byte[] paddedSystemTitle = Arrays.copyOf(getSystemTitle(), SYSTEM_TITLE_LENGTH);
        byte[] iv = ProtocolUtils.concatByteArrays(paddedSystemTitle, fc);
        return iv;
    }

    /**
     * Getter for the responding InitializationVector
     *
     * @return a byteArray containing the IV of the server
     */
    public byte[] getRespondingInitializationVector() {
        byte[] fc = getRespondingFrameCounterInBytes();
        byte[] iv = ProtocolUtils.concatByteArrays(getResponseSystemTitle(), fc);
        return iv;
    }

    /**
     * @return the clients' SystemTitle
     */
    public byte[] getSystemTitle() {
        return systemTitle;
    }

    /**
     * @return the servers' SystemTitle
     */
    public byte[] getResponseSystemTitle() {
        if (responseSystemTitle == null) {
            throw DataEncryptionException.dataEncryptionException(new ProtocolException("The AssociationResponse did NOT have a server SystemTitle - Encryption can not be applied!"));
        }

        return Arrays.copyOf(responseSystemTitle, SYSTEM_TITLE_LENGTH);
    }

    /**
     * Setter for the servers' responding SystemTitle
     *
     * @param title the server his SystemTitle
     */
    public void setResponseSystemTitle(byte[] title) {
        if (title != null) {
            this.responseSystemTitle = new byte[SYSTEM_TITLE_LENGTH];
            int copyLength = title.length < SYSTEM_TITLE_LENGTH ? title.length : this.responseSystemTitle.length;
            System.arraycopy(title, 0, this.responseSystemTitle, 0, copyLength);
        } else {
            responseSystemTitle = null;
        }
    }

    /**
     * @return the frameCounter
     */
    public long getFrameCounter() {
        return this.frameCounter;
    }

    /**
     * Set the frameCounter with a new value
     *
     * @param frameCounter
     */
    public void setFrameCounter(long frameCounter) {
        this.frameCounter = frameCounter;
    }

    /**
     * Add 1 to the existing frameCounter
     */
    public void incFrameCounter() {
        setFrameCounter(this.frameCounter + 1);
    }

    /**
     * Decrements the existing frameCounter
     */
    public void decrementFrameCounter() {
        setFrameCounter(this.frameCounter - 1);
    }

    /**
     * @return the responding frameCounter
     */
    public long getResponseFrameCounter() {
        return this.responseFrameCounter;
    }

    /**
     * Setter for the responding FrameCounter
     *
     * @param frameCounter the frameCounter to set from the server
     * @throws com.energyict.dlms.DLMSConnectionException * @throws com.energyict.dlms.DLMSConnectionException if the FrameCounter was not incremented in a proper way
     */
    public void setResponseFrameCounter(long frameCounter) throws DLMSConnectionException {
        this.responseFrameCounter = this.securityProvider.getRespondingFrameCounterHandler().checkRespondingFrameCounter(frameCounter);
    }

    /**
     * @return the responding frameCounter as byte array
     */
    public byte[] getRespondingFrameCounterInBytes() {
        return calculateFrameCounterInBytes(getResponseFrameCounter());
    }

    /**
     * @return the frameCounter as byte array
     */
    public byte[] getFrameCounterInBytes() {
        return calculateFrameCounterInBytes(getFrameCounter());
    }

    /**
     * Convert the given frameCounter into a byteArray
     *
     * @param frameCounter the frameCounter to convert
     * @return the converted frameCounter
     */
    private byte[] calculateFrameCounterInBytes(long frameCounter) {
        byte[] fc = new byte[FRAME_COUNTER_SIZE];
        for (int i = 0; i < fc.length; i++) {
            fc[fc.length - 1 - i] = (byte) ((frameCounter >> (i * BITS_PER_BYTE)) & 0xff);
        }
        return fc;
    }

    /**
     * HelperMethod to check for the largest trailing number in the logical
     * device name
     * <p/>
     * <pre>
     * ex.
     * - ISKT372M40581297 -&gt; 40581297
     * - KAMM1436321499 -&gt; 1436321499
     * </pre>
     *
     * @param str is the String which contains the number
     * @return a string containing only a number
     */
    protected String getLargestIntFromString(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (ProtocolUtils.isInteger(str.substring(i))) {
                return str.substring(i);
            }
        }
        return "";
    }

    @Override
    public String toString() {
        final Object crlf = "\r\n";
        StringBuilder sb = new StringBuilder();
        sb.append("SecurityContext:").append(crlf);
        return sb.toString();
    }

    /**
     * Getter for the {@link #cipheringType}
     *
     * @return the cipheringType
     */
    public int getCipheringType() {
        return cipheringType;
    }

    /**
     * Checks whether Global ciphering is used<br/>
     * This could be either global or general-global ciphering.
     *
     * @return true if it is, false otherwise
     */
    public boolean isGlobalCiphering() {
        return this.cipheringType == CipheringType.GLOBAL.getType() || this.cipheringType == CipheringType.GENERAL_GLOBAL.getType();
    }

    /**
     * Checks whether Dedicated ciphering is used <br/>
     * This could be either dedicated or general-dedicated ciphering.
     *
     * @return true if it is, false otherwise
     */
    public boolean isDedicatedCiphering() {
        return this.cipheringType == CipheringType.DEDICATED.getType() || this.cipheringType == CipheringType.GENERAL_DEDICATED.getType();
    }

    /**
     * Used for encrypt DataProtection -> invoke_protected_method -> protected_method_invocation_parameters
     * @param plainText
     * @param protectionType
     * @return
     * @throws UnsupportedException
     */
    public byte[] encryptProtectedMethodInvocationParameters(byte[] plainText, ProtectionType protectionType) throws UnsupportedException {
        try {
            switch (protectionType) {
                case AUTHENTICATION_AND_ENCRYPTION:
                    return getAuthenticatedAndEncryptedRequestBytes(plainText);
                case AUTHENTICATION:
                    return getAuthenticatedRequestBytes(plainText);
                case DIGITAL_SIGNATURE:
                    return ParseUtils.concatArray(new byte[]{DLMSCOSEMGlobals.GENERAL_SIGNING}, applyGeneralSigning(plainText, ECCCurve.P256_SHA256, new byte[]{(byte) 0x01, (byte) 0x00}, new byte[]{(byte) 0x01, (byte) 0x00}));
                case ENCRYPTION:
                    return getEncryptedRequestBytes(plainText);
                case NO_PROTECTION:
                    return plainText;
            }
        } finally {
            //TODO: see if we should increase FC
//            incFrameCounter();
        }
        return new byte[]{};
    }

}