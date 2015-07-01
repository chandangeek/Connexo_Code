package com.energyict.mdc.device.topology;

import com.energyict.mdc.device.data.Device;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.orm.associations.Effectivity;

import java.net.InetAddress;

/**
 * Models information that addresses a {@link Device}
 * for the Powerline Carrier/G3 technology.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-17 (09:03)
 */
@ProviderType
public interface G3DeviceAddressInformation extends Effectivity {

    /**
     * Gets the {@link Device} for which PLC/G3
     * identification information is provided.
     *
     * @return The Device
     */
    public Device getDevice();

    /**
     * Gets the {@link Device}'s IPv6 address.
     *
     * @return The IPv6 address
     */
    public InetAddress getIPv6Address();

    /**
     * Gets the short version of the {@link Device}'s IPv6 address.
     * Some technical people will refer to this as 6LoWPAN.
     *
     * @return The IPv6 short address
     */
    public int getIpv6ShortAddress();

    /**
     * Gets the identifier that was assigned by the
     * {@link Device}'s gateway and is used when
     * communicating with the gateway about the Device.
     *
     * @return The logical device identifier
     */
    public int getLogicalDeviceId();

}