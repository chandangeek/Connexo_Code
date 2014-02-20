package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.device.config.DeviceType;
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

    public DeviceTypeInfo(DeviceType deviceType) {
        this.id=deviceType.getId();
        this.name=deviceType.getName();
        this.loadProfileCount = deviceType.getLoadProfileTypes().size();
        this.registerCount=deviceType.getRegisterMappings().size();
        this.logBookCount=deviceType.getLogBookTypes().size();
        this.deviceConfigurationCount=deviceType.getConfigurations().size();
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = deviceType.getDeviceProtocolPluggableClass();
        if (deviceProtocolPluggableClass!=null) {
            this.deviceProtocolInfo=new DeviceProtocolInfo(deviceProtocolPluggableClass);
            DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
            if (deviceProtocol!=null) {
                List<DeviceProtocolCapabilities> deviceProtocolCapabilities = deviceProtocol.getDeviceProtocolCapabilities();
                if (deviceProtocolCapabilities!=null) {
                    this.canBeGateway= deviceProtocolCapabilities.contains(DeviceProtocolCapabilities.PROTOCOL_MASTER);
                    this.canBeDirectlyAddressed = deviceProtocolCapabilities.contains(DeviceProtocolCapabilities.PROTOCOL_SESSION);
                }
            }
        }
    }

    public DeviceTypeInfo(DeviceType deviceType, List<RegisterMapping> registerMappings) {
        this(deviceType);
        for (RegisterMapping registerMapping : registerMappings) {
            this.registerMappings.add(new RegisterMappingInfo(registerMapping));
        }
    }

}