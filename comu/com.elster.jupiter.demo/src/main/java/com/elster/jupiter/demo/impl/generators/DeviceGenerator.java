package com.elster.jupiter.demo.impl.generators;

import com.elster.jupiter.demo.impl.DemoServiceImpl;
import com.elster.jupiter.demo.impl.Store;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DeviceGenerator {
    private final DeviceService deviceService;
    private final Store store;

    private String mrid;
    private String serialNumber;
    private DeviceConfiguration deviceConfiguration;

    @Inject
    public DeviceGenerator(Store store, DeviceService deviceService) {
        this.deviceService = deviceService;
        this.store = store;
    }

    public DeviceGenerator withMrid(String mrid){
        this.mrid = mrid;
        return this;
    }

    public DeviceGenerator withSerialNumber(String serialNumber){
        this.serialNumber = serialNumber;
        return this;
    }

    public DeviceGenerator withDeviceConfiguration(DeviceConfiguration deviceConfiguration){
        this.deviceConfiguration = deviceConfiguration;
        return this;
    }

    public void create(){
        System.out.println("==> Creating Device '" + mrid + "'...");
        Device device = deviceService.newDevice(deviceConfiguration, mrid, mrid);
        device.setSerialNumber(serialNumber);
        device.setYearOfCertification(LocalDateTime.of(2014, 1, 1, 0, 0).toInstant(ZoneOffset.UTC));
        device.newScheduledComTaskExecution(store.getComSchedules().get(DemoServiceImpl.COM_SCHEDULE_DAILY_READ_ALL)).add();
        device.newScheduledComTaskExecution(store.getComSchedules().get(DemoServiceImpl.COM_SCHEDULE_MOUNTHLY_BILLING_DATA)).add();
        device.save();
        store.add(Device.class, device);
    }

}
