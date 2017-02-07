/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DeviceConfigurationInfos {
    public static class DeviceConfigAndTypeInfo {
        public DeviceConfigurationInfo config;
        public DeviceTypeInfo deviceType;

        public DeviceConfigAndTypeInfo(DeviceConfiguration config) {
            this.config = new DeviceConfigurationInfo(config);
            this.deviceType = DeviceTypeInfo.from(config.getDeviceType());
        }
    }

    public static Comparator<DeviceConfigAndTypeInfo> DEVICE_CONFIG_NAME_COMPARATOR
            = new Comparator<DeviceConfigAndTypeInfo>() {

        public int compare(DeviceConfigAndTypeInfo config1, DeviceConfigAndTypeInfo config2) {
            if(config1.config == null || config2.config == null || config1.config.name == null || config2.config.name == null) {
                throw new IllegalArgumentException("Device configuration information is missed");
            }
            return config1.config.name.compareToIgnoreCase(config2.config.name);
        }
    };

    public List<DeviceConfigAndTypeInfo> deviceConfigurations = new ArrayList<>();
    public int total = 0;

    public DeviceConfigurationInfos() {
    }

    public void add(DeviceConfiguration config) {
        deviceConfigurations.add(new DeviceConfigAndTypeInfo(config));
        total++;
    }
}
