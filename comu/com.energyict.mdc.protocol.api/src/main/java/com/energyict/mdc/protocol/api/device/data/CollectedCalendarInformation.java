/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import java.util.Optional;

/**
 * Models the actual calendar information collected from a device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-27 (08:48)
 */
public interface CollectedCalendarInformation {
    /**
     * Gets the name of the active calendar, if that information was collected.
     *
     * @return The name of the active calendar that was collected and if it was collected
     */
    Optional<String> getActiveCalendar();

    /**
     * Gets the name of the passive calendar, if that information was collected.
     *
     * @return The name of the passive calendar that was collected and if it was collected
     */
    Optional<String> getPassiveCalendar();

    /**
     * Tests if data was collected or not. If that is the case,
     * either {@link #getActiveCalendar()} or {@link #getPassiveCalendar()}
     * will return an non empty Optional.
     *
     * @return <code>false</code> iff data was data collected.
     */
    boolean isEmpty();

}