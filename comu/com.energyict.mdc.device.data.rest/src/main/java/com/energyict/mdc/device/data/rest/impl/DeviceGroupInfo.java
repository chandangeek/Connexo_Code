package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.metering.groups.SearchCriteria;

import java.util.ArrayList;
import java.util.List;

public class DeviceGroupInfo {

    public long id;
    public String mRID;
    public String name;
    public boolean dynamic;
    public List<SearchCriteriaInfo> criteria = new ArrayList<SearchCriteriaInfo>();

    public DeviceGroupInfo() {
    }

    public static DeviceGroupInfo from(EndDeviceGroup endDeviceGroup) {
        DeviceGroupInfo deviceGroupInfo = new DeviceGroupInfo();
        deviceGroupInfo.id = endDeviceGroup.getId();
        deviceGroupInfo.mRID = endDeviceGroup.getMRID();
        deviceGroupInfo.name = endDeviceGroup.getName();
        deviceGroupInfo.dynamic = endDeviceGroup.isDynamic();
        if (endDeviceGroup.isDynamic()) {
           for (SearchCriteria criteriaToAdd : ((QueryEndDeviceGroup) endDeviceGroup).getSearchCriteria()) {
               deviceGroupInfo.criteria.add(new SearchCriteriaInfo(criteriaToAdd));
           }
        }
        return deviceGroupInfo;
    }

    public static List<DeviceGroupInfo> from(List<EndDeviceGroup> endDeviceGroups) {
        List<DeviceGroupInfo> deviceGroupsInfos = new ArrayList<DeviceGroupInfo>();
        for (EndDeviceGroup endDeviceGroup : endDeviceGroups) {
            deviceGroupsInfos.add(DeviceGroupInfo.from(endDeviceGroup));
        }
        return deviceGroupsInfos;
    }

}
