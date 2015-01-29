package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;

public class CreateDeviceCommand {
    private String serialNumber;
    private String mridPrefix;

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setMridPrefix(String mridPrefix) {
        this.mridPrefix = mridPrefix;
    }

    public void run(){
        DeviceType deviceType = Builders.from(DeviceTypeTpl.Elster_AS1440).find()
                .orElseThrow(() -> new UnableToCreate("Unable to find the Elster AS1440 device type"));
        DeviceConfiguration configuration = Builders.from(DeviceConfigurationTpl.DEFAULT).withDeviceType(deviceType).find()
                .orElseThrow(() -> new UnableToCreate("Unable to find the Default device configuration"));
        Builders.from(DeviceBuilder.class)
                .withMrid(this.mridPrefix + serialNumber)
                .withDeviceConfiguration(configuration)
                .withSerialNumber(serialNumber)
                .get();
    }

    protected String getSerialNumber() {
        return serialNumber;
    }

    protected String getMridPrefix() {
        return mridPrefix;
    }
}
