/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

public class DeviceFirmwareActionInfo extends IdWithLocalizedValue<String>{
    public long comTaskId;
    public long version; // device version here

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
