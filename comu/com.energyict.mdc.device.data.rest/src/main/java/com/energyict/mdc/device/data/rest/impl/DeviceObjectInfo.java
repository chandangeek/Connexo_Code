package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.EndDevice;

public class DeviceObjectInfo {
    public long id;
    public String name;
    public String mrId;

    public DeviceObjectInfo(long id , String name, String  mrId) {
            this.id = id;
            this.name = name;
            this.mrId = mrId;
    }
}

