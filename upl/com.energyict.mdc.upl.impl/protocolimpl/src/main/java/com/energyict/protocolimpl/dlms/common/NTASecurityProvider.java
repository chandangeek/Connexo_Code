package com.energyict.protocolimpl.dlms.common;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.dlms.aso.framecounter.DefaultRespondingFrameCounterHandler;
import com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.UnsupportedException;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Properties;

/**
 * Default implementation of the securityProvider.
 * Provides all the securityKeys, just for LOCAL purpose
 * Functionality is implemented according to the NTA specification.
 * <p/>
 * The RespondingFrameCounterHandler is the default one which will not verify or check the received frameCounter.
 *
 * @author gna
 */
public class NTASecurityProvider implements SecurityProvider {

    /**
     * Property name of the new AuthenticationKey
     */
    public static final String NEW_DATATRANSPORT_AUTHENTICATION_KEY = "NewDataTransportAuthenticationKey";
    /**
     * Property name of the new Global encryption Key
     */
    public static final String NEW_DATATRANSPORT_ENCRYPTION_KEY = "NewDataTransportEncryptionKey";
    /**
     * Property name of the new HighLevel security Secret
     */
    public static final String NEW_HLS_SECRET = "NewHLSSecret";
    /**
     * Property name of the DataTransport EncryptionKey
     */
    public static final String DATATRANSPORT_ENCRYPTIONKEY = "DataTransportEncryptionKey";
    /**
     * Property name of the Master key, or KeyEncryptionKey
     */
    public static final String MASTERKEY = "MasterKey";
    /**
     * Property name of the DataTransport AuthenticationKey
     */
    public static final String DATATRANSPORT_AUTHENTICATIONKEY = "DataTransportAuthenticationKey";
    /**
     * Property name of the length of the client to server challenge
     */
    public static final String CLIENT_TO_SERVER_CHALLENGE_LENGTH = "ChallengeLength";
    protected int securityLevel = -1;
    protected byte[] cTOs;
    protected byte[] dedicatedKey;
    protected Properties properties;
    protected Long initialFrameCounter;
    private byte[] authenticationKey;
    private byte[] encryptionKey;
    private byte[] masterKey;
    private String hlsSecret;
    private int challengeLength;
    private RespondingFrameCounterHandler respondingFrameCounterHandler = new DefaultRespondingFrameCounterHandler();

    /**
     * Create a new instance of LocalSecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public NTASecurityProvider(Properties properties) {
        this.properties = properties;
    }

    protected int getSecurityLevel() {
        if (securityLevel == -1) {
            String sl = properties.getProperty("SecurityLevel", "0");
            if (sl.contains(":")) {
                this.securityLevel = Integer.parseInt(sl.substring(0, sl.indexOf(":")));
            } else {
                this.securityLevel = Integer.parseInt(sl);
            }
        }
        return securityLevel;
    }

    /**
     * Generate a random challenge of x (default 16) bytes long
     */
    protected void generateClientToServerChallenge() {
        this.generateClientToServerChallenge(getClientToServerChallengeLength());
    }

    protected void generateClientToServerChallenge(int length) {
        if (this.cTOs == null) {
            SecureRandom generator = new SecureRandom();
            this.cTOs = new byte[length];
            generator.nextBytes(this.cTOs);
        }
    }

    /**
     * Return the dataTransprot authenticationKey
     */
    public byte[] getAuthenticationKey() {
        if (this.authenticationKey == null) {
            this.authenticationKey = DLMSUtils.hexStringToByteArray(properties.getProperty(DATATRANSPORT_AUTHENTICATIONKEY, ""));
        }
        return this.authenticationKey;
    }

    protected void setAuthenticationKey(byte[] authenticationKey) {
        this.authenticationKey = authenticationKey;
    }

    public byte[] getCallingAuthenticationValue() throws UnsupportedException {

        switch (this.getSecurityLevel()) {
            case 0:
                return new byte[0];
            case 1: {
                return getHLSSecret();
            }
            case 2:
                throw new UnsupportedException("SecurityLevel 2 is not implemented.");
            case 3: {    // this is a ClientToServer challenge for MD5
                generateClientToServerChallenge();
                return this.cTOs;
            }
            case 4: {    // this is a ClientToServer challenge for SHA-1
                generateClientToServerChallenge();
                return this.cTOs;
            }
            case 5: {    // this is a ClientToServer challenge for GMAC
                generateClientToServerChallenge();
                return this.cTOs;
            }
            case 6: {    // this is a ClientToServer challenge for SHA-256
                generateClientToServerChallenge();
                return this.cTOs;
            }
            case 7: {    // this is a ClientToServer challenge for ECDSA, it requires a challenge length between 32 and 64 bytes
                generateClientToServerChallenge(32);
                return this.cTOs;
            }
            default:
                return new byte[0];
        }
    }

