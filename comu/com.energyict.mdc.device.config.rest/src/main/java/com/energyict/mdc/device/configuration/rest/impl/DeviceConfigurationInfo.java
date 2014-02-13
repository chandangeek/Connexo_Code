package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdw.core.DeviceConfiguration;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class DeviceConfigurationInfo {

    @JsonProperty("name")
    public String name;

    public DeviceConfigurationInfo(DeviceConfiguration deviceConfiguration) {
        name = deviceConfiguration.getName();

    }
    
}
