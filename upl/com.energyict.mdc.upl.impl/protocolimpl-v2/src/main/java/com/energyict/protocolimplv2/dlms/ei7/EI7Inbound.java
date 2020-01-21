package com.energyict.protocolimplv2.dlms.ei7;

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.a2.A2Inbound;

public class EI7Inbound extends A2Inbound {

    private EI7DataPushNotificationParser parser;

    public EI7Inbound(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public DiscoverResultType doDiscovery() {
//        parser = new EI7DataPushNotificationParser(comChannel, getContext());
//        parser.parseInboundFrame();
        return super.doDiscovery();
    }
}
