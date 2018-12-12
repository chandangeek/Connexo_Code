/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.calendar.Calendar;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Optional;

@ProviderType
public interface AllowedCalendar {

    /**
     * Checks if the calendar is available in the system
     * @return the ghost status of the calendar
     */
    boolean isGhost();

    /**
     * Gets the name of the calendar
     * @return the name of the calendar
     */
    String getName();

    long getId();

    Optional<Calendar> getCalendar();

    boolean isObsolete();

    void setObsolete(Instant instant);

    Instant getObsolete();

}
