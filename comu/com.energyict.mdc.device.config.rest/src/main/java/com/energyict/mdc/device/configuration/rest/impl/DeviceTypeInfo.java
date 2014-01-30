package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ServiceKind;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdw.core.DeviceType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class DeviceTypeInfo {

    @JsonProperty("name")
    private String name;
    @JsonProperty("communicationProtocol")
    private DeviceProtocolInfo deviceProtocolInfo;
    @JsonProperty("loadProfileCount")
    private int loadProfileCount;
    @JsonProperty("registerCount")
    private int registerCount;
    @JsonProperty("logBookCount")
    private int logBookCount;
    @JsonProperty("deviceConfigurationCount")
    private int deviceConfigurationCount;
    @JsonProperty("isDirectlyAddressable")
    private boolean isDirectlyAddressable;
    @JsonProperty("canBeGateway")
    private boolean canBeGateway;

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
