package com.energyict.mdc.device.topology;

import com.energyict.mdc.device.data.Device;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the path that was used while one {@link Device}
 * was communicating with another Device.
 * Such a path can be simple when the two Devices are directly connected.
 * In that case the number of hops will be zero.
 * In complex cases, many intermediate Devices may be involved in the communication.
 * Each intermediate Device retransmits the communication to the Device
 * that was known to be capable of communicating with the target device before.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-08 (13:54)
 */
@ProviderType
public interface CommunicationPath {

    /**
     * Gets the source of the CommunicationPath.
     * The source is the Device that initiated the communication
     * that was established via this CommunicationPath.
     *
     * @return The source Device
     */
    public Device getSource();

    /**
     * Gets the target of the CommunicationPath.
     * The target is the final destination of the communication,
     * i.e. the Device that was intented to receive the message
     * from the source Device.
     *
     * @return The target Device
     * @see #getSource()
     */
    public Device getTarget();

    /**
     * Gets the number of intermediate {@link com.energyict.mdc.device.data.Device}s
     * that are involved in this CommunicationPath.
     *
     * @return The number of intermediate Devices
     */
    public int getNumberOfHops();

}