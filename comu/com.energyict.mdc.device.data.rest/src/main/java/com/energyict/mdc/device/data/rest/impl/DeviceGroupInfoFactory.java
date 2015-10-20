package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.metering.groups.SearchCriteria;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeviceGroupInfoFactory {

    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public DeviceGroupInfoFactory(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }
    
    public DeviceGroupInfo from(EndDeviceGroup endDeviceGroup) {
        DeviceGroupInfo deviceGroupInfo = new DeviceGroupInfo();
        deviceGroupInfo.id = endDeviceGroup.getId();
        deviceGroupInfo.mRID = endDeviceGroup.getMRID();
        deviceGroupInfo.name = endDeviceGroup.getName();
        deviceGroupInfo.dynamic = endDeviceGroup.isDynamic();
        deviceGroupInfo.version = endDeviceGroup.getVersion();
        if (endDeviceGroup.isDynamic()) {
            DeviceType deviceType = null;
            List<SearchCriteria> searchCriteria = translateCriteria(((QueryEndDeviceGroup) endDeviceGroup).getSearchCriteria(), deviceConfigurationService);
            for (SearchCriteria criteriaToAdd : searchCriteria) {
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

    public List<DeviceGroupInfo> from(List<EndDeviceGroup> endDeviceGroups) {
        List<DeviceGroupInfo> deviceGroupsInfos = new ArrayList<>();
        for (EndDeviceGroup endDeviceGroup : endDeviceGroups) {
            deviceGroupsInfos.add(from(endDeviceGroup));
        }
        return deviceGroupsInfos;
    }


    public List<SearchCriteria> translateCriteria(List<SearchCriteria> criteria, DeviceConfigurationService deviceConfigurationService) {
        List<SearchCriteria> result = new ArrayList<>();
        for (SearchCriteria criterium : criteria) {
            String criteriaName = criterium.getCriteriaName();
            if ("deviceConfiguration.deviceType.id".equals(criteriaName)) {
                List<Object> values = criterium.getCriteriaValues();
                List<Object> newValues = new ArrayList<Object>();
                for (Object value : values) {
                    Optional<DeviceType> deviceTypeOptional = deviceConfigurationService.findDeviceType(((Number) value).longValue());
                    if (deviceTypeOptional.isPresent()) {
                        newValues.add(deviceTypeOptional.get().getName());
                    }
                }
                criterium.setCriteriaValues(newValues);
                criterium.setCriteriaName("deviceConfiguration.deviceType.name");
            } else if ("deviceConfiguration.id".equals(criteriaName)) {
                List<Object> values = criterium.getCriteriaValues();
                List<Object> newValues = new ArrayList<Object>();
                for (Object value : values) {
                    Optional<DeviceConfiguration> deviceConfigurationOptional = deviceConfigurationService.findDeviceConfiguration(((Number) value).longValue());
                    if (deviceConfigurationOptional.isPresent()) {
                        newValues.add(deviceConfigurationOptional.get().getName());
                    }
                }
                criterium.setCriteriaValues(newValues);
                criterium.setCriteriaName("deviceConfiguration.name");
            }
            result.add(criterium);
        }
        return result;
    }

}
