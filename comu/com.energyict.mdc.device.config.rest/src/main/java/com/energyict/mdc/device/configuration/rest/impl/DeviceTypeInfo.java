package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdw.core.DeviceType;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonUnwrapped;

@XmlRootElement
public class DeviceTypeInfo {

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
    @JsonProperty("isDirectlyAddressable")
    public boolean isDirectlyAddressable;
    @JsonProperty("canBeGateway")
    public boolean canBeGateway;
    @JsonUnwrapped // As requested by ExtJS people
    public DeviceProtocolInfo deviceProtocolInfo;

    public DeviceTypeInfo() {
    }

    public DeviceTypeInfo(DeviceType deviceType) {
        this.name=deviceType.getName();
        this.loadProfileCount = deviceType.getLoadProfileTypes().size();
        this.registerCount=deviceType.getRegisterMappings().size();
        this.logBookCount=deviceType.getLogBookTypes().size();
        this.deviceConfigurationCount=deviceType.getConfigurations().size();
        this.isDirectlyAddressable=deviceType.getDeviceProtocolPluggableClass().getDeviceProtocol().getDeviceProtocolCapabilities().contains(DeviceProtocolCapabilities.PROTOCOL_SESSION);
        this.canBeGateway=deviceType.getDeviceProtocolPluggableClass().getDeviceProtocol().getDeviceFunction().equals(DeviceFunction.GATEWAY);
        this.deviceProtocolInfo=new DeviceProtocolInfo(deviceType.getDeviceProtocolPluggableClass());
    }
}
