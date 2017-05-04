/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceTypePurpose;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfoFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class DeviceTypeInfo {
    @JsonIgnore
    static final String COMMUNICATION_PROTOCOL_NAME = "deviceProtocolPluggableClass";

    public long id;
    public String name;
    public int loadProfileCount;
    public int registerCount;
    public int logBookCount;
    public int deviceConfigurationCount;
    public int activeDeviceConfigurationCount;
    public long deviceConflictsCount;
    public boolean canBeDirectlyAddressed;
    public boolean canBeGateway;
    @JsonProperty(COMMUNICATION_PROTOCOL_NAME)
    public String deviceProtocolPluggableClassName;
    public long deviceProtocolPluggableClassId;
    @JsonProperty("registerTypes")
    public List<RegisterTypeInfo> registerTypes;
    public Long deviceLifeCycleId;
    public String deviceLifeCycleName;
    @JsonProperty(required=false)
    public PeriodInfo deviceLifeCycleEffectiveTimeShiftPeriod;
    public long version;
    public String deviceTypePurpose;
    public boolean fileManagementEnabled;

    public DeviceTypeInfo() {
    }

    public static DeviceTypeInfo from(DeviceType deviceType, List<RegisterType> registerTypes, RegisterTypeInfoFactory registerTypeInfoFactory) {
        DeviceTypeInfo deviceTypeInfo = from(deviceType);
        deviceTypeInfo.registerTypes = new ArrayList<>();
        for (MeasurementType measurementType : registerTypes) {
            deviceTypeInfo.registerTypes.add(registerTypeInfoFactory.asInfo(measurementType, true, false));
        }
        return deviceTypeInfo;
    }

    public static DeviceTypeInfo from(DeviceType deviceType) {
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.id=deviceType.getId();
        deviceTypeInfo.name=deviceType.getName();
        deviceTypeInfo.loadProfileCount = deviceType.getLoadProfileTypes().size();
        deviceTypeInfo.registerCount=deviceType.getRegisterTypes().size();
        deviceTypeInfo.logBookCount=deviceType.getLogBookTypes().size();
        List<DeviceConfiguration> configurations = deviceType.getConfigurations();
        deviceTypeInfo.deviceConfigurationCount = configurations.size();
        deviceTypeInfo.activeDeviceConfigurationCount = (int) configurations.stream().filter(DeviceConfiguration::isActive).count();
        deviceTypeInfo.deviceConflictsCount=deviceType.getDeviceConfigConflictMappings().stream().filter(f -> !f.isSolved()).count();
        deviceTypeInfo.canBeGateway= deviceType.canActAsGateway();
        deviceTypeInfo.canBeDirectlyAddressed = deviceType.isDirectlyAddressable();
        deviceType.getDeviceProtocolPluggableClass().ifPresent(deviceProtocolPluggableClass -> {
            deviceTypeInfo.deviceProtocolPluggableClassName =deviceProtocolPluggableClass.getName();
            deviceTypeInfo.deviceProtocolPluggableClassId =deviceProtocolPluggableClass.getId();
        });
        DeviceLifeCycle deviceLifeCycle = deviceType.getDeviceLifeCycle();
        if (deviceLifeCycle != null) {
            deviceTypeInfo.deviceLifeCycleId = deviceLifeCycle.getId();
            deviceTypeInfo.deviceLifeCycleName = deviceLifeCycle.getName();
            deviceTypeInfo.deviceLifeCycleEffectiveTimeShiftPeriod = new PeriodInfo(deviceLifeCycle.getMaximumPastEffectiveTimestamp().toEpochMilli(), deviceLifeCycle.getMaximumFutureEffectiveTimestamp().toEpochMilli());
        }
        deviceTypeInfo.version = deviceType.getVersion();
        deviceTypeInfo.deviceTypePurpose = deviceType.isDataloggerSlave() ? DeviceTypePurpose.DATALOGGER_SLAVE.name() : (deviceType.isMultiElementSlave() ? DeviceTypePurpose.MULTI_ELEMENT_SLAVE.name() : DeviceTypePurpose.REGULAR.name());
        deviceTypeInfo.fileManagementEnabled = deviceType.isFileManagementEnabled();
        return deviceTypeInfo;
    }

    public static List<DeviceTypeInfo> from(List<DeviceType> deviceTypes) {
        List<DeviceTypeInfo> deviceTypeInfos = new ArrayList<>();
        for (DeviceType deviceType : deviceTypes) {
            deviceTypeInfos.add(DeviceTypeInfo.from(deviceType));
        }
        return deviceTypeInfos;
    }

    @XmlType
    public static class PeriodInfo {
        public long start;
        public long end;

        public PeriodInfo(){}

        PeriodInfo(long start, long end){
            this.start = start;
            this.end = end;
        }
    }

}