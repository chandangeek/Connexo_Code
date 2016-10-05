package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;

import java.time.Instant;

public class CreateDeviceCommand {

    private DeviceConfigurationTpl configurationTpl = DeviceConfigurationTpl.DEFAULT;
    private DeviceTypeTpl deviceType = DeviceTypeTpl.Elster_AS1440;
    private String serialNumber;
    private String mridPrefix;
    private Instant shipmentDate;

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setMridPrefix(String mridPrefix) {
        this.mridPrefix = mridPrefix;
    }

    public void setShipmentDate(Instant shipmentDate) {
        this.shipmentDate = shipmentDate;
    }

    public void run() {
        DeviceType deviceType = Builders.from(this.deviceType).find()
                .orElseThrow(() -> new UnableToCreate("Unable to find the " + this.deviceType.getLongName() + " device type"));
        DeviceConfiguration configuration = Builders.from(this.configurationTpl).withDeviceType(deviceType).find()
                .orElseThrow(() -> new UnableToCreate("Unable to find the device configuration '" + this.configurationTpl.getName() + "'"));
        Builders.from(DeviceBuilder.class)
                .withMrid(getMrid())
                .withDeviceConfiguration(configuration)
                .withSerialNumber(this.serialNumber)
                .withShipmentDate(this.shipmentDate)
                .get();
    }

    protected void setDeviceTypeTpl(DeviceTypeTpl deviceTypeTpl) {
        this.deviceType = deviceTypeTpl;
    }

    protected String getSerialNumber() {
        return this.serialNumber;
    }

    protected String getMridPrefix() {
        return this.mridPrefix;
    }

    protected String getMrid() {
        return this.mridPrefix + this.serialNumber;
    }

    protected void setConfigurationTpl(DeviceConfigurationTpl configurationTpl) {
        this.configurationTpl = configurationTpl;
    }
}
