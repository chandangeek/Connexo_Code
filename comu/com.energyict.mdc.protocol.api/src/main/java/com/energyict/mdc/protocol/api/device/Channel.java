package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.CanGoOffline;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;

import java.math.BigDecimal;

/**
 * Represents a single load profile on a data logger or energy meter.
 * Channel objects are created automatically when an Device is created.
 */

public interface Channel extends CanGoOffline<OfflineLoadProfileChannel> {

    /**
     * Returns the device the receiver belongs to.
     *
     * @return the receiver's device.
     */
    BaseDevice getDevice();

    /**
     * Returns the configured interval in seconds.
     * Equivalent to getRtu().getIntervalInSeconds().
     *
     * @return the interval in seconds.
     */
    int getIntervalInSeconds();

    /**
     * Returns the receiver's configured unit.
     * Equivalent to getPhenomenon().getUnit().
     *
     * @return the configured unit.
     */
    Unit getUnit();

    /**
     * Returns the LoadProfile associated with this channel, null if no LoadProfile associated
     *
     * @return the LoadProfile
     */
    LoadProfile getLoadProfile();

    ObisCode getRegisterTypeObisCode();
}
