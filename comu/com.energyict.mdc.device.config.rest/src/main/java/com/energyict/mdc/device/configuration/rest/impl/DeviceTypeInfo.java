package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class DeviceTypeInfo {
    public static final String COMMUNICATION_PROTOCOL_NAME = "communicationProtocolName";

    @JsonProperty("id")
    public long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("loadProfileCount")
    public int loadProfileCount;
    @JsonProperty("registerCount")
    public int registerCount;
    @JsonProperty("logBookCount")
    public int logBookCount;
    @JsonProperty("deviceConfigurationCount")
    public int deviceConfigurationCount;
    @JsonProperty("canBeDirectlyAddressed")
    public boolean canBeDirectlyAddressed;
    @JsonProperty("canBeGateway")
    public boolean canBeGateway;
    @JsonProperty(COMMUNICATION_PROTOCOL_NAME)
    public String communicationProtocolName;
    @JsonProperty("registerTypes")
    public List<RegisterMappingInfo> registerMappings;

    public DeviceTypeInfo() {
    }

    public static DeviceTypeInfo from(DeviceType deviceType, List<RegisterMapping> registerMappings) {
        DeviceTypeInfo deviceTypeInfo = from(deviceType);
        deviceTypeInfo.registerMappings = new ArrayList<>();
        for (RegisterMapping registerMapping : registerMappings) {
            deviceTypeInfo.registerMappings.add(new RegisterMappingInfo(registerMapping));
        }
        return deviceTypeInfo;
    }
    
    public static DeviceTypeInfo from(DeviceType deviceType) {
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.id=deviceType.getId();
        deviceTypeInfo.name=deviceType.getName();
        deviceTypeInfo.loadProfileCount = deviceType.getLoadProfileTypes().size();
        deviceTypeInfo.registerCount=deviceType.getRegisterMappings().size();
        deviceTypeInfo.logBookCount=deviceType.getLogBookTypes().size();
        deviceTypeInfo.deviceConfigurationCount=deviceType.getConfigurations().size();
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = deviceType.getDeviceProtocolPluggableClass();
        deviceTypeInfo.canBeGateway= deviceType.canActAsGateway();
        deviceTypeInfo.canBeDirectlyAddressed = deviceType.isDirectlyAddressable();
        if (deviceProtocolPluggableClass!=null) {
            deviceTypeInfo.communicationProtocolName=deviceProtocolPluggableClass.getName();
        }
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