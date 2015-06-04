package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
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
    public boolean canBeDirectlyAddressed;
    public boolean canBeGateway;
    @JsonProperty(COMMUNICATION_PROTOCOL_NAME)
    public String deviceProtocolPluggableClassName;
    public long deviceProtocolPluggableClassId;
    @JsonProperty("registerTypes")
    public List<RegisterTypeInfo> registerTypes;
    public Long deviceLifeCycleId;
    public String deviceLifeCycleName;
    public long version;

    public DeviceTypeInfo() {
    }

    public static DeviceTypeInfo from(DeviceType deviceType, List<RegisterType> registerTypes) {
        DeviceTypeInfo deviceTypeInfo = from(deviceType);
        deviceTypeInfo.registerTypes = new ArrayList<>();
        for (MeasurementType measurementType : registerTypes) {
            deviceTypeInfo.registerTypes.add(new RegisterTypeInfo(measurementType, true, false));
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
        deviceTypeInfo.deviceConfigurationCount=deviceType.getConfigurations().size();
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = deviceType.getDeviceProtocolPluggableClass();
        deviceTypeInfo.canBeGateway= deviceType.canActAsGateway();
        deviceTypeInfo.canBeDirectlyAddressed = deviceType.isDirectlyAddressable();
        if (deviceProtocolPluggableClass!=null) {
            deviceTypeInfo.deviceProtocolPluggableClassName =deviceProtocolPluggableClass.getName();
            deviceTypeInfo.deviceProtocolPluggableClassId =deviceProtocolPluggableClass.getId();
        }
        DeviceLifeCycle deviceLifeCycle = deviceType.getDeviceLifeCycle();
        if (deviceLifeCycle != null) {
            deviceTypeInfo.deviceLifeCycleId = deviceLifeCycle.getId();
            deviceTypeInfo.deviceLifeCycleName = deviceLifeCycle.getName();
        }
        deviceTypeInfo.version = deviceType.getVersion();
        return deviceTypeInfo;
    }

    public static List<DeviceTypeInfo> from(List<DeviceType> deviceTypes) {
        List<DeviceTypeInfo> deviceTypeInfos = new ArrayList<>();
        for (DeviceType deviceType : deviceTypes) {
            deviceTypeInfos.add(DeviceTypeInfo.from(deviceType));
        }
        return deviceTypeInfos;
    }

}