package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

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
    DeviceIdentifier getDeviceIdentifier();

}