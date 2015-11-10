package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.search.rest.SearchablePropertyValueConverter;

import java.util.ArrayList;
import java.util.List;

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
        List<DeviceGroupInfo> deviceGroupsInfos = new ArrayList<>();
        for (EndDeviceGroup endDeviceGroup : endDeviceGroups) {
            deviceGroupsInfos.add(from(endDeviceGroup));
        }
        return deviceGroupsInfos;
    }
}
