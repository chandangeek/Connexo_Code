/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.calendar.Status;
import com.elster.jupiter.orm.DataModel;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.util.Optional;

/**
 * Created by igh on 21/04/2016.
 */
public class CalendarBuilderImpl implements CalendarService.CalendarBuilder {

    private DataModel dataModel;
    private CalendarImpl calendarImpl;

    public CalendarBuilderImpl(DataModel dataModel, EventSet eventSet) {
        this.dataModel = dataModel;
        this.calendarImpl = CalendarImpl.from(dataModel, eventSet);
        this.calendarImpl.setStatus(Status.INACTIVE);
    }

     CalendarBuilderImpl(DataModel dataModel, CalendarImpl calendarImpl) {
        this.dataModel = dataModel;
        this.calendarImpl = calendarImpl;
    }

    void init(String name, Year start) {
        this.calendarImpl.setName(name);
        this.calendarImpl.setStartYear(start);
    }

    @Override
    public CalendarService.CalendarBuilder category(Category category) {
        this.calendarImpl.setCategory(category);
        return this;
    }

    @Override
    public CalendarService.CalendarBuilder name(String name) {
        this.calendarImpl.setName(name);
        return this;
    }

    @Override
    public CalendarService.CalendarBuilder startYear(Year start) {
        this.calendarImpl.setStartYear(start);
        return this;
    }

    @Override
    public CalendarService.CalendarBuilder endYear(Year setStartYear) {
        this.calendarImpl.setEndYear(setStartYear);
        return this;
    }

    @Override
    public CalendarService.CalendarBuilder mRID(String mRID) {
        this.calendarImpl.setmRID(mRID);
        return this;
    }

    @Override
    public CalendarService.CalendarBuilder description(String description) {
        this.calendarImpl.setDescription(description);
        return this;
    }

    @Override
    public CalendarService.DayTypeBuilder newDayType(String name) {
        return new DayTypeBuilderImpl(dataModel, this, calendarImpl, name);
    }

    @Override
    public CalendarService.CalendarBuilder addPeriod(String name, String mondayDayTypeName, String tuesdayDayTypeName, String wednesdayDayTypeName, String thursdayDayTypeName, String fridayDayTypeName, String saturdayDayTypeName, String sundayDayTypeName) {
        this.calendarImpl.addPeriod(name,
                getDayType(mondayDayTypeName),
                getDayType(tuesdayDayTypeName),
                getDayType(wednesdayDayTypeName),
                getDayType(thursdayDayTypeName),
                getDayType(fridayDayTypeName),
                getDayType(saturdayDayTypeName),
                getDayType(sundayDayTypeName));
        return this;
    }

    @Override
    public CalendarService.TransitionBuilder on(MonthDay occurrence) {
        return new TransitionBuilderImpl(dataModel, this, calendarImpl, occurrence);
    }

    @Override
    public CalendarService.TransitionBuilder on(LocalDate occurrence) {
        return new TransitionBuilderImpl(dataModel, this, calendarImpl, occurrence);
    }

    @Override
    public CalendarService.ExceptionBuilder except(String dayTypeName) {
        return new ExceptionBuilderImpl(dataModel, this, calendarImpl, dayTypeName);
    }

    @Override
    public Calendar add() {
        this.calendarImpl.save();
        return this.calendarImpl;
    }

    private DayType getDayType(String name) {
        Optional<DayType> dayType =
                calendarImpl.getDayTypes().stream().filter(type -> type.getName().equals(name)).findAny();
        if (!dayType.isPresent()) {
            throw new IllegalArgumentException("No daytype defined yet with name '" + name + "'");
        }
        return dayType.get();
    }

}
