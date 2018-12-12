/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class DeviceBuilder extends NamedBuilder<Device, DeviceBuilder> {
    private final DeviceService deviceService;
    private final Clock clock;

    private String serialNumber;
    private DeviceConfiguration deviceConfiguration;
    private List<ComSchedule> comSchedules;
    private int yearOfCertification;
    private Instant shippingDate;
    private String modelNumber;
    private String modelVersion;
    private String manufacturer;
    private Random random = new Random();

    @Inject
    public DeviceBuilder(DeviceService deviceService, Clock clock) {
        super(DeviceBuilder.class);
        this.deviceService = deviceService;
        this.clock = clock;
        this.yearOfCertification = 2013;
    }

    public DeviceBuilder withSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    public DeviceBuilder withDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        this.deviceConfiguration = deviceConfiguration;
        this.manufacturer = deviceConfiguration.getDeviceType().getName().split(" ")[0];
        this.modelNumber = deviceConfiguration.getDeviceType().getName().split(" ")[1];
        this.modelVersion = Integer.toString(random.nextInt(10));
        return this;
    }

    public DeviceBuilder withComSchedules(List<ComSchedule> comSchedules) {
        this.comSchedules = comSchedules;
        return this;
    }

    public DeviceBuilder withYearOfCertification(int year) {
        this.yearOfCertification = year;
        return this;
    }

    public DeviceBuilder withShippingDate(Instant date) {
        this.shippingDate = date;
        return this;
    }

    @Override
    public Optional<Device> find() {
        return deviceService.findDeviceByName(getName());
    }

    @Override
    public Device create() {
        Log.write(this);
        if(this.shippingDate == null) {
            this.shippingDate = clock.instant().minusSeconds(60);
        }
        com.energyict.mdc.device.data.DeviceBuilder deviceBuilder = deviceService.newDeviceBuilder(deviceConfiguration, getName(), this.shippingDate);
        deviceBuilder.withSerialNumber(serialNumber);
        deviceBuilder.withYearOfCertification(this.yearOfCertification);
        deviceBuilder.withModelNumber(this.modelNumber);
        deviceBuilder.withModelVersion(this.modelVersion);
        deviceBuilder.withManufacturer(this.manufacturer);
        Device device = deviceBuilder.create();
        if (comSchedules != null) {
            for (ComSchedule comSchedule : comSchedules) {
                device.newScheduledComTaskExecution(comSchedule).add();
            }
        }
        device.save();
        applyPostBuilders(device);
        return device;
    }
}
