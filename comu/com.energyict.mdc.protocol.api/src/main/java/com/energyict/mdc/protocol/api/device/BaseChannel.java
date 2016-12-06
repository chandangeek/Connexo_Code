package com.energyict.mdc.protocol.api.device;

import com.energyict.obis.ObisCode;
import com.energyict.cbo.Unit;

/**
 * Represents a single load profile on a data logger or energy meter.
 * Channel objects are created automatically when an Device is created.
 */

public interface BaseChannel {

    /**
     * Returns the device the receiver belongs to.
     *
     * @return the receiver's device.
     */
    BaseDevice getDevice();

    /**
     * Returns the receiver's configured unit.
     * @return the configured unit.
     */
    Unit getUnit();

    /**
     * Returns the LoadProfile associated with this channel, null if no LoadProfile associated
     *
     * @return the LoadProfile
     */
    BaseLoadProfile getLoadProfile();

    ObisCode getRegisterTypeObisCode();
}
