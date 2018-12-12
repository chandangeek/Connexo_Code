/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnectionTaskProtocolDialectInfo {
    @JsonProperty("protocolDialect")
    public String protocolDialect;

    public DeviceInfo device;
}
