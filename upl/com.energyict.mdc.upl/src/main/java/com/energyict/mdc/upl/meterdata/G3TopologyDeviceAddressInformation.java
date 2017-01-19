package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

/**
 * Straightforward ValueObject for the G3 deviceAddressInformation
 * <p>
 * Copyrights EnergyICT
 * Date: 1/13/15
 * Time: 12:11 PM
 */
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