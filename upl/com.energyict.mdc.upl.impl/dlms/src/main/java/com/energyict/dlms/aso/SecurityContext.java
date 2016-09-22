package com.energyict.dlms.aso;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.*;
import com.energyict.dlms.protocolimplv2.GeneralCipheringSecurityProvider;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.encryption.AesGcm128;
import com.energyict.encryption.BitVector;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.exceptions.CodingException;
import com.energyict.protocol.exceptions.DataEncryptionException;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocol.support.FrameCounterCache;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public static final int SECURITYPOLICY_NONE = 0;
    public static final int SECURITYPOLICY_AUTHENTICATION = 1;
    public static final int SECURITYPOLICY_ENCRYPTION = 2;
    public static final int SECURITYPOLICY_BOTH = 3;

    public static final int INITIALIZATION_VECTOR_SIZE = 12;
    public static final int FRAME_COUNTER_SIZE = 4;
    public static final int LENGTH_INDEX = 1;
    public static final int FRAMECOUNTER_INDEX = 2;
    public static final int FRAMECOUNTER_BYTE_LENGTH = 4;
    public static final int BITS_PER_BYTE = 8;
    public static final int SYSTEM_TITLE_LENGTH = 8;
    public static final int CB_LENGTH = 1;
    public static final int FC_LENGTH = 4;
    private static final String GENERAL_CIPHERING_KEY_TYPE = "GeneralCipheringKeyType";
    private static int DLMS_AUTH_TAG_SIZE = 12;    // 12 bytes is specified for DLMS using GCM
    /**
     * Holds the securityLevel for the DataTransport.
     */
    private final int securityPolicy;
    /**
     * Points to the encryption Method that has to be used for dataTransport.
     * Currently only 0 (meaning AES-GCM-128) is allowed
     */
    private final int securitySuite;
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
    private long frameCounter;
    private Integer responseFrameCounter = null;
    private byte[] systemTitle;
    private byte[] responseSystemTitle;
    private AuthenticationTypes authenticationAlgorithm;
    /**
     * Indicates whether the FrameCounter needs to be validated with a +1
     */
    private boolean frameCounterInitialized = false;

    /**
     * Used to signal change in frame counter for caching. clientId is used to match the interface
     */
    private FrameCounterCache deviceCache;
    private int clientId;

    /**
     * This state allows us to include the general ciphering key information just once, for the first request.
     * From then on, the used session key is fixed, so there's no need to include the key information again in the next requests.
     */
    private boolean includeGeneralCipheringKeyInformation = true;


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
        this.securityPolicy = dataTransportSecurityLevel;
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
     * Get the security level for dataTransport
     * <pre>
     * - 0 : Security not imposed
     * - 1 : All messages(APDU's) must be authenticated
     * - 2 : All messages(APDU's) must be encrypted
     * - 3 : All messages(APDU's) must be authenticated AND encrypted
     * </pre>
     *
     * @return the securityPolicy
     */
    public int getSecurityPolicy() {
        return securityPolicy;
    }

    /**
     * Get the type of encryption used for dataTransport
     *
     * @return the securitySuite
     */
    public int getSecuritySuite() {
        return securitySuite;
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
            switch (this.securityPolicy) {
                case SECURITYPOLICY_NONE: {
                    return plainText;
                } // no encryption/authentication
                case SECURITYPOLICY_AUTHENTICATION: {
                    AesGcm128 ag128 = new AesGcm128(getEncryptionKey(), DLMS_AUTH_TAG_SIZE);

                    /*
                          * the associatedData is a concatenation of:
                          * - the securityControlByte
                          * - the authenticationKey
                          * - the plainText
                          */
                    byte[] associatedData = new byte[plainText.length + getSecurityProvider().getAuthenticationKey().length + 1];
                    associatedData[0] = getSecurityControlByte();
                    System.arraycopy(getSecurityProvider().getAuthenticationKey(), 0, associatedData, 1, getSecurityProvider().getAuthenticationKey().length);
                    System.arraycopy(plainText, 0, associatedData, 1 + getSecurityProvider().getAuthenticationKey().length, plainText.length);

                    ag128.setAdditionalAuthenticationData(new BitVector(associatedData));
                    ag128.setInitializationVector(new BitVector(getInitializationVector()));
                    ag128.encrypt();
                    return createSecuredApdu(plainText, ag128.getTag().getValue());
                } // authenticated
                case SECURITYPOLICY_ENCRYPTION: {
                    AesGcm128 ag128 = new AesGcm128(getEncryptionKey(), DLMS_AUTH_TAG_SIZE);

                    ag128.setInitializationVector(new BitVector(getInitializationVector()));
                    ag128.setPlainText(new BitVector(plainText));
                    ag128.encrypt();
                    return createSecuredApdu(ag128.getCipherText().getValue(), null);
                } // encrypted
                case SECURITYPOLICY_BOTH: {
                    AesGcm128 ag128 = new AesGcm128(getEncryptionKey(), DLMS_AUTH_TAG_SIZE);

                    /*
                          * the associatedData is a concatenation of:
                          * - the securityControlByte
                          * - the authenticationKey
                          */
                    byte[] associatedData = new byte[getSecurityProvider().getAuthenticationKey().length + 1];
                    associatedData[0] = getSecurityControlByte();
                    System.arraycopy(getSecurityProvider().getAuthenticationKey(), 0, associatedData, 1, getSecurityProvider().getAuthenticationKey().length);
                    ag128.setAdditionalAuthenticationData(new BitVector(associatedData));
                    ag128.setInitializationVector(new BitVector(getInitializationVector()));
                    ag128.setPlainText(new BitVector(plainText));
                    ag128.encrypt();
                    return createSecuredApdu(ag128.getCipherText().getValue(), ag128.getTag().getValue());
                } // authenticated and encrypted
                default:
                    throw new UnsupportedException("Unknown securityPolicy: " + this.securityPolicy);
            }
        } finally {
            if (incrementFrameCounter) {
                incFrameCounter();
            }
        }
    }

    private byte[] getEncryptionKey() {
        return getEncryptionKey(this.generalCipheringKeyType);
    }

    /**
     * The block cipher key used to encrypt/decrypt an APDU.
     * The key to be used depends on the context. (ciphering type, and in case of general ciphering, the {@link GeneralCipheringKeyType}).
     *
     * @param generalCipheringKeyType - in case of general ciphering, this indicates the type of encryption key that should be used.
     *                                If no key type is given, use the one that is configured in EIServer.
     */
    private byte[] getEncryptionKey(GeneralCipheringKeyType generalCipheringKeyType) {
        if (this.cipheringType == CipheringType.GENERAL_CIPHERING.getType()) {
            switch (generalCipheringKeyType) {
                case IDENTIFIED_KEY:
                    return getSecurityProvider().getGlobalKey();
                case WRAPPED_KEY:
                    return getGeneralCipheringSecurityProvider().getSessionKey();
                case AGREED_KEY:
                    throw new IllegalStateException("not yet implemented");
                    //TODO implement
                default:
                    throw DeviceConfigurationException.missingProperty(GENERAL_CIPHERING_KEY_TYPE);
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
     * Constructs a general-ciphered xDLMS APDU.
     * Structure: transaction-id, client system title, server system title, date-time, other info, key-info, ciphered APDU
     *
     * @param plainText - the text to encrypt
     * @return the cipherText
     */
    public byte[] dataTransportGeneralEncryption(byte[] plainText) throws IOException {

        switch (this.generalCipheringKeyType) {
            case IDENTIFIED_KEY: {
                return ProtocolTools.concatByteArrays(
                        createGeneralCipheringHeader(true),
                        new byte[]{(byte) generalCipheringKeyType.getId()}, //key-id
                        new byte[]{(byte) GeneralCipheringKeyType.IdentifiedKeyTypes.GLOBAL_UNICAST_ENCRYPTION_KEY.getId()},
                        dataTransportEncryption(plainText)
                );
            }

            case WRAPPED_KEY: {
                if (includeGeneralCipheringKeyInformation) {
                    byte[] sessionKey = getGeneralCipheringSecurityProvider().getSessionKey();

                    //This is a newly generated session key, so reset the frame counters
                    resetFrameCounters();

                    byte[] masterKey = getSecurityProvider().getMasterKey();
                    byte[] wrappedKey = ProtocolTools.aesWrap(sessionKey, masterKey);

                    //Only include the wrapped key information the first request
                    includeGeneralCipheringKeyInformation = false;

                    return ProtocolTools.concatByteArrays(
                            createGeneralCipheringHeader(true),
                            new byte[]{(byte) generalCipheringKeyType.getId()}, //key-id
                            new byte[]{(byte) GeneralCipheringKeyType.WrappedKeyTypes.MASTER_KEY.getId()},
                            new byte[]{(byte) wrappedKey.length},
                            wrappedKey,
                            dataTransportEncryption(plainText)
                    );
                } else {
                    //Do not include the wrapped key information any more for the next requests
                    return ProtocolTools.concatByteArrays(
                            createGeneralCipheringHeader(false),
                            dataTransportEncryption(plainText)
                    );
                }
            }

            case AGREED_KEY: {
                //TODO implement
                throw new IllegalStateException("General ciphering with agreed key is not yet implemented");
            }

            default:
                throw DeviceConfigurationException.missingProperty(GENERAL_CIPHERING_KEY_TYPE);
        }
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
    private byte[] createGeneralCipheringHeader(boolean includeKeyInfo) {

        //TODO replace epoch by transaction-id and treat it as invokeid??
        int transactionIdLength = 8;
        byte[] transactionIdBytes = ProtocolTools.getBytesFromLong(System.currentTimeMillis(), transactionIdLength);

        return ProtocolTools.concatByteArrays(
                new byte[]{(byte) transactionIdLength},
                transactionIdBytes,
                new byte[]{(byte) getSystemTitle().length},
                getSystemTitle(),
                new byte[]{(byte) getResponseSystemTitle().length},
                getResponseSystemTitle(),
                new byte[]{(byte) 0x00},    //No datetime
                new byte[]{(byte) 0x00},    //No other-info
                new byte[]{includeKeyInfo ? (byte) 0x01 : 0x00}    //key-info is optional
        );
    }

    /**
     * Decrypts a received general-ciphered xDLMS APDU.
     * Structure: transaction-id, client system title, server system title, date-time, other info, key-info, ciphered APDU
     */
    public byte[] dataTransportGeneralDecryption(byte[] generalCipheringAPDU) throws ConnectionException, DLMSConnectionException, ProtocolException {
        int ptr = 0;
        ptr = parseGeneralCipheringHeader(generalCipheringAPDU, ptr);
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

                    byte[] sessionKey = ProtocolTools.aesUnwrap(wrappedKey, getSecurityProvider().getMasterKey());

                    if (!Arrays.equals(getGeneralCipheringSecurityProvider().getSessionKey(), sessionKey)) {
                        getGeneralCipheringSecurityProvider().setSessionKey(sessionKey);

                        //We're using a new session key (the one received from the server) from now on,
                        //so make sure to specify it once again in the next general ciphering request
                        includeGeneralCipheringKeyInformation = true;

                        //New key in use, so start using a new frame counter
                        resetFrameCounters();
                    }
                }
                break;


                case AGREED_KEY:
                    throw new IllegalStateException("General ciphering with agreed key is not yet implemented");
                    //TODO implement
                default:
                    throw DeviceConfigurationException.missingProperty(GENERAL_CIPHERING_KEY_TYPE);
            }
        }

        // First byte is reserved for the tag, here we just insert a dummy byte
        // Decryption will start from position 1
        byte[] fullCipherFrame = ProtocolTools.concatByteArrays(new byte[]{(byte) 0x00}, ProtocolTools.getSubArray(generalCipheringAPDU, ptr));

        //Decrypt the frame using the key type that we received from the meter, it can be different from the configured key type in EIServer
        return dataTransportDecryption(fullCipherFrame, serverKeyType);
    }

    private void resetFrameCounters() {
        setFrameCounter(1);
        getSecurityProvider().getRespondingFrameCounterHandler().resetRespondingFrameCounter(0);
        responseFrameCounter = 0;
    }

    private int parseGeneralCipheringHeader(byte[] generalCipheringAPDU, int ptr) throws ConnectionException {
        if (generalCipheringAPDU[ptr] != DLMSCOSEMGlobals.GENERAL_CIPHERING) {
            throw new ConnectionException("Invalid General Ciphering-Tag :" + generalCipheringAPDU[ptr]);
        }
        ptr++;

        int transactionIdLength = generalCipheringAPDU[ptr++] & 0xFF;
        byte[] transactionId = ProtocolTools.getSubArray(generalCipheringAPDU, ptr, ptr + transactionIdLength);
        ptr += transactionIdLength;

        int serverSystemTitleLength = generalCipheringAPDU[ptr++] & 0xFF;
        byte[] serverSystemTitle = ProtocolTools.getSubArray(generalCipheringAPDU, ptr, ptr + serverSystemTitleLength);
        ptr += serverSystemTitleLength;
        if (!Arrays.equals(serverSystemTitle, getResponseSystemTitle())) {
            throw DataEncryptionException.dataEncryptionException(new ProtocolException("The system-title of the response doesn't correspond to the system-title used during association establishment"));
        }

        int clientSystemTitleLength = generalCipheringAPDU[ptr++] & 0xFF;
        byte[] clientSystemTitle = ProtocolTools.getSubArray(generalCipheringAPDU, ptr, ptr + clientSystemTitleLength);
        ptr += clientSystemTitleLength;
        if (!Arrays.equals(clientSystemTitle, getSystemTitle())) {
            throw DataEncryptionException.dataEncryptionException(new ProtocolException("The system-title of the client doesn't correspond to the system-title used during association establishment"));
        }

        int dateTimeLength = generalCipheringAPDU[ptr++] & 0xFF;
        byte[] dateTime = ProtocolTools.getSubArray(generalCipheringAPDU, ptr, ptr + dateTimeLength);
        ptr += dateTimeLength;

        int otherInfoLength = generalCipheringAPDU[ptr++] & 0xFF;
        byte[] otherInfo = ProtocolTools.getSubArray(generalCipheringAPDU, ptr, ptr + otherInfoLength);
        ptr += otherInfoLength;

        return ptr;
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
        securedApdu[offset] = getSecurityControlByte();
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
     * @param plainText to encrypt using GMAC
     * @return the secured APDU
     */
    public byte[] highLevelAuthenticationGMAC(byte[] plainText) {
        int offset = 0;
        List<byte[]> plainArray = new ArrayList<byte[]>();
        plainArray.add(new byte[]{getHLS5SecurityControlByte()});
        plainArray.add(getSecurityProvider().getAuthenticationKey());
        plainArray.add(plainText);
        byte[] associatedData = DLMSUtils.concatListOfByteArrays(plainArray);

        AesGcm128 ag128 = new AesGcm128(getSecurityProvider().getGlobalKey(), DLMS_AUTH_TAG_SIZE);
        ag128.setAdditionalAuthenticationData(new BitVector(associatedData));
        ag128.setInitializationVector(new BitVector(getInitializationVector()));

        ag128.encrypt();

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
        System.arraycopy(ProtocolUtils.getSubArray2(ag128.getTag().getValue(), 0, DLMS_AUTH_TAG_SIZE), 0, securedApdu, offset,
                DLMS_AUTH_TAG_SIZE);
        return securedApdu;
    }

    /**
     * Create the encrypted packet with the StoC challenge and the framecounter of the meter.
     * This way you can check if the meter has calculated the same one and both systems are then authenticated
     *
     * @param clientChallenge our challenge we originally send to the meter
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

        AesGcm128 ag128 = new AesGcm128(getSecurityProvider().getGlobalKey(), DLMS_AUTH_TAG_SIZE);
        ag128.setAdditionalAuthenticationData(new BitVector(associatedData));
        ag128.setInitializationVector(new BitVector(ProtocolUtils.concatByteArrays(getResponseSystemTitle(), fc)));

        ag128.encrypt();

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
        System.arraycopy(ProtocolUtils.getSubArray2(ag128.getTag().getValue(), 0, DLMS_AUTH_TAG_SIZE), 0, securedApdu, offset,
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

    public byte[] dataTransportDecryption(byte[] cipherFrame) throws UnsupportedException, ConnectionException, DLMSConnectionException {
        return dataTransportDecryption(cipherFrame, this.generalCipheringKeyType);
    }

    /**
     * Decrypts the ciphered APDU.
     *
     * @param cipherFrame             - the text to decrypt ...
     * @param generalCipheringKeyType - in case of general ciphering, this indicates the type of encryption key that should be used.
     *                                The server can respond with a different key type, so we should take that into account here
     * @return the plainText
     * @throws ConnectionException when the decryption fails
     */
    public byte[] dataTransportDecryption(byte[] cipherFrame, GeneralCipheringKeyType generalCipheringKeyType) throws UnsupportedException, ConnectionException, DLMSConnectionException {
        switch (this.securityPolicy) {
            case SECURITYPOLICY_NONE: {
                return cipherFrame;
            }
            case SECURITYPOLICY_AUTHENTICATION: {
                AesGcm128 ag128 = new AesGcm128(getEncryptionKey(generalCipheringKeyType), DLMS_AUTH_TAG_SIZE);

                byte[] aTag = getAuthenticationTag(cipherFrame);
                byte[] apdu = getApdu(cipherFrame, true);
                /* the associatedData is a concatenation of:
                 * - the securityControlByte
                 * - the authenticationKey
                 * - the plainText */
                byte[] associatedData = new byte[apdu.length + getSecurityProvider().getAuthenticationKey().length + 1];
                associatedData[0] = getSecurityControlByte();
                System.arraycopy(getSecurityProvider().getAuthenticationKey(), 0, associatedData, 1, getSecurityProvider().getAuthenticationKey().length);
                System.arraycopy(apdu, 0, associatedData, 1 + getSecurityProvider().getAuthenticationKey().length, apdu.length);


                ag128.setAdditionalAuthenticationData(new BitVector(associatedData));
                ag128.setInitializationVector(new BitVector(getRespondingInitializationVector()));
                ag128.setTag(new BitVector(aTag));

                if (ag128.decrypt()) {
                    return apdu;
                } else {
                    throw new ConnectionException("Received an invalid cipher frame.");
                }
            }
            case SECURITYPOLICY_ENCRYPTION: {
                AesGcm128 ag128 = new AesGcm128(getEncryptionKey(generalCipheringKeyType), DLMS_AUTH_TAG_SIZE);

                byte[] cipheredAPDU = getApdu(cipherFrame, false);
                ag128.setInitializationVector(new BitVector(getRespondingInitializationVector()));
                ag128.setCipherText(new BitVector(cipheredAPDU));

                if (ag128.decrypt()) {
                    return ag128.getPlainText().getValue();
                } else {
                    throw new ConnectionException("Received an invalid cipher frame.");
                }
            }
            case SECURITYPOLICY_BOTH: {
                AesGcm128 ag128 = new AesGcm128(getEncryptionKey(generalCipheringKeyType), DLMS_AUTH_TAG_SIZE);

                byte[] aTag = getAuthenticationTag(cipherFrame);
                byte[] cipheredAPDU = getApdu(cipherFrame, true);
                /* the associatedData is a concatenation of:
                 * - the securityControlByte
                 * - the authenticationKey */
                byte[] associatedData = new byte[getSecurityProvider().getAuthenticationKey().length + 1];
                associatedData[0] = getSecurityControlByte();
                System.arraycopy(getSecurityProvider().getAuthenticationKey(), 0, associatedData, 1, getSecurityProvider().getAuthenticationKey().length);

                ag128.setAdditionalAuthenticationData(new BitVector(associatedData));
                ag128.setInitializationVector(new BitVector(getRespondingInitializationVector()));
                ag128.setTag(new BitVector(aTag));
                ag128.setCipherText(new BitVector(cipheredAPDU));

                if (ag128.decrypt()) {
                    return ag128.getPlainText().getValue();
                } else {
                    throw new ConnectionException("Received an invalid cipher frame.");
                }
            }
            default:
                throw new UnsupportedException("Unknown securityPolicy: "
                        + this.securityPolicy);
        }
    }

    /**
     * Decrypts the ciphered general-global or general-dedicated APDU
     *
     * @param cipherFrame - the text to decrypt
     * @return the plainText
     */
    public byte[] dataTransportGeneralGloOrDedDecryption(byte[] cipherFrame) throws UnsupportedException, DLMSConnectionException, ConnectionException {
        int ptr = 0;
        if (cipherFrame[ptr] != DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING && cipherFrame[ptr] != DLMSCOSEMGlobals.GENERAL_DEDICATED_CIPTHERING) {
            throw new ConnectionException("Invalid General-Global Ciphering-Tag :" + cipherFrame[ptr]);
        }
        ptr++;
        int systemTitleLength = cipherFrame[ptr++];
        byte[] systemTitleBytes = ProtocolTools.getSubArray(cipherFrame, ptr, ptr + systemTitleLength);
        ptr += systemTitleLength;
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
     * every encrypted/authenticated message.
     * <pre>
     * Bit 3-0: Security_Suite_Id;
     * Bit 4: 'A' subfield: indicate that the APDU is authenticated;
     * Bit 5: 'E' subfield: indicates that the APDU is encrypted;
     * Bit 6: Key_set subfield 0 = Unicast; 1 = Broadcast,
     * Bit 7: Reserved, must be set to 0.
     * </pre>
     *
     * @return the constructed SecurityControlByte
     */
    public byte getSecurityControlByte() {
        byte scByte = 0;
        scByte |= (this.securitySuite & 0x0F); // add the securitySuite to bits 0 to 3
        scByte |= (this.securityPolicy << 4); // set the encryption/authentication
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
            throw new IllegalArgumentException("The AssociationRequest did NOT have a client SystemTitle - Encryption can not be applied!");
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
        if (getResponseSystemTitle() == null) {
            throw new IllegalArgumentException("The AssociationResponse did NOT have a server SystemTitle - Encryption can not be applied!");
        }
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
        return responseSystemTitle == null ? null : Arrays.copyOf(responseSystemTitle, SYSTEM_TITLE_LENGTH);
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
        if (this.deviceCache!=null){
            deviceCache.setTXFrameCounter(clientId, (int)frameCounter);
        }
    }

    /**
     * Add 1 to the existing frameCounter
     */
    public void incFrameCounter() {
        setFrameCounter(this.frameCounter+1);
    }

    /**
     * Decrements the existing frameCounter
     */
    public void decrementFrameCounter() {
        setFrameCounter(this.frameCounter-1);
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
    public void setResponseFrameCounter(int frameCounter) throws DLMSConnectionException {
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

    public boolean isFrameCounterInitialized() {
        return frameCounterInitialized;
    }

    public void setFrameCounterInitialized(boolean frameCounterInitialized) {
        this.frameCounterInitialized = frameCounterInitialized;
    }

    public void setFrameCounterCache(int clientId, FrameCounterCache deviceCache){
        this.deviceCache = deviceCache;
        this.clientId = clientId;
    }
}
