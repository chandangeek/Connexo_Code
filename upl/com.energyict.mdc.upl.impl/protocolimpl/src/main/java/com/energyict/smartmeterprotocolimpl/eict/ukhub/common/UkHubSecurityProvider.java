package com.energyict.smartmeterprotocolimpl.eict.ukhub.common;

import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;

/**
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 2/02/12
 * Time: 10:20
 * To change this template use File | Settings | File Templates.
 */
public class UkHubSecurityProvider extends NTASecurityProvider{
    public UkHubSecurityProvider(TypedProperties properties) {
        super(properties);
        setRespondingFrameCounterHandling(new UkHubRespondingFrameCounterHandler());
    }
}