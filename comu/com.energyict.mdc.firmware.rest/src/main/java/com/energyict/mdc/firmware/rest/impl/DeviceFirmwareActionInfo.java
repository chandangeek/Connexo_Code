package com.energyict.mdc.firmware.rest.impl;

public class DeviceFirmwareActionInfo extends IdWithLocalizedValue<String>{
    public long comTaskId;

    public DeviceFirmwareActionInfo() {
        super();
    }

    public DeviceFirmwareActionInfo(String id, String name)  {
        super(id, name);
    }

    public DeviceFirmwareActionInfo(String id, String name, long comTaskId)  {
        this(id, name);
        this.comTaskId = comTaskId;
    }
}
