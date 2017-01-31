/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.search.rest.SearchablePropertyValueConverter;

import java.util.List;
import java.util.stream.Collectors;

public class DeviceGroupInfoFactory {
    
    public DeviceGroupInfo from(EndDeviceGroup endDeviceGroup) {
        DeviceGroupInfo deviceGroupInfo = new DeviceGroupInfo();
        deviceGroupInfo.id = endDeviceGroup.getId();
        deviceGroupInfo.mRID = endDeviceGroup.getMRID();
        deviceGroupInfo.name = endDeviceGroup.getName();
        deviceGroupInfo.dynamic = endDeviceGroup.isDynamic();
        deviceGroupInfo.version = endDeviceGroup.getVersion();
        if (endDeviceGroup.isDynamic()) {
            QueryEndDeviceGroup queryEndDeviceGroup = (QueryEndDeviceGroup) endDeviceGroup;
            deviceGroupInfo.filter = SearchablePropertyValueConverter.convert(queryEndDeviceGroup.getSearchablePropertyValues());
        }
        return deviceGroupInfo;
    }

    public List<DeviceGroupInfo> from(List<EndDeviceGroup> endDeviceGroups) {
        return endDeviceGroups.stream()
                .map(this::from)
                .collect(Collectors.toList());
    }
}
