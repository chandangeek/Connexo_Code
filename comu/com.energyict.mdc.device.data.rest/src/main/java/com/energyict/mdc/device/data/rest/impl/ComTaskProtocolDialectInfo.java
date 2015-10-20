package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ComTaskProtocolDialectInfo {
    @JsonProperty("protocolDialect")
    public String protocolDialect;

    public DeviceInfo device;
}
