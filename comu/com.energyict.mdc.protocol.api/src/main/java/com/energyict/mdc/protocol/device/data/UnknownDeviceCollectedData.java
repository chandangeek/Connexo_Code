package com.energyict.mdc.protocol.device.data;

import com.energyict.mdc.protocol.inbound.DeviceIdentifier;

/**
 * Models the situation in which data is collected, for an unknown Device.
 *
 * @author sva
 * @since 13/12/12 - 16:28
 */

public interface UnknownDeviceCollectedData extends CollectedData {

    /**
     * Getter for the {@link DeviceIdentifier} for this unknown device.
     *
     * @return the {@link DeviceIdentifier deviceIdentifier}
     */
    public DeviceIdentifier getDeviceIdentifier();

}