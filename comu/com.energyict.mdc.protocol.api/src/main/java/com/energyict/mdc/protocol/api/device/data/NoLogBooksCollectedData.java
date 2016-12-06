package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;

/**
 * Models the situation in which events are collected, for a device uniquely identified by its {@link DeviceIdentifier},
 * but which has no {@link com.energyict.mdw.core.LogBook LogBooks} configured.
 *
 * @author sva
 * @since 14/12/12 - 9:11
 */

public interface NoLogBooksCollectedData extends CollectedLogBook {

    /**
     * Getter for the {@link DeviceIdentifier} for the device.
     *
     * @return the {@link DeviceIdentifier deviceIdentifier}
     */
    public DeviceIdentifier getDeviceIdentifier();

}
