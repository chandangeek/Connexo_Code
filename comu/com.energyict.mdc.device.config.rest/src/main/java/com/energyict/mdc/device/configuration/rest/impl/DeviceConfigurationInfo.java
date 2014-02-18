package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeviceConfigurationInfo {

    @JsonProperty("name")
    public String name;

    public DeviceConfigurationInfo(DeviceConfiguration deviceConfiguration) {
        name = deviceConfiguration.getName();

    }

}