/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class EndDeviceEventOrActionAdapter extends MapBasedXmlAdapter<EndDeviceEventOrAction> {

    public EndDeviceEventOrActionAdapter() {
        for (EndDeviceEventOrAction eventOrAction : EndDeviceEventOrAction.values()) {
            register(eventOrAction.name(), eventOrAction);
        }
    }
}
