package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class DeviceLifeCycleFactory {
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public DeviceLifeCycleFactory(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public DeviceLifeCycleInfo from(DeviceLifeCycle deviceLifeCycle){
        DeviceLifeCycleInfo info = new DeviceLifeCycleInfo(deviceLifeCycle);
        info.deviceTypes = deviceConfigurationService.findDeviceTypesUsingDeviceLifeCycle(deviceLifeCycle)
                .stream()
                .map(IdWithNameInfo::new)
                .sorted((dt1, dt2) -> dt1.name.compareToIgnoreCase(dt2.name))
                .collect(Collectors.toList());
        return info;
    }
}
