package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdw.core.DeviceConfiguration;
import com.energyict.mdw.core.LogBookType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class DeviceConfigurationInfo {

    @JsonProperty("name")
    public String name;

    public DeviceConfigurationInfo(DeviceConfiguration deviceConfiguration) {
        name = deviceConfiguration.getName();
    }
    
}
