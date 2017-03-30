/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeviceProtocolInfo {

    @JsonProperty(DeviceTypeInfo.COMMUNICATION_PROTOCOL_NAME)
    public String name;

    public DeviceProtocolInfo() {
    }

    public DeviceProtocolInfo(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        this.name = deviceProtocolPluggableClass.getName();
    }
}
