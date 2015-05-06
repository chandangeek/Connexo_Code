package com.energyict.mdc.firmware.rest.impl;

public class DeviceFirmwareActionInfo {
    public String id;
    public String localizedValue;
    public long comTaskId;

    public DeviceFirmwareActionInfo() {

    }

    public DeviceFirmwareActionInfo(String id, String name)  {
        this.id = id;
        this.localizedValue = name;
    }

    public DeviceFirmwareActionInfo(String id, String name, long comTaskId)  {
        this.id = id;
        this.localizedValue = name;
        this.comTaskId = comTaskId;
    }
}
