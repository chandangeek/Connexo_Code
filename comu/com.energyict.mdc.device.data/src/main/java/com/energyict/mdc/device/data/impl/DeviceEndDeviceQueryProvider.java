package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceQueryProvider;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component(name = "com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider", service = {EndDeviceQueryProvider.class}, property = "name=" + DeviceDataService.COMPONENTNAME, immediate = true)
public class DeviceEndDeviceQueryProvider implements EndDeviceQueryProvider {

    private volatile MeteringGroupsService meteringGroupsService;
    private volatile MeteringService meteringService;
    private volatile DeviceDataService deviceDataService;

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }


    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public static final String DEVICE_ENDDEVICE_QUERYPRVIDER = DeviceEndDeviceQueryProvider.class.getName();

    @Override
    public String getName() {
        return DEVICE_ENDDEVICE_QUERYPRVIDER;
    }

    @Override
    public List<EndDevice> findEndDevices(Condition conditions) {
        return findEndDevices(new Date(), conditions);
    }

    @Override
    public List<EndDevice> findEndDevices(Date date, Condition conditions) {
        List<Device> devices = deviceDataService.findAllDevices(conditions).find();
        List<EndDevice> meters = new ArrayList<EndDevice>();
        for (Device device : devices) {
            Optional<Meter> optionalMeter =
                    meteringService.findAmrSystem(1).get().findMeter(String.valueOf(device.getId()));
            if (optionalMeter.isPresent()) {
                meters.add(optionalMeter.get());
            }
        }
        return meters;
    }
}

