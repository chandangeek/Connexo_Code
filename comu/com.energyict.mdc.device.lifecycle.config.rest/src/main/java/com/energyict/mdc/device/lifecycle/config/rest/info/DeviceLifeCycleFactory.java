/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DeviceLifeCycleFactory {
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public DeviceLifeCycleFactory(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public DeviceLifeCycleInfo from(DeviceLifeCycle deviceLifeCycle){
        DeviceLifeCycleInfo info = new DeviceLifeCycleInfo(deviceLifeCycle);
        List<DeviceType> deviceTypes = deviceConfigurationService.findDeviceTypesUsingDeviceLifeCycle(deviceLifeCycle);
        info.deviceTypes = createFromDeviceType(deviceTypes);
//        info.deviceTypes = deviceTyes
//                .stream()
//                .map(IdWithNameInfo::new)
//                .sorted((dt1, dt2) -> dt1.name.compareToIgnoreCase(dt2.name))
//                .collect(Collectors.toList());
        return info;
    }

    // Temporary change - why doesn't this work with streams?
    private List<IdWithNameInfo> createFromDeviceType(List<DeviceType> deviceTypes) {
        List<IdWithNameInfo> idsWithName = new ArrayList<>();
        for (DeviceType devicetype : deviceTypes) {
            idsWithName.add(new IdWithNameInfo(devicetype));
        }

        idsWithName.sort(new Comparator<IdWithNameInfo>() {
            @Override
            public int compare(IdWithNameInfo dt1, IdWithNameInfo dt2) {
                return dt1.name.compareToIgnoreCase(dt2.name);
            }
        });

        return idsWithName;
    }
}
