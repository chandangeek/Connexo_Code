package com.energyict.mdc.device.data;

import com.energyict.mdc.protocol.api.device.BaseChannel;

/**
 * Copyrights EnergyICT
 * Date: 3/14/14
 * Time: 11:43 AM
 */
public interface Channel extends BaseChannel{

    /**
     * Returns the configured interval in seconds.
     * Equivalent to getRtu().getIntervalInSeconds().
     *
     * @return the interval in seconds.
     */
    int getIntervalInSeconds();
}
