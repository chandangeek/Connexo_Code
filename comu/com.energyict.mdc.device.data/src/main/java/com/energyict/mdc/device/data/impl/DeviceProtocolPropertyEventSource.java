/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;


public class DeviceProtocolPropertyEventSource {

    private final String MRID;

    DeviceProtocolPropertyEventSource(String MRID){
        this.MRID = MRID;
    }

    public String getMRID() {
        return this.MRID;
    }

}
