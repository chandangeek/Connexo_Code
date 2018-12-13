/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.orm.DataModel;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Optional;

/**
 * Created by igh on 22/04/2016.
 */
public class TransitionBuilderImpl implements CalendarService.TransitionBuilder {

    private DataModel dataModel;
    private CalendarBuilderImpl calendarBuilderImpl;
    private CalendarImpl calendarImpl;
    private PeriodTransitionSpecImpl periodTransitionSpecImpl;

    public TransitionBuilderImpl(DataModel dataModel, CalendarBuilderImpl calendarBuilderImpl, CalendarImpl calendarImpl, MonthDay occurrence) {
        this.dataModel = dataModel;
        this.calendarBuilderImpl = calendarBuilderImpl;
        this.calendarImpl = calendarImpl;
        this.periodTransitionSpecImpl =
                this.dataModel.getInstance(RecurrentPeriodTransitionSpecImpl.class)
                        .init(calendarImpl, occurrence.getDayOfMonth(), occurrence.getMonthValue());
    }

    public TransitionBuilderImpl(DataModel dataModel, CalendarBuilderImpl calendarBuilderImpl, CalendarImpl calendarImpl, LocalDate occurrence) {
        this.dataModel = dataModel;
        this.calendarBuilderImpl = calendarBuilderImpl;
        this.calendarImpl = calendarImpl;
        this.periodTransitionSpecImpl =
                this.dataModel.getInstance(FixedPeriodTransitionSpecImpl.class)
                        .init(calendarImpl, occurrence.getDayOfMonth(), occurrence.getMonthValue(), occurrence.getYear());
    }

    @Override
    public CalendarService.CalendarBuilder transitionTo(String name) {
        Optional<Period> period =
                calendarImpl.getPeriods().stream().filter(p -> p.getName().equals(name)).findAny();
        if (!period.isPresent()) {
            throw new IllegalArgumentException("No period defined yet with name '" + name + "'");
        }
        this.periodTransitionSpecImpl.setPeriod(period.get());
        calendarImpl.addPeriodTransitionSpec(this.periodTransitionSpecImpl);
        return calendarBuilderImpl;
    }
}
