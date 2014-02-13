package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeviceProtocolInfo {

    @JsonProperty("communicationProtocolName")
    public String name;

    public DeviceProtocolInfo() {
    }

    public DeviceProtocolInfo(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        this.name = deviceProtocolPluggableClass.getName();
    }
}
