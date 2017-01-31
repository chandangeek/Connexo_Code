/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.FixedPeriodTransitionSpec;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Provides an implementation for the {@link com.elster.jupiter.calendar.FixedPeriodTransitionSpec} interface.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-04-19
 */
class FixedPeriodTransitionSpecImpl extends PeriodTransitionSpecImpl implements FixedPeriodTransitionSpec {

    static final String TYPE_IDENTIFIER = "FIX";

    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Integer year;

    @Inject
    FixedPeriodTransitionSpecImpl(ServerCalendarService calendarService) {
        super(calendarService);
    }

    @Override
    public LocalDate getOccurrence() {
        return LocalDate.of(year, getMonth(), getDay());
    }


    public FixedPeriodTransitionSpecImpl init(Calendar calendar, int day, int month, int year) {
        FixedPeriodTransitionSpecImpl fixedPeriodTransitionSpecImpl =
                (FixedPeriodTransitionSpecImpl) super.init(calendar, day, month);
        fixedPeriodTransitionSpecImpl.year = year;
        return fixedPeriodTransitionSpecImpl;
    }

}