package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class DeviceTypeInfo {
    @JsonIgnore
    static final String COMMUNICATION_PROTOCOL_NAME = "communicationProtocolName";

    public long id;
    public String name;
    public int loadProfileCount;
    public int registerCount;
    public int logBookCount;
    public int deviceConfigurationCount;
    public boolean canBeDirectlyAddressed;
    public boolean canBeGateway;
    @JsonProperty(COMMUNICATION_PROTOCOL_NAME)
    public String communicationProtocolName;
    public long communicationProtocolId;
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
            deviceTypeInfo.communicationProtocolId =deviceProtocolPluggableClass.getId();
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