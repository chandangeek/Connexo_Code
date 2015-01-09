package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.metering.groups.SearchCriteria;
import com.elster.jupiter.rest.util.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import java.time.Instant;
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
    public Object devices;  // frontend <=> backend (contains selected devices ids for a static group)

    public List<Long> deviceTypeIds = new ArrayList<>();  //backend => frontend
    public List<Long> deviceConfigurationIds = new ArrayList<>(); //backend => frontend
    public List<Long> selectedDevices = new ArrayList<>(); //backend => frontend


    public DeviceGroupInfo() {
    }

    public static DeviceGroupInfo from(EndDeviceGroup endDeviceGroup, DeviceConfigurationService deviceConfigurationService, DeviceService deviceService) {
        DeviceGroupInfo deviceGroupInfo = new DeviceGroupInfo();
        deviceGroupInfo.id = endDeviceGroup.getId();
        deviceGroupInfo.mRID = endDeviceGroup.getMRID();
        deviceGroupInfo.name = endDeviceGroup.getName();
        deviceGroupInfo.dynamic = endDeviceGroup.isDynamic();
        if (endDeviceGroup.isDynamic()) {
            DeviceType deviceType = null;
            List<SearchCriteria> searchCriteria =
                    translateCriteria(((QueryEndDeviceGroup) endDeviceGroup).getSearchCriteria(), deviceConfigurationService);
            for (SearchCriteria criteriaToAdd : searchCriteria) {
                deviceGroupInfo.criteria.add(new SearchCriteriaInfo(criteriaToAdd));

                String criteriaName = criteriaToAdd.getCriteriaName();
                List<Object> values = criteriaToAdd.getCriteriaValues();

                if ("deviceConfiguration.deviceType.id".equals(criteriaName)) {
                    for (Object value : values) {
                        String deviceTypeName = (String) value;
                        Optional<DeviceType> deviceTypeOptional = deviceConfigurationService.findDeviceTypeByName(deviceTypeName);
                        if (deviceTypeOptional.isPresent()) {
                            deviceType = deviceTypeOptional.get();
                            deviceGroupInfo.deviceTypeIds.add(deviceType.getId());
                        }
                    }
                } else if ("deviceConfiguration.id".equals(criteriaName)) {
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
        } else {
            List<? extends EndDevice> endDevices = endDeviceGroup.getMembers(Instant.now());

            for (EndDevice endDevice : endDevices) {
                Device device = deviceService.findDeviceById(Long.parseLong(endDevice.getAmrId()));
                if (device != null) {
                    deviceGroupInfo.selectedDevices.add(device.getId());
                }
            }
        }
        return deviceGroupInfo;
    }

    public static List<DeviceGroupInfo> from(List<EndDeviceGroup> endDeviceGroups, DeviceConfigurationService deviceConfigurationService, DeviceService deviceService) {
        List<DeviceGroupInfo> deviceGroupsInfos = new ArrayList<>();
        for (EndDeviceGroup endDeviceGroup : endDeviceGroups) {
            deviceGroupsInfos.add(DeviceGroupInfo.from(endDeviceGroup, deviceConfigurationService, deviceService));
        }
        return deviceGroupsInfos;
    }


    static List<SearchCriteria> translateCriteria(List<SearchCriteria> criteria, DeviceConfigurationService deviceConfigurationService) {
        List<SearchCriteria> result = new ArrayList<>();
        for (SearchCriteria criterium : criteria) {
            String criteriaName = criterium.getCriteriaName();
            if ("deviceConfiguration.deviceType.id".equals(criteriaName)) {
                List<Object> values = criterium.getCriteriaValues();
                List<Object> newValues = new ArrayList<Object>();
                for (Object value : values) {
                    Optional<DeviceType> deviceTypeOptional = deviceConfigurationService.findDeviceType((int) value);
                    if (deviceTypeOptional.isPresent()) {
                        newValues.add(deviceTypeOptional.get().getName());
                    }
                }
                criterium.setCriteriaValues(newValues);
            } else if ("deviceConfiguration.id".equals(criteriaName)) {
                List<Object> values = criterium.getCriteriaValues();
                List<Object> newValues = new ArrayList<Object>();
                for (Object value : values) {
                    Optional<DeviceConfiguration> deviceConfigurationOptional = deviceConfigurationService.findDeviceConfiguration((int) value);
                    if (deviceConfigurationOptional.isPresent()) {
                        newValues.add(deviceConfigurationOptional.get().getName());
                    }
                }
                criterium.setCriteriaValues(newValues);
            }
            result.add(criterium);
        }
        return result;
    }

}
