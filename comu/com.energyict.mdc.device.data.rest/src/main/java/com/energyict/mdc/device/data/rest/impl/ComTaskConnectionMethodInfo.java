package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties
public class ComTaskConnectionMethodInfo {
    @JsonProperty("connectionMethod")
    public String connectionMethod;

    public DeviceInfo device;
}
