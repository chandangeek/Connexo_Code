/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.ActiveEffectiveCalendar;

import java.time.Instant;

/**
 * Adds behavior to {@link ActiveEffectiveCalendar} that is reserved
 * for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-06 (12:02)
 */
public interface ServerActiveEffectiveCalendar extends ActiveEffectiveCalendar {

    /**
     * Closes this ActiveEffectiveCalendar so that it is
     * only effective until the specified closing Date.
     *
     * @param closingDate The closing Date
     */
    void close(Instant closingDate);

}