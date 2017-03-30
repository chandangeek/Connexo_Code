/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest;

import com.energyict.mdc.device.config.DeviceConfiguration;

/**
 * Created by bvn on 10/23/14.
 */
public class DeviceConfigurationIdInfo {
    public long deviceTypeId;
    public long id;
    public String name;

    public DeviceConfigurationIdInfo() {
    }

    public DeviceConfigurationIdInfo(DeviceConfiguration deviceConfiguration) {
        this.name=deviceConfiguration.getName();
        this.id=deviceConfiguration.getId();
        this.deviceTypeId=deviceConfiguration.getDeviceType().getId();
    }
}
