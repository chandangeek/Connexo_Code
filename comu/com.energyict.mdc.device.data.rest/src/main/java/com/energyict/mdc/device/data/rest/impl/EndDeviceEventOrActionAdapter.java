package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class EndDeviceEventOrActionAdapter extends MapBasedXmlAdapter<EndDeviceEventorAction> {

    public EndDeviceEventOrActionAdapter() {
        for (EndDeviceEventorAction eventOrAction : EndDeviceEventorAction.values()) {
            register(eventOrAction.name(), eventOrAction);
        }
    }
}
