package com.energyict.protocolimplv2.dlms.ei6v2021;

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.ei7.EI7Inbound;

public class EI6v2021Inbound extends EI7Inbound {
    private EI6DataPushNotificationParser parser;

    public EI6v2021Inbound(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getVersion() {
        return "2022-06-14";
    }

    @Override
    protected EI6DataPushNotificationParser getParser() {
        if (parser == null) {
            parser = new EI6DataPushNotificationParser(comChannel, getContext());
        }
        return parser;
    }
}
