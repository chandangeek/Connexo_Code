/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.legacy;

import com.energyict.mdc.protocol.api.legacy.dynamic.Pluggable;
import com.energyict.mdc.upl.meterdata.BreakerStatus;

import java.io.IOException;
import java.util.Optional;

/**
 * Adds configuration support to the {@link com.energyict.mdc.upl.MeterProtocol} interface.
 * <p>
 * At configuration time, the getRequiredKeys, getOptionalKeys and setProperties methods
 * can be called in any sequence </p><p>
 * <p>
 * During normal operations the data collection system will first set the configured properties
 * and will then call the methods of the com.energyict.mdc.upl.MeterProtocol interface as defined.
 *
 * @author Karel
 *         KV 15122003 serialnumber of the device
 */
public interface MeterProtocol extends Pluggable, com.energyict.mdc.upl.cache.CachingProtocol, com.energyict.mdc.upl.MeterProtocol {

    /**
     * Gets the current status of the breaker<br/>
     * Note: if the {@link MeterProtocol} doesn't support breaker functionality (e.g. the device is
     * not equipped with a breaker), then {@link Optional#empty()} should be returned.
     *
     * @return the current status of the breaker
     * @throws IOException Thrown in case of an exception
     */
    default Optional<BreakerStatus> getBreakerStatus() throws IOException {
        return Optional.empty();
    }

    /**
     * Gets the name of the active calendar that is currently configured on the device.
     * Note: if the {@link MeterProtocol} doesn't support calendar functionality,
     * then {@link Optional#empty()} should be returned.
     *
     * @return The name of the active calendar
     * @throws IOException
     */
    default Optional<String> getActiveCalendarName() throws IOException {
        return Optional.empty();
    }

    /**
     * Gets the name of the passive calendar that is currently configured on the device.
     * Note: if the {@link MeterProtocol} doesn't support calendar functionality,
     * then {@link Optional#empty()} should be returned.
     *
     * @return The name of the passive calendar
     * @throws IOException
     */
    default Optional<String> getPassiveCalendarName() throws IOException {
        return Optional.empty();
    }
}