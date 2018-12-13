/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import aQute.bnd.annotation.ProviderType;

import java.time.LocalDate;

/**
 * Models an effective transition from one {@link Period} to another.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-18 (10:44)
 */
@ProviderType
public interface PeriodTransition {
    /**
     * Gets the fixed point in time in the TimeZone of the related {@link Calendar}.
     *
     * @return The fixed point in time
     */
    LocalDate getOccurrence();

    /**
     * The {@link Period} to which is being transitioned.
     *
     * @return The Period to which is being transitioned
     */
    Period getPeriod();

}