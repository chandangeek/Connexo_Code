package com.energyict.smartmeterprotocolimpl.eict.ukhub.common;

import com.energyict.genericprotocolimpl.nta.abstractnta.NTASecurityProvider;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 2/02/12
 * Time: 10:20
 * To change this template use File | Settings | File Templates.
 */
public class UkHubSecurityProvider extends NTASecurityProvider{

    /**
     * Create a new instance of LocalSecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public UkHubSecurityProvider(Properties properties) {
        super(properties);
        setRespondingFrameCounterHandling(new UkHubRespondingFrameCounterHandler());
    }
}
