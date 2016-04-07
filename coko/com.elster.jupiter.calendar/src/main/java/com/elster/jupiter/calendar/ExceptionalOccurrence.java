package com.elster.jupiter.calendar;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Models an exception to the standard occurrences of {@link Event}s
 * as defined by a {@link Period}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-07 (14:02)
 */
@ProviderType
public interface ExceptionalOccurrence {

    DayType getDayType();

    boolean occursAt(Instant instant);

}