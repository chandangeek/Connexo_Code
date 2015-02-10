package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class DeviceBuilder implements Builder<Device> {
    private final DeviceService deviceService;

    private String mrid;
    private String serialNumber;
    private DeviceConfiguration deviceConfiguration;
    private List<ComSchedule> comSchedules;
    private int yearOfCertification;

    @Inject
    public DeviceBuilder(DeviceService deviceService) {
        this.deviceService = deviceService;
        this.yearOfCertification = 2013;
    }

    public DeviceBuilder withMrid(String mrid){
        this.mrid = mrid;
        return this;
    }

    public DeviceBuilder withSerialNumber(String serialNumber){
        this.serialNumber = serialNumber;
        return this;
    }

    public DeviceBuilder withDeviceConfiguration(DeviceConfiguration deviceConfiguration){
        this.deviceConfiguration = deviceConfiguration;
        return this;
    }

    public DeviceBuilder withComSchedules(List<ComSchedule> comSchedules){
        this.comSchedules = comSchedules;
        return this;
    }

    public DeviceBuilder withYearOfCertification(int year){
        this.yearOfCertification = year;
        return this;
    }

    @Override
    public Optional<Device> find() {
        return Optional.ofNullable(deviceService.findByUniqueMrid(this.mrid));
    }

    @Override
    public Device create() {
        Log.write(this);
        Device device = deviceService.newDevice(deviceConfiguration, mrid, mrid);
        device.setSerialNumber(serialNumber);
        device.setYearOfCertification(this.yearOfCertification);
        if (comSchedules != null) {
            for (ComSchedule comSchedule : comSchedules) {
                device.newScheduledComTaskExecution(comSchedule).add();
            }
        }
        device.save();
        return device;
    }

}
