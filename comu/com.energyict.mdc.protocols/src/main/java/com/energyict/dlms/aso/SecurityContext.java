package com.energyict.dlms.aso;

import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.encryption.AesGcm128;
import com.energyict.encryption.BitVector;
import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.dlms.protocolimplv2.SecurityProvider;


import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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

    public static final int CIPHERING_TYPE_GLOBAL = 0;
    public static final int CIPHERING_TYPE_DEDICATED = 1;

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

    private long frameCounter;
    private Integer responseFrameCounter = null;
    private byte[] systemTitle;
    private byte[] responseSystemTitle;

    private AuthenticationTypes authenticationAlgorithm;

    private static int DLMS_AUTH_TAG_SIZE = 12;    // 12 bytes is specified for DLMS using GCM

    /**
     * Indicates whether the FrameCounter needs to be validated with a +1
     */
    private boolean frameCounterInitialized = false;

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
     * @param cipheringType                  - the cipheringType to use (global [0] or dedicated [1])
     */
    public SecurityContext(int dataTransportSecurityLevel,
                           int associationAuthenticationLevel,
                           int dataTransportEncryptionType, byte[] systemIdentifier,
                           SecurityProvider securityProvider, int cipheringType) {
        this.securityPolicy = dataTransportSecurityLevel;
        this.authenticationLevel = associationAuthenticationLevel;
        this.securitySuite = dataTransportEncryptionType;
        this.securityProvider = securityProvider;
        this.cipheringType = cipheringType;
        this.authenticationAlgorithm = AuthenticationTypes.getTypeFor(this.authenticationLevel);
        this.frameCounter = securityProvider.getInitialFrameCounter();
        this.systemTitle = systemIdentifier != null ? systemIdentifier.clone() : null;
        this.responseFrameCounter = null;
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
     * @throws IOException when the desired Encryption algorithm isn't supported
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
     *         applied)
     */
    public byte[] dataTransportEncryption(byte[] plainText) throws UnsupportedException {
        int offset = 0;
        try {
            switch (this.securityPolicy) {
                case SECURITYPOLICY_NONE: {
                    return plainText;
                } // no encryption/authentication
                case SECURITYPOLICY_AUTHENTICATION: {
                    AesGcm128 ag128 = new AesGcm128(isGlobalCiphering() ? getSecurityProvider().getGlobalKey() : getSecurityProvider().getDedicatedKey(), DLMS_AUTH_TAG_SIZE);

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

                    /*
                          * x for length, 1 for controlByte, 4 for frameCounter,
                          * length of plainText
                          * and 12 for the AuthenticationTag (normally this is
                          * 16byte, but the securitySpec said it had to be 12)
                          */
                    byte[] securedLength = DLMSUtils.getAXDRLengthEncoding(CB_LENGTH + FC_LENGTH + plainText.length + DLMS_AUTH_TAG_SIZE);
                    byte[] securedApdu = new byte[(int) (securedLength.length + DLMSUtils.getAXDRLength(securedLength, 0))];
                    System.arraycopy(securedLength, 0, securedApdu, offset, securedLength.length);
                    offset += securedLength.length;
                    securedApdu[offset] = getSecurityControlByte();
                    offset++;
                    System.arraycopy(getFrameCounterInBytes(), 0, securedApdu, offset, getFrameCounterInBytes().length);
                    offset += getFrameCounterInBytes().length;
                    System.arraycopy(plainText, 0, securedApdu, offset, plainText.length);
                    offset += plainText.length;
                    System.arraycopy(ProtocolUtils.getSubArray2(ag128.getTag().getValue(), 0, DLMS_AUTH_TAG_SIZE), 0, securedApdu, offset,
                            DLMS_AUTH_TAG_SIZE);
                    return securedApdu;
                } // authenticated
                case SECURITYPOLICY_ENCRYPTION: {
                    AesGcm128 ag128 = new AesGcm128(isGlobalCiphering() ? getSecurityProvider().getGlobalKey() : getSecurityProvider().getDedicatedKey(), DLMS_AUTH_TAG_SIZE);

                    ag128.setInitializationVector(new BitVector(getInitializationVector()));
                    ag128.setPlainText(new BitVector(plainText));
                    ag128.encrypt();

                    /*
                          * x for length, 1 for controlByte, 4 for frameCounter,
                          * length of cipherText
                          */
                    byte[] securedLength = DLMSUtils.getAXDRLengthEncoding(CB_LENGTH + FC_LENGTH + plainText.length);
                    byte[] securedApdu = new byte[(int) (securedLength.length + DLMSUtils.getAXDRLength(securedLength, 0))];
                    System.arraycopy(securedLength, 0, securedApdu, offset, securedLength.length);
                    offset += securedLength.length;
                    securedApdu[offset] = getSecurityControlByte();
                    offset++;
                    System.arraycopy(getFrameCounterInBytes(), 0, securedApdu, offset, getFrameCounterInBytes().length);
                    offset += getFrameCounterInBytes().length;
                    System.arraycopy(ag128.getCipherText().getValue(), 0, securedApdu, offset, ag128.getCipherText().getValue().length);
                    return securedApdu;
                } // encrypted
                case SECURITYPOLICY_BOTH: {
                    AesGcm128 ag128 = new AesGcm128(isGlobalCiphering() ? getSecurityProvider().getGlobalKey() : getSecurityProvider().getDedicatedKey(), DLMS_AUTH_TAG_SIZE);

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

                    /*
                          * x for length, 1 for controlByte, 4 for frameCounter,
                          * length of cipherText
                          * and 12 for the AuthenticationTag (normally this is
                          * 16byte, but the securitySpec said it had to be 12)
                          */
                    byte[] securedLength = DLMSUtils.getAXDRLengthEncoding(CB_LENGTH + FC_LENGTH + plainText.length + DLMS_AUTH_TAG_SIZE);
                    byte[] secApdu = new byte[(int) (securedLength.length + DLMSUtils.getAXDRLength(securedLength, 0))];
                    System.arraycopy(securedLength, 0, secApdu, offset, securedLength.length);
                    offset += securedLength.length;
                    secApdu[offset] = getSecurityControlByte();
                    offset++;
                    System.arraycopy(getFrameCounterInBytes(), 0, secApdu, offset, getFrameCounterInBytes().length);
                    offset += getFrameCounterInBytes().length;
                    System.arraycopy(ag128.getCipherText().getValue(), 0, secApdu, offset, ag128.getCipherText().getValue().length);
                    offset += ag128.getCipherText().getValue().length;
                    System.arraycopy(ProtocolUtils.getSubArray(ag128.getTag().getValue(), 0, DLMS_AUTH_TAG_SIZE - 1), 0, secApdu, offset, DLMS_AUTH_TAG_SIZE);
                    return secApdu;
                } // authenticated and encrypted
                default:
                    throw new UnsupportedException("Unknown securityPolicy: " + this.securityPolicy);
            }
        } finally {
            incFrameCounter();
        }
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

    /**
     * Decrypts the ciphered APDU.
     *
     * @param cipherFrame - the text to decrypt ...
     * @return the plainText
     * @throws IOException         when Keys could not be fetched
     * @throws ConnectionException when the decryption fails
     */
    public byte[] dataTransportDecryption(byte[] cipherFrame) throws UnsupportedException, ConnectionException, DLMSConnectionException {
        switch (this.securityPolicy) {
            case SECURITYPOLICY_NONE: {
                return cipherFrame;
            }
            case SECURITYPOLICY_AUTHENTICATION: {
                AesGcm128 ag128 = new AesGcm128(isGlobalCiphering() ? getSecurityProvider().getGlobalKey() : getSecurityProvider().getDedicatedKey(), DLMS_AUTH_TAG_SIZE);

                byte[] aTag = ProtocolUtils.getSubArray(cipherFrame, cipherFrame.length - DLMS_AUTH_TAG_SIZE);
                int lengthOffset = DLMSUtils.getAXDRLengthOffset(cipherFrame, LENGTH_INDEX);
                byte[] fc = ProtocolUtils.getSubArray2(cipherFrame, FRAMECOUNTER_INDEX + lengthOffset, FRAME_COUNTER_SIZE);
                setResponseFrameCounter(ProtocolUtils.getInt(fc));
                byte[] apdu = ProtocolUtils.getSubArray(cipherFrame, FRAMECOUNTER_INDEX + lengthOffset + FRAME_COUNTER_SIZE, cipherFrame.length - DLMS_AUTH_TAG_SIZE - 1);
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
                AesGcm128 ag128 = new AesGcm128(isGlobalCiphering() ? getSecurityProvider().getGlobalKey() : getSecurityProvider().getDedicatedKey(), DLMS_AUTH_TAG_SIZE);

                int lengthOffset = DLMSUtils.getAXDRLengthOffset(cipherFrame, LENGTH_INDEX);
                byte[] fc = ProtocolUtils.getSubArray2(cipherFrame, FRAMECOUNTER_INDEX + lengthOffset, FRAME_COUNTER_SIZE);
                setResponseFrameCounter(ProtocolUtils.getInt(fc));
                byte[] cipheredAPDU = ProtocolUtils.getSubArray(cipherFrame, FRAMECOUNTER_INDEX + lengthOffset + FRAME_COUNTER_SIZE);

                ag128.setInitializationVector(new BitVector(getRespondingInitializationVector()));
                ag128.setCipherText(new BitVector(cipheredAPDU));

                if (ag128.decrypt()) {
                    return ag128.getPlainText().getValue();
                } else {
                    throw new ConnectionException("Received an invalid cipher frame.");
                }
            }
            case SECURITYPOLICY_BOTH: {
                AesGcm128 ag128 = new AesGcm128(isGlobalCiphering() ? getSecurityProvider().getGlobalKey() : getSecurityProvider().getDedicatedKey(), DLMS_AUTH_TAG_SIZE);

                byte[] aTag = ProtocolUtils.getSubArray(cipherFrame, cipherFrame.length - DLMS_AUTH_TAG_SIZE);
                int lengthOffset = DLMSUtils.getAXDRLengthOffset(cipherFrame, LENGTH_INDEX);
                byte[] fc = ProtocolUtils.getSubArray2(cipherFrame, FRAMECOUNTER_INDEX + lengthOffset, FRAME_COUNTER_SIZE);
                setResponseFrameCounter(ProtocolUtils.getInt(fc));
                byte[] cipheredAPDU = ProtocolUtils.getSubArray(cipherFrame, FRAMECOUNTER_INDEX + lengthOffset + FRAME_COUNTER_SIZE, cipherFrame.length - DLMS_AUTH_TAG_SIZE - 1);
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
    protected byte[] getInitializationVector() {
        if (getSystemTitle() == null) {
            throw new IllegalArgumentException("The AssociationRequest did NOT have a client SystemTitle - Encryption can not be applied!");
        }
        byte[] fc = getFrameCounterInBytes();
        byte[] iv = ProtocolUtils.concatByteArrays(getSystemTitle(), fc);
        return iv;
    }

    /**
     * Getter for the responding InitializationVector
     *
     * @return a byteArray containing the IV of the server
     */
    private byte[] getRespondingInitializationVector() {
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
        return responseSystemTitle;
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
        this.frameCounter++;
    }

    /**
     * @return the responding frameCounter
     */
    public long getResponseFrameCounter() {
        return this.responseFrameCounter;
    }

    /**
     * @return the responding frameCounter as byte array
     */
    public byte[] getRespondingFrameCounterInBytes() {
        return calculateFrameCounterInBytes(getResponseFrameCounter());
    }

    /**
     * Setter for the responding FrameCounter
     *
     * @param frameCounter the frameCounter to set from the server
     * @throws com.energyict.dlms.DLMSConnectionException
     *          if the FrameCounter was not incremented in a proper way
     */
    public void setResponseFrameCounter(int frameCounter) throws DLMSConnectionException {
        this.responseFrameCounter = this.securityProvider.getRespondingFrameCounterHandler().checkRespondingFrameCounter(frameCounter);
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
     * Checks whether Global ciphering is used ({@link #cipheringType} equals {@link #CIPHERING_TYPE_GLOBAL})
     *
     * @return true if it is, false otherwise
     */
    public boolean isGlobalCiphering() {
        return this.cipheringType == CIPHERING_TYPE_GLOBAL;
    }

    /**
     * Checks whether Dedicated ciphering is used ({@link #cipheringType} equals {@link #CIPHERING_TYPE_DEDICATED})
     *
     * @return true if it is, false otherwise
     */
    public boolean isDedicatedCiphering() {
        return this.cipheringType == CIPHERING_TYPE_DEDICATED;
    }

    public boolean isFrameCounterInitialized() {
        return frameCounterInitialized;
    }

    public void setFrameCounterInitialized(boolean frameCounterInitialized) {
        this.frameCounterInitialized = frameCounterInitialized;
    }
}
