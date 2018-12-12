/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import aQute.bnd.annotation.ProviderType;

import java.time.LocalDate;

/**
 * Models an {@link PeriodTransitionSpec} that occurs only
 * at a very specific point in time. In other words
 * it is fixed on that point in time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-18 (10:42)
 */
@ProviderType
public interface FixedPeriodTransitionSpec extends PeriodTransitionSpec {
    /**
     * Gets the fixed point in time in the TimeZone of the related {@link Calendar}.
     *
     * @return The fixed point in time
     */
    LocalDate getOccurrence();
}