package com.energyict.protocolimplv2.nta.esmr50.common;

import com.energyict.protocolimplv2.nta.dsmr40.Dsmr40SecurityProvider;

public class ESMR50SecurityProvider extends Dsmr40SecurityProvider {

    /**
     * Create a new instance of LocalSecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     * @param authenticationLevel
     */
    public ESMR50SecurityProvider(com.energyict.mdc.upl.TypedProperties properties, int authenticationLevel) {
        super(properties, authenticationLevel);
    }

    @Override
    public byte[] getHLSSecret() {
        String hexPassword = properties.getTypedProperty(ESMR50ConfigurationSupport.ESMR_50_HEX_PASSWORD);
        if (hexPassword != null) {
            return com.energyict.dlms.DLMSUtils.hexStringToByteArray(hexPassword);
        } else {
            return super.getHLSSecret();
        }
    }

    protected void initializeRespondingFrameCounterHandler() {
        setRespondingFrameCounterHandling(new ESMR50RespondingFrameCounterHandler());
    }

}
