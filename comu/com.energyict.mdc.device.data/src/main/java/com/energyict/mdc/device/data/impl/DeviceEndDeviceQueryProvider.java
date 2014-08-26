package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceQueryProvider;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.DeviceDataService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Date;
import java.util.List;

@Component(name = "com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider", service = {EndDeviceQueryProvider.class}, property = "name=" + DeviceDataService.COMPONENTNAME, immediate = true)
public class DeviceEndDeviceQueryProvider implements EndDeviceQueryProvider {

    private volatile MeteringGroupsService meteringGroupsService;
    private volatile MeteringService meteringService;

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
        return null;
    }
}

