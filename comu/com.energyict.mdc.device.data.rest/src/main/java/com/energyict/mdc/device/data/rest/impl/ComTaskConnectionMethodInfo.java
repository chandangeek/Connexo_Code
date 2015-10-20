package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ComTaskConnectionMethodInfo {
    @JsonProperty("connectionMethod")
    public String connectionMethod;

    public DeviceInfo device;
}
