package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonUnwrapped;

@XmlRootElement
public class DeviceConfigurationInfo {

    @JsonProperty("id")
    public long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("description")
    public String description;
    @JsonProperty("active")
    public Boolean active;
    @JsonProperty("loadProfileCount")
    public Integer loadProfileCount;
    @JsonProperty("registerCount")
    public Integer registerCount;
    @JsonProperty("logBookCount")
    public Integer logBookCount;
    @JsonProperty("canBeGateway")
    public Boolean canBeGateway;
    @JsonProperty("isDirectlyAddressable")
    public Boolean isDirectlyAddressable;
    @JsonUnwrapped // As requested by ExtJS people
    public DeviceProtocolInfo deviceProtocolInfo;
    @JsonProperty("deviceFunction")
    @XmlJavaTypeAdapter(DeviceFunctionAdapter.class)
    public DeviceFunction deviceFunction;

    public DeviceConfigurationInfo() {
    }

    public DeviceConfigurationInfo(DeviceConfiguration deviceConfiguration) {
        id = deviceConfiguration.getId();
        name = deviceConfiguration.getName();
        active = deviceConfiguration.isActive();
        description = deviceConfiguration.getDescription();
        loadProfileCount = deviceConfiguration.getLoadProfileSpecs().size();
        registerCount = deviceConfiguration.getRegisterSpecs().size();
        logBookCount = deviceConfiguration.getLogBookSpecs().size();
        canBeGateway = deviceConfiguration.canActAsGateway();
        isDirectlyAddressable = deviceConfiguration.canBeDirectlyAddressable();

        DeviceProtocolPluggableClass deviceProtocolPluggableClass = deviceConfiguration.getDeviceType().getDeviceProtocolPluggableClass();
        if (deviceProtocolPluggableClass!=null) {
            this.deviceProtocolInfo=new DeviceProtocolInfo(deviceProtocolPluggableClass);
            DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
            if (deviceProtocol!=null) {
                deviceFunction = deviceProtocol.getDeviceFunction();
            }
        }
    }

    public static List<DeviceConfigurationInfo> from(List<DeviceConfiguration> deviceConfigurations) {
        List<DeviceConfigurationInfo> infos = new ArrayList<>(deviceConfigurations.size());
        for (DeviceConfiguration deviceConfiguration : deviceConfigurations) {
            infos.add(new DeviceConfigurationInfo(deviceConfiguration));
        }
        return infos;
    }

    public void writeTo(DeviceConfiguration deviceConfiguration) {
        deviceConfiguration.setDescription(this.description);
        deviceConfiguration.setName(this.name);
        deviceConfiguration.setCanActAsGateway(this.canBeGateway);
        deviceConfiguration.setCanBeDirectlyAddressed(this.isDirectlyAddressable);
    }
}