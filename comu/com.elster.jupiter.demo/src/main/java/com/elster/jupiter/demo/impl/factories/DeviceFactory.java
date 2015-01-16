package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.demo.impl.finders.ComScheduleFinder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

public class DeviceFactory implements Factory<Device> {
    private final DeviceService deviceService;
    private final Store store;
    private final Provider<ComScheduleFinder> scheduleFinderProvider;

    private String mrid;
    private String serialNumber;
    private DeviceConfiguration deviceConfiguration;
    private List<String> comSchedules;

    @Inject
    public DeviceFactory(Store store, DeviceService deviceService, Provider<ComScheduleFinder> scheduleFinderProvider) {
        this.deviceService = deviceService;
        this.store = store;
        this.scheduleFinderProvider = scheduleFinderProvider;
    }

    public DeviceFactory withMrid(String mrid){
        this.mrid = mrid;
        return this;
    }

    public DeviceFactory withSerialNumber(String serialNumber){
        this.serialNumber = serialNumber;
        return this;
    }

    public DeviceFactory withDeviceConfiguration(DeviceConfiguration deviceConfiguration){
        this.deviceConfiguration = deviceConfiguration;
        return this;
    }

    public DeviceFactory withComSchedules(String... comSchedules){
        if (comSchedules != null) {
            this.comSchedules = Arrays.asList(comSchedules);
        } else{
            this.comSchedules = null;
        }
        return this;
    }

    public Device get(){
        Log.write(this);
        Device device = deviceService.newDevice(deviceConfiguration, mrid, mrid);
        device.setSerialNumber(serialNumber);
        device.setYearOfCertification(LocalDateTime.of(2013, 6, 1, 0, 0).toInstant(ZoneOffset.UTC));
        if (comSchedules != null) {
            for (String comSchedule : comSchedules) {
                device.newScheduledComTaskExecution(scheduleFinderProvider.get().withName(comSchedule).find()).add();
            }
        }
        device.save();
        store.add(Device.class, device);
        return device;
    }

}
