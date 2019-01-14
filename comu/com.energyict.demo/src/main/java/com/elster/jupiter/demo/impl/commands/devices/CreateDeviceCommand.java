/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;

public class CreateDeviceCommand {
    private DeviceConfigurationTpl configurationTpl = DeviceConfigurationTpl.PROSUMERS;
    private DeviceTypeTpl deviceType = DeviceTypeTpl.Elster_AS1440;
    private String serialNumber;
    private String deviceNamePrefix;

    public void run() {
        DeviceType deviceType = Builders.from(this.deviceType).find()
                .orElseThrow(() -> new UnableToCreate("Unable to find the " + this.deviceType.getName() + " device type"));
        DeviceConfiguration configuration = Builders.from(configurationTpl).withDeviceType(deviceType).find()
                .orElseThrow(() -> new UnableToCreate("Unable to find the device configuration '" + configurationTpl.getName() + "'"));
        Builders.from(DeviceBuilder.class)
                .withName(getDeviceName())
                .withDeviceConfiguration(configuration)
                .withSerialNumber(serialNumber)
                .get();
    }

    protected String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    protected String getDeviceNamePrefix() {
        return deviceNamePrefix;
    }

    public void setDeviceNamePrefix(String deviceNamePrefix) {
        this.deviceNamePrefix = deviceNamePrefix;
    }

    protected String getDeviceName() {
        return this.deviceNamePrefix + serialNumber;
    }

    protected void setDeviceTypeTpl(DeviceTypeTpl deviceTypeTpl) {
        this.deviceType = deviceTypeTpl;
    }

    protected void setConfigurationTpl(DeviceConfigurationTpl configurationTpl) {
        this.configurationTpl = configurationTpl;
    }
}
