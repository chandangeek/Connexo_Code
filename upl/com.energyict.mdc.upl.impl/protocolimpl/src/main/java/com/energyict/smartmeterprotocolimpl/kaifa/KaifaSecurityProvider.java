package com.energyict.smartmeterprotocolimpl.kaifa;

import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RRespondingFrameCounterHandler;

public class KaifaSecurityProvider extends NTASecurityProvider {

    /**
     * Create a new instance of LocalSecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public KaifaSecurityProvider(TypedProperties properties) {
        super(properties);
        setRespondingFrameCounterHandling(new AM110RRespondingFrameCounterHandler());
    }

    @Override
    public byte[] getHLSSecret() {
        String hexPassword = getProperties().getTypedProperty("Password");
        if (hexPassword != null) {
            return com.energyict.dlms.DLMSUtils.hexStringToByteArray(hexPassword);
        }
        return null;
    }
}