package com.energyict.smartmeterprotocolimpl.eict.AM110R.common;

import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;

import java.util.Properties;


public class AM110RSecurityProvider extends NTASecurityProvider {

    /**
     * Create a new instance of LocalSecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public AM110RSecurityProvider(Properties properties) {
        super(properties);
        setRespondingFrameCounterHandling(new AM110RRespondingFrameCounterHandler());
    }
}