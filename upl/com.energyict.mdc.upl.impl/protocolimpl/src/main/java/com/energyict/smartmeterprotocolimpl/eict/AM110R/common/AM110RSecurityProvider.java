package com.energyict.smartmeterprotocolimpl.eict.AM110R.common;

import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;

public class AM110RSecurityProvider extends NTASecurityProvider {
    public AM110RSecurityProvider(TypedProperties properties) {
        super(properties);
        setRespondingFrameCounterHandling(new AM110RRespondingFrameCounterHandler());
    }
}