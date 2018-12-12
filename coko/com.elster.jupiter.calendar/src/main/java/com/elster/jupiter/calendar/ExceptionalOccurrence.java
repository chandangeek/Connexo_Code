/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.HasId;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Models an exception to the standard occurrences of {@link Event}s
 * as defined by a {@link Period}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-07 (14:02)
 */
@ProviderType
public interface ExceptionalOccurrence extends HasId {

    Calendar getCalendar();

    DayType getDayType();

    boolean occursAt(LocalDate localDate);

}