package com.energyict.smartmeterprotocolimpl.nta.esmr50.common;

import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40SecurityProvider;

import java.util.Properties;
import java.util.Random;

@Deprecated
public class ESMR50SecurityProvider extends Dsmr40SecurityProvider {

    /**
     * Create a new instance of LocalSecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public ESMR50SecurityProvider(TypedProperties properties) {
        super(properties);
        initializeRespondingFrameCounterHandler();
    }

    protected void initializeRespondingFrameCounterHandler() {
        setRespondingFrameCounterHandling(new ESMR50RespondingFrameCounterHandler());
    }

    /**
     * The HLSSecret is the password of the RTU
     *
     * @return the password of the RTU
     */
    @Override
    public byte[] getHLSSecret() {
        String hexPassword = String.valueOf(getProperties().getProperty(ESMR50Properties.ESMR_50_HEX_PASSWORD));
        if (hexPassword != null) {
            return com.energyict.dlms.DLMSUtils.hexStringToByteArray(hexPassword);
        } else {
            return super.getHLSSecret();
        }
    }
    public void setInitialFrameCounter(Long frameCounter){
        if (frameCounter!=null) {
            initialFrameCounter = frameCounter;
        }
    }
    /**
     * @return the initial frameCounter
     */
    @Override
    public long getInitialFrameCounter() {
        // will be overwritten with the cache
        if (initialFrameCounter == null){
            initialFrameCounter = Long.valueOf(-1);
        }
        return initialFrameCounter;
    }

}