    /**
     * The global key or encryption key is a custom property of the rtu
     */
    public byte[] getGlobalKey() {
        if (this.encryptionKey == null) {
            this.encryptionKey = DLMSUtils.hexStringToByteArray(properties.getProperty(DATATRANSPORT_ENCRYPTIONKEY, ""));
        }
        return this.encryptionKey;
    }

    /**
     * The HLSSecret is the password of the RTU
     *
     * @return the password of the RTU
     */
    public byte[] getHLSSecret() {
        if (this.hlsSecret == null) {
            this.hlsSecret = properties.getProperty(MeterProtocol.PASSWORD, "");
        }
        byte[] byteWord = new byte[this.hlsSecret.length()];
        for (int i = 0; i < this.hlsSecret.length(); i++) {
            byteWord[i] = (byte) this.hlsSecret.charAt(i);
        }
        return byteWord;
    }

    /**
     * The LLSSecret is the same as the HLSSecret
     *
     * @return the password of the RTU
     */
    public byte[] getLLSSecret() {
        return getHLSSecret();
    }

    /**
     * @return the master key (this is the KeyEncryptionKey)
     */
    public byte[] getMasterKey(){
        if (this.masterKey == null) {
            this.masterKey = DLMSUtils.hexStringToByteArray(properties.getProperty(MASTERKEY, ""));
        }
        return this.masterKey;
    }

    protected void setMasterKey(byte[] masterKey) {
        this.masterKey = masterKey;
    }

    /**
     * Construct the content of the responseValue when a Manufacturer Specific encryption algorithm ({@link com.energyict.dlms.aso.AuthenticationTypes#MAN_SPECIFIC_LEVEL}) is applied.
     *
     * @param respondingAuthenticationValue the response value from the meter OR null
     * @return the encrypted Value to send back to the meter
     */
    public byte[] associationEncryptionByManufacturer(final byte[] respondingAuthenticationValue) throws IOException {
        throw new UnsupportedException("High level security 2 is not supported.");
    }

    /**
     * @return the initial frameCounter
     */
    public long getInitialFrameCounter() {
        if (initialFrameCounter != null) {
            return initialFrameCounter;
        } else {
            SecureRandom generator = new SecureRandom();
            return generator.nextLong();
        }
    }

    public void setInitialFrameCounter(long frameCounter) {
        this.initialFrameCounter = frameCounter;
    }

    /**
     * Provide the handler for the receiving frameCounter
     *
     * @param respondingFrameCounterHandler the object which will handle the received frameCounter
     */
    public void setRespondingFrameCounterHandling(final RespondingFrameCounterHandler respondingFrameCounterHandler) {
        this.respondingFrameCounterHandler = respondingFrameCounterHandler;
    }

    /**
     * @return the used handler for the responding frameCounter
     */
    public RespondingFrameCounterHandler getRespondingFrameCounterHandler() {
        return this.respondingFrameCounterHandler;
    }

    @Override
    public void changeEncryptionKey(byte[] newEncryptionKey) throws IOException {
        this.encryptionKey = newEncryptionKey;
    }

    @Override
    public void changeAuthenticationKey(byte[] newAuthenticationKey) throws IOException {
        this.authenticationKey = newAuthenticationKey;
    }

    @Override
    public void changeMasterKey(byte[] newMasterKey) throws IOException {
        this.masterKey = newMasterKey;
    }

    public byte[] getDedicatedKey() {
        if (dedicatedKey == null) {
            dedicatedKey = new byte[16];
            SecureRandom rnd = new SecureRandom();
            rnd.nextBytes(dedicatedKey);
        }
        return dedicatedKey;
    }

    protected void setDedicatedKey(byte[] dedicatedKey) {
        this.dedicatedKey = dedicatedKey;
    }

    protected void setEncryptionKey(byte[] encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    protected void setHlsSecret(String hlsSecret) {
        this.hlsSecret = hlsSecret;
    }

    public int getClientToServerChallengeLength() {
        if (this.challengeLength == 0) {
            this.challengeLength = Integer.parseInt(this.properties.getProperty(CLIENT_TO_SERVER_CHALLENGE_LENGTH, Integer.toString(ChallengeLength.LENGTH_16_BYTE.length)));
        }
        return this.challengeLength;
    }

    public void setClientToServerChallengeLength(ChallengeLength length) {
        this.challengeLength = length.getLength();
    }

    protected Properties getProperties() {
        return this.properties;
    }

    public enum ChallengeLength {
        LENGTH_8_BYTE(8),
        LENGTH_16_BYTE(16);

        private int length;

        ChallengeLength(int length) {
            this.length = length;
        }

        public int getLength() {
            return length;
        }
    }
}
