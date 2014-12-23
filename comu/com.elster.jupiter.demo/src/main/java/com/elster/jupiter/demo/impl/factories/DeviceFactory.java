package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.Store;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DeviceFactory implements Factory<Device> {
    private final DeviceService deviceService;
    private final Store store;

    private String mrid;
    private String serialNumber;
    private DeviceConfiguration deviceConfiguration;

    @Inject
    public DeviceFactory(Store store, DeviceService deviceService) {
        this.deviceService = deviceService;
        this.store = store;
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

    public Device get(){
        Log.write(this);
        Device device = deviceService.newDevice(deviceConfiguration, mrid, mrid);
        device.setSerialNumber(serialNumber);
        device.setYearOfCertification(LocalDateTime.of(2014, 1, 1, 0, 0).toInstant(ZoneOffset.UTC));
        device.newScheduledComTaskExecution(store.getComSchedules().get(Constants.CommunicationSchedules.DAILY_READ_ALL)).add();
        device.newScheduledComTaskExecution(store.getComSchedules().get(Constants.CommunicationSchedules.MONTHLY_BILLING_DATA)).add();
        device.save();
        store.add(Device.class, device);
        return device;
    }

}
