package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;

import java.time.LocalDate;

/**
 * Created by igh on 21/04/2016.
 */
public class PeriodTransitionImpl implements PeriodTransition {

    private LocalDate occurrence;
    private Period period;

    public PeriodTransitionImpl(LocalDate occurrence, Period period) {
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
