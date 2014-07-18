package com.energyict.protocolimpl.dlms.common;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.dlms.aso.framecounter.DefaultRespondingFrameCounterHandler;
import com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.UnsupportedException;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

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

    protected int securityLevel = -1;
    protected byte[] cTOs;
    private byte[] authenticationKey;
    private byte[] encryptionKey;
    protected byte[] dedicatedKey;
    private byte[] masterKey;
    private String hlsSecret;
    protected Properties properties;
    private RespondingFrameCounterHandler respondingFrameCounterHandler = new DefaultRespondingFrameCounterHandler();

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
     * Property name of the new LowLevel security Secret
     */
    public static final String NEW_LLS_SECRET = "NewLLSSecret";

    Long initialFrameCounter;

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
     * Generate a random challenge of 8 bytes long
     */
    protected void generateClientToServerChallenge() {
        if (this.cTOs == null) {
            Random generator = new Random();
            this.cTOs = new byte[16];
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
    public byte[] getMasterKey() throws IOException {
        if (this.masterKey == null) {
            this.masterKey = DLMSUtils.hexStringToByteArray(properties.getProperty(MASTERKEY, ""));
        }
        return this.masterKey;
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
            Random generator = new Random();
            return generator.nextLong();
        }
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
        changeEncryptionKey();
    }

    @Override
    public void changeAuthenticationKey(byte[] newAuthenticationKey) throws IOException {
        changeAuthenticationKey();
    }

    public void changeEncryptionKey() throws IOException {
        this.encryptionKey = getNEWGlobalKey();
    }

    public void changeAuthenticationKey() throws IOException {
        this.authenticationKey = getNEWAuthenticationKey();
    }

    public void setInitialFrameCounter(long frameCounter) {
        this.initialFrameCounter = frameCounter;
    }

    //********** Return new keys for KeyChange functionality **********/

    /**
     * @return the new data encryption Authentication Key
     */
    public byte[] getNEWAuthenticationKey() throws IOException {
        return DLMSUtils.hexStringToByteArray(getNEWAuthenticationKeys()[0]);
    }

    /**
     * @return the new authentication Key, as a String array
     * @throws IOException
     */
    public String[] getNEWAuthenticationKeys() throws IOException {
        if (this.properties.containsKey(NEW_DATATRANSPORT_AUTHENTICATION_KEY)) {
            return new String[]{this.properties.getProperty(NEW_DATATRANSPORT_AUTHENTICATION_KEY)};
        }
        throw new IllegalArgumentException("New authenticationKey is not correctly filled in.");
    }

    /**
     * @return the new encryption Key
     */
    public byte[] getNEWGlobalKey() throws IOException {
        return DLMSUtils.hexStringToByteArray(getNEWGlobalKeys()[0]);
    }

    /**
     * @return the new encryption Key, as a String array
     * @throws IOException
     */
    public String[] getNEWGlobalKeys() throws IOException {
        if (this.properties.containsKey(NEW_DATATRANSPORT_ENCRYPTION_KEY)) {
            return new String[]{this.properties.getProperty(NEW_DATATRANSPORT_ENCRYPTION_KEY)};
        }
        throw new IllegalArgumentException("New globalKey is not correctly filled in.");
    }

    /**
     * @return the new HLS secret
     */
    public byte[] getNEWHLSSecret() throws IOException {
        if (this.properties.containsKey(NEW_HLS_SECRET)) {
            return DLMSUtils.hexStringToByteArray(this.properties.getProperty(NEW_HLS_SECRET));
        }
        throw new IllegalArgumentException("New HLSSecret is not correctly filled in.");
    }

    /**
     * @return
     * @throws java.io.IOException
     */
    public byte[] getNEWLLSSecret() throws IOException {
        if (this.properties.containsKey(NEW_LLS_SECRET)) {
            String newLlsSecret = this.properties.getProperty(NEW_LLS_SECRET);
            byte[] byteWord = new byte[newLlsSecret.length()];
            for (int i = 0; i < newLlsSecret.length(); i++) {
                byteWord[i] = (byte) newLlsSecret.charAt(i);
            }
            return byteWord;
        }
        throw new IllegalArgumentException("New LLSSecret is not correctly filled in.");
    }

    public byte[] getDedicatedKey() {
        if (dedicatedKey == null) {
            dedicatedKey = new byte[16];
            Random rnd = new Random();
            rnd.nextBytes(dedicatedKey);
        }
        return dedicatedKey;
    }

    protected void setAuthenticationKey(byte[] authenticationKey) {
        this.authenticationKey = authenticationKey;
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

    protected void setMasterKey(byte[] masterKey) {
        this.masterKey = masterKey;
    }

    protected Properties getProperties() {
        return this.properties;
    }
}
