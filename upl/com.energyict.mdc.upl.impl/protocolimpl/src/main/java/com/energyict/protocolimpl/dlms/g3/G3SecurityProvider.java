package com.energyict.protocolimpl.dlms.g3;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40SecurityProvider;

import java.io.IOException;

/**
 * Extension on the Dsmr40SecurityProvider, with extra functionality for immediate key changing.
 * The NEWGlobalKey and NEWAuthenticationKey must contain the original and the wrapped key, comma separated.
 * Wrapped key is necessary to write to the device, original key is necessary to use (after key has been written) in the encryption and decryption of the communication
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/10/12
 * Time: 14:41
 * Author: khe
 */
public class G3SecurityProvider extends Dsmr40SecurityProvider {

    private static final String SEPARATOR = ",";
    private long frameCounter = -1;
    private G3Properties g3Properties;
    private String hlsSecret;

    /**
     * Create a new instance of G3SecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public G3SecurityProvider(G3Properties properties) {
        super(properties.getProtocolProperties());
        this.g3Properties = properties;
    }

    public long getFrameCounter() {
        return frameCounter;
    }

    public void setFrameCounter(long frameCounter) {
        this.frameCounter = frameCounter;
    }

    @Override
    public byte[] getHLSSecret() {
        String hexPassword = getProperties().getProperty(Dsmr40Properties.DSMR_40_HEX_PASSWORD);
        if (hexPassword != null) {
            return com.energyict.dlms.DLMSUtils.hexStringToByteArray(hexPassword);
        }
        if (this.hlsSecret == null) {
            this.hlsSecret = g3Properties.getPassword();
        }
        byte[] byteWord = new byte[this.hlsSecret.length()];
        for (int i = 0; i < this.hlsSecret.length(); i++) {
            byteWord[i] = (byte) this.hlsSecret.charAt(i);
        }
        return byteWord;
    }

    /**
     * String array containing the original key and the wrapped key
     */
    @Override
    public String[] getNEWAuthenticationKeys() throws IOException {
        if (getProperties().containsKey(NEW_DATATRANSPORT_AUTHENTICATION_KEY)) {
            String keys = getProperties().getProperty(NEW_DATATRANSPORT_AUTHENTICATION_KEY);   //Comma separated keys
            return splitKeys(keys, NEW_DATATRANSPORT_AUTHENTICATION_KEY);                      //Array of original and wrapped key
        }
        throw new IllegalArgumentException("New authenticationKey is not correctly filled in.");
    }

    /**
     * String array containing the original key and the wrapped key
     */
    @Override
    public String[] getNEWGlobalKeys() throws IOException {
        if (getProperties().containsKey(NEW_DATATRANSPORT_ENCRYPTION_KEY)) {
            String keys = getProperties().getProperty(NEW_DATATRANSPORT_ENCRYPTION_KEY);
            return splitKeys(keys, NEW_DATATRANSPORT_ENCRYPTION_KEY);
        }
        throw new IllegalArgumentException("New encryptionKey is not correctly filled in.");
    }

    @Override
    public void changeEncryptionKey() throws IOException {
        setEncryptionKey(ProtocolTools.getBytesFromHexString(getNEWGlobalKeys()[0], ""));    //First key is the original (not wrapped) key
    }

    @Override
    public void changeAuthenticationKey() throws IOException {
        setAuthenticationKey(ProtocolTools.getBytesFromHexString(getNEWAuthenticationKeys()[0], ""));
    }

    public String[] splitKeys(String keys, String propertyName) {
        String[] splitKeys = keys.split(SEPARATOR);
        if (splitKeys.length == 2) {
            return splitKeys;
        } else {
            throw new IllegalArgumentException("Invalid property '" + propertyName + "': should contain the original key and the wrapped key, comma separated");
        }
    }

    @Override
    public long getInitialFrameCounter() {
        return frameCounter == -1 ? 1 : frameCounter;
    }
}