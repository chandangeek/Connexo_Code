package com.elster.jupiter.calendar;

import aQute.bnd.annotation.ProviderType;

import java.time.LocalTime;

/**
 * Models the occurrence of an {@link Event} in a {@link DayType}.
 * An occurrence only specifies the start of the occurrence.
 * It will automatically end at the occurrence of the next Event
 * or at the occurrence of the first event (modulo wrap around midnight).
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-07 (12:59)
 */
@ProviderType
public interface EventOccurrence {

    Event getEvent();

    LocalTime getFrom();

}