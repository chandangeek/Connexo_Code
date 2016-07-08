package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;

public class CreateDeviceCommand {

    private DeviceTypeTpl deviceType = DeviceTypeTpl.Elster_AS1440;
    private String serialNumber;
    private String mridPrefix;

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setMridPrefix(String mridPrefix) {
        this.mridPrefix = mridPrefix;
    }

    public void run(){
        DeviceType deviceType = Builders.from(this.deviceType).find()
                .orElseThrow(() -> new UnableToCreate("Unable to find the " + this.deviceType.getLongName()+ " device type"));
        DeviceConfiguration configuration = Builders.from(DeviceConfigurationTpl.DEFAULT).withDeviceType(deviceType).find()
                .orElseThrow(() -> new UnableToCreate("Unable to find the Default device configuration"));
        Builders.from(DeviceBuilder.class)
                .withMrid(getMrid())
                .withDeviceConfiguration(configuration)
                .withSerialNumber(serialNumber)
                .get();
    }

    protected void setDeviceTypeTpl(DeviceTypeTpl deviceTypeTpl){
        this.deviceType = deviceTypeTpl;
    }

    protected String getSerialNumber() {
        return serialNumber;
    }

    protected String getMridPrefix() {
        return mridPrefix;
    }

    protected String getMrid(){
        return this.mridPrefix + serialNumber;
    }
}
