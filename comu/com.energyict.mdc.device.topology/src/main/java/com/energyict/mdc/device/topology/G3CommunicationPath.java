package com.energyict.mdc.device.topology;

import com.energyict.mdc.device.data.Device;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * Models a {@link CommunicationPath} that is specific to the G3 case.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-08 (14:06)
 */
@ProviderType
public interface G3CommunicationPath extends CommunicationPath {

    /**
     * Gets the List of {@link Device}s that were involved
     * in this CommunicationPath.
     * Note that when the number of hops is zero,
     * this list of intermediate Devices will be empty.
     *
     * @return The List of intermediate Devices
     * @see #getNumberOfHops()
     */
    public List<Device> getIntermediateDevices();

}