package com.elster.jupiter.calendar;

import aQute.bnd.annotation.ProviderType;

import java.time.MonthDay;

/**
 * Models an {@link PeriodTransitionSpec} that occurs multiple times
 * like a person's birthday or a national holiday.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-07 (14:10)
 */
@ProviderType
public interface RecurringPeriodTransitionSpec extends PeriodTransitionSpec {
    MonthDay getOccurrence();
}