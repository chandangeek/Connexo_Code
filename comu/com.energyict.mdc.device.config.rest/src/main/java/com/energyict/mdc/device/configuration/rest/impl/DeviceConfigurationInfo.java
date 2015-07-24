package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.configuration.rest.GatewayTypeAdapter;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class DeviceConfigurationInfo {

    public long id;
    public String name;
    public String description;
    public Boolean active;
    public Integer loadProfileCount;
    public Integer registerCount;
    public Integer logBookCount;
    public Boolean canBeGateway;
    public Boolean isDirectlyAddressable;
    public long version;
    @XmlJavaTypeAdapter(GatewayTypeAdapter.class)
    public GatewayType gatewayType;
    @JsonUnwrapped // As requested by ExtJS people
    public DeviceProtocolInfo deviceProtocolInfo;
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
        gatewayType = deviceConfiguration.getGetwayType();
        isDirectlyAddressable = deviceConfiguration.isDirectlyAddressable();
        version = deviceConfiguration.getVersion();

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
        deviceConfiguration.setGatewayType(this.gatewayType);
        if (this.canBeGateway!=null) {
            deviceConfiguration.setCanActAsGateway(this.canBeGateway);
        }
        if (this.isDirectlyAddressable!=null) {
            deviceConfiguration.setDirectlyAddressable(this.isDirectlyAddressable);
        }
    }
}