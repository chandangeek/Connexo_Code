package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.metering.groups.SearchCriteria;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeviceGroupInfo {

    public long id;
    public String mRID;
    public String name;
    public boolean dynamic;
    public List<SearchCriteriaInfo> criteria = new ArrayList<>(); //backend => frontend
    public Object filter;  // frontend => backend (contains filter criteria for a dynamic group)
    public Object devices;  // frontend => backend (contains selected deices ids for a static group)

    public List<Long> deviceTypeIds = new ArrayList<>();  //backend => frontend
    public List<Long> deviceConfigurationIds = new ArrayList<>(); //backend => frontend


    public DeviceGroupInfo() {
    }

    public static DeviceGroupInfo from(EndDeviceGroup endDeviceGroup, DeviceConfigurationService deviceConfigurationService) {
        DeviceGroupInfo deviceGroupInfo = new DeviceGroupInfo();
        deviceGroupInfo.id = endDeviceGroup.getId();
        deviceGroupInfo.mRID = endDeviceGroup.getMRID();
        deviceGroupInfo.name = endDeviceGroup.getName();
        deviceGroupInfo.dynamic = endDeviceGroup.isDynamic();
        if (endDeviceGroup.isDynamic()) {
            DeviceType deviceType = null;
            for (SearchCriteria criteriaToAdd : ((QueryEndDeviceGroup) endDeviceGroup).getSearchCriteria()) {
                deviceGroupInfo.criteria.add(new SearchCriteriaInfo(criteriaToAdd));

                String criteriaName = criteriaToAdd.getCriteriaName();
                List<Object> values = criteriaToAdd.getCriteriaValues();

                if ("deviceConfiguration.deviceType.name".equals(criteriaName)) {
                    for (Object value : values) {
                        String deviceTypeName = (String) value;
                        Optional<DeviceType> deviceTypeOptional = deviceConfigurationService.findDeviceTypeByName(deviceTypeName);
                        if (deviceTypeOptional.isPresent()) {
                            deviceType = deviceTypeOptional.get();
                            deviceGroupInfo.deviceTypeIds.add(deviceType.getId());
                        }
                    }
                } else if ("deviceConfiguration.name".equals(criteriaName)) {
                    for (Object value : values) {
                        if (deviceType != null) {
                            String deviceConfigurationName = (String) value;
                            List<DeviceConfiguration> configs = deviceType.getConfigurations();
                            for (DeviceConfiguration config : configs) {
                                if (config.getName().equals(deviceConfigurationName)) {
                                    deviceGroupInfo.deviceConfigurationIds.add(config.getId());
                                }
                            }
                        }
                    }
                }
            }
        }
        return deviceGroupInfo;
    }

    public static List<DeviceGroupInfo> from(List<EndDeviceGroup> endDeviceGroups, DeviceConfigurationService deviceConfigurationService) {
        List<DeviceGroupInfo> deviceGroupsInfos = new ArrayList<>();
        for (EndDeviceGroup endDeviceGroup : endDeviceGroups) {
            deviceGroupsInfos.add(DeviceGroupInfo.from(endDeviceGroup, deviceConfigurationService));
        }
        return deviceGroupsInfos;
    }

}
