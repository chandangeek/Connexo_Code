/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;

import java.time.LocalDate;

/**
 * Provides an implementation for the {@link com.elster.jupiter.calendar.PeriodTransition} interface.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-04-21
 */
class PeriodTransitionImpl implements PeriodTransition {

    private LocalDate occurrence;
    private Period period;

    PeriodTransitionImpl(LocalDate occurrence, Period period) {
        this.occurrence = occurrence;
        this.period = period;
    }

    @Override
    public LocalDate getOccurrence() {
        return occurrence;
    }

    @Override
    public Period getPeriod() {
        return period;
    }

}