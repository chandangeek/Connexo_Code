/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AdvancedDeviceProtocolSecurityCapabilities;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
public class DeviceProtocolInfo {

    @JsonProperty(DeviceTypeInfo.COMMUNICATION_PROTOCOL_NAME)
    public String name;
    @XmlJavaTypeAdapter(DeviceFunctionAdapter.class)
    public DeviceFunction deviceFunction;

    public boolean deviceProtocolSupportsClient;
    public boolean deviceProtocolSupportSecuritySuites;

    public DeviceProtocolInfo() {
    }

    public DeviceProtocolInfo(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        this.name = deviceProtocolPluggableClass.getName();
        DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
        if (deviceProtocol != null) {
            deviceFunction = deviceProtocol.getDeviceFunction();
            deviceProtocolSupportsClient = deviceProtocolSupportsClient(deviceProtocol);
            deviceProtocolSupportSecuritySuites = deviceProtocolSupportSecuritySuites(deviceProtocol);
        }
    }

    private boolean deviceProtocolSupportsClient(DeviceProtocol deviceProtocol) {
        return deviceProtocol.getClientSecurityPropertySpec() != null && deviceProtocol.getClientSecurityPropertySpec().isPresent();
    }

    private boolean deviceProtocolSupportSecuritySuites(DeviceProtocol deviceProtocol) {
        return deviceProtocol instanceof AdvancedDeviceProtocolSecurityCapabilities;
    }
}