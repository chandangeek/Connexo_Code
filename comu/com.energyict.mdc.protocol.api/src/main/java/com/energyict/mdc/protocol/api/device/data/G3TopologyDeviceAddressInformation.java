/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

public class G3TopologyDeviceAddressInformation {

    private final DeviceIdentifier deviceIdentifier;
    private final String formattedIPv6Address;
    private final int ipv6ShortAddress;
    private final int logicalDeviceId;

    public G3TopologyDeviceAddressInformation(DeviceIdentifier deviceIdentifier, String formattedIPv6Address, int ipv6ShortAddress, int logicalDeviceId) {
        this.deviceIdentifier = deviceIdentifier;
        this.formattedIPv6Address = formattedIPv6Address;
        this.ipv6ShortAddress = ipv6ShortAddress;
        this.logicalDeviceId = logicalDeviceId;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public String getFullIPv6Address() {
        return formattedIPv6Address;
    }

    public int getIpv6ShortAddress() {
        return ipv6ShortAddress;
    }

    public int getLogicalDeviceId() {
        return logicalDeviceId;
    }
}
