/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLProtocolAdapter;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityCapabilities;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

@XmlRootElement
public class DeviceProtocolInfo {

    @JsonProperty(DeviceTypeInfo.COMMUNICATION_PROTOCOL_NAME)
    public String name;
    public boolean isLogicalSlave;
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
            isLogicalSlave = deviceProtocol.getDeviceProtocolCapabilities().size() == 1 && deviceProtocol.getDeviceProtocolCapabilities().get(0) == DeviceProtocolCapabilities.PROTOCOL_SLAVE;
            deviceFunction = deviceProtocol.getDeviceFunction();
            deviceProtocolSupportsClient = deviceProtocolSupportsClient(deviceProtocol);
            deviceProtocolSupportSecuritySuites = deviceProtocolSupportSecuritySuites(deviceProtocol);
        }
    }

    private boolean deviceProtocolSupportsClient(DeviceProtocol deviceProtocol) {
        return deviceProtocol.getClientSecurityPropertySpec() != null && deviceProtocol.getClientSecurityPropertySpec().isPresent();
    }

    private boolean deviceProtocolSupportSecuritySuites(DeviceProtocol deviceProtocol) {
        if (deviceProtocol instanceof UPLProtocolAdapter) {
            return ((UPLProtocolAdapter) deviceProtocol).getActual() instanceof AdvancedDeviceProtocolSecurityCapabilities;
        } else {
            return deviceProtocol instanceof AdvancedDeviceProtocolSecurityCapabilities;
        }
    }
}