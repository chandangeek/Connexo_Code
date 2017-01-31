/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.RecurrentPeriodTransitionSpec;

import javax.inject.Inject;
import java.time.MonthDay;

/**
 * Provides an implementation for the {@link com.elster.jupiter.calendar.RecurrentPeriodTransitionSpec} interface.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-04-19
 */
class RecurrentPeriodTransitionSpecImpl extends PeriodTransitionSpecImpl implements RecurrentPeriodTransitionSpec {

    static final String TYPE_IDENTIFIER = "REC";

    public RecurrentPeriodTransitionSpecImpl init(Calendar calendar, int day, int month) {
        return (RecurrentPeriodTransitionSpecImpl) super.init(calendar, day, month);
    }

    @Inject
    RecurrentPeriodTransitionSpecImpl(ServerCalendarService calendarService) {
        super(calendarService);
    }

    @Override
    public MonthDay getOccurrence() {
        return MonthDay.of(getMonth(), getDay());
    }

}