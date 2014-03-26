package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonUnwrapped;

@XmlRootElement
public class DeviceTypeInfo {
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
    @JsonUnwrapped // As requested by ExtJS people
    public DeviceProtocolInfo deviceProtocolInfo;
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
            deviceTypeInfo.deviceProtocolInfo=new DeviceProtocolInfo(deviceProtocolPluggableClass);
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