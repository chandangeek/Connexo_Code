/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import aQute.bnd.annotation.ProviderType;

import java.time.LocalDate;

/**
 * Models an {@link ExceptionalOccurrence} that occurs only
 * at a very specific point in time. In other words
 * it is fixed on that point in time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-07 (14:06)
 */
@ProviderType
public interface FixedExceptionalOccurrence extends ExceptionalOccurrence {
    /**
     * Gets the fixed point in time in the TimeZone of the related {@link Calendar}.
     *
     * @return The fixed point in time
     */
    LocalDate getOccurrence();

}