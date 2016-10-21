package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import javax.inject.Inject;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DeviceBuilder extends NamedBuilder<Device, DeviceBuilder> {
    private final DeviceService deviceService;
    private final Clock clock;

    private String mrid;
    private String serialNumber;
    private DeviceConfiguration deviceConfiguration;
    private List<ComSchedule> comSchedules;
    private int yearOfCertification;
    private Location location;
    private SpatialCoordinates spatialCoordinates;

    private List<Consumer<Device>> postBuilders;

    @Inject
    public DeviceBuilder(DeviceService deviceService, Clock clock) {
        super(DeviceBuilder.class);
        this.deviceService = deviceService;
        this.clock = clock;
        this.yearOfCertification = 2013;
    }

    public DeviceBuilder withMrid(String mrid){
        this.mrid = mrid;
        super.withName(mrid);
        return this;
    }

    public DeviceBuilder withLocation(Location location){
        this.location = location;
        return this;
    }

    public DeviceBuilder withSpatialCoordinates(SpatialCoordinates spatialCoordinates) {
        this.spatialCoordinates = spatialCoordinates;
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
        return deviceService.findByUniqueMrid(this.mrid);
    }

    @Override
    public Device create() {
        Log.write(this);
        Device device = deviceService.newDevice(deviceConfiguration, getName(), mrid, clock.instant());
        device.setSerialNumber(serialNumber);
        device.setYearOfCertification(this.yearOfCertification);
        if (comSchedules != null) {
            for (ComSchedule comSchedule : comSchedules) {
                device.getDeviceConfiguration().getComTaskEnablements()
                        .stream()
                        .forEach(comTaskEnablement -> device.newScheduledComTaskExecution(comTaskEnablement, comSchedule).add());
            }
        }
        device.setLocation(location);
        device.setSpatialCoordinates(spatialCoordinates);
        device.save();
        applyPostBuilders(device);
        return device;
    }

}
