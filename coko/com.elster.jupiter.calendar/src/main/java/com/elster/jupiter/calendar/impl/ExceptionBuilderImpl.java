/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.orm.DataModel;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Optional;

/**
 * Created by igh on 22/04/2016.
 */
public class ExceptionBuilderImpl implements CalendarService.ExceptionBuilder {

    private DataModel dataModel;
    private CalendarBuilderImpl calendarBuilderImpl;
    private CalendarImpl calendarImpl;
    private ExceptionalOccurrenceImpl exceptionalOccurrenceImpl;
    private DayTypeImpl dayTypeImpl;

    public ExceptionBuilderImpl(DataModel dataModel, CalendarBuilderImpl calendarBuilderImpl, CalendarImpl calendarImpl, String dayTypeName) {
        this.dataModel = dataModel;
        this.calendarBuilderImpl = calendarBuilderImpl;
        this.calendarImpl = calendarImpl;
        Optional<DayType> dayType =
                calendarImpl.getDayTypes().stream().filter(type -> type.getName().equals(dayTypeName)).findAny();
        if (!dayType.isPresent()) {
            throw new IllegalArgumentException("No dayType defined yet with name '" + dayTypeName + "'");
        }
        this.dayTypeImpl = (DayTypeImpl) dayType.get();
    }


    @Override
    public CalendarService.ExceptionBuilder occursOnceOn(LocalDate date) {
        this.calendarImpl.addFixedExceptionalOccurrence(this.dayTypeImpl, date);
        return this;
    }

    @Override
    public CalendarService.ExceptionBuilder occursAlwaysOn(MonthDay recurringDay) {
        this.calendarImpl.addRecurrentExceptionalOccurrence(this.dayTypeImpl, recurringDay);
        return this;
    }

    @Override
    public CalendarService.CalendarBuilder add() {
        return calendarBuilderImpl;
    }
}
