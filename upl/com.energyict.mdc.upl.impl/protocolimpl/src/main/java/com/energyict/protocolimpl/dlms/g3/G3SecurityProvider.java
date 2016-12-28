package com.energyict.protocolimpl.dlms.g3;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40SecurityProvider;

import java.util.Properties;

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

    private G3Properties g3Properties;
    private String hlsSecret;

    /**
     * Create a new instance of G3SecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public G3SecurityProvider(Properties properties) {
        super(properties);
        this.g3Properties = new G3Properties(properties, propertySpecService);
    }

    protected void initializeRespondingFrameCounterHandler() {
        setRespondingFrameCounterHandling(new G3RespondingFrameCounterHandler(DLMSConnectionException.REASON_CONTINUE_INVALID_FRAMECOUNTER));   //Validating that the received FC is higher than the previously received FC.
    }

    /**
     * @return the initial frameCounter
     */
    public long getInitialFrameCounter() {
        if (initialFrameCounter != null) {
            return initialFrameCounter;             //G3 way
        } else {
            return super.getInitialFrameCounter();  //DSMR 4.0 way
    }
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
}