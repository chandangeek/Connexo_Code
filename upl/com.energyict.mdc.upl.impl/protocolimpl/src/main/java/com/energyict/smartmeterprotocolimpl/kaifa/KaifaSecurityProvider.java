package com.energyict.smartmeterprotocolimpl.kaifa;

import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RRespondingFrameCounterHandler;

import java.util.Properties;


public class KaifaSecurityProvider extends NTASecurityProvider {

    /**
     * Create a new instance of LocalSecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public KaifaSecurityProvider(Properties properties) {
        super(properties);
        setRespondingFrameCounterHandling(new AM110RRespondingFrameCounterHandler());
    }

    @Override
    public byte[] getHLSSecret() {
        String hexPassword = getProperties().getProperty("Password");
        if (hexPassword != null) {
            return com.energyict.dlms.DLMSUtils.hexStringToByteArray(hexPassword);
        }
        return null;
    }
}