/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.orm.DataModel;

/**
 * Created by igh on 21/04/2016.
 */
public class DayTypeBuilderImpl implements CalendarService.DayTypeBuilder {

    private DataModel dataModel;
    private CalendarBuilderImpl calendarBuilderImpl;
    private CalendarImpl calendarImpl;
    private DayTypeImpl dayTypeImpl;

    public DayTypeBuilderImpl(DataModel dataModel, CalendarBuilderImpl calendarBuilderImpl, CalendarImpl calendarImpl, String name) {
        this.dataModel = dataModel;
        this.calendarBuilderImpl = calendarBuilderImpl;
        this.calendarImpl = calendarImpl;
        this.dayTypeImpl = this.dataModel.getInstance(DayTypeImpl.class).init(calendarImpl, name);
    }

    @Override
    public CalendarService.DayTypeEventOccurrenceBuilder event(String eventName) {
        Event event = calendarImpl.getEvents()
                .stream()
                .filter(evt -> evt.getName().equals(eventName))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No event defined yet with name '" + eventName + "'"));
        return new DayTypeEventOccurrenceBuilderImpl(dataModel, this, dayTypeImpl, event);
    }

    @Override
    public CalendarService.DayTypeEventOccurrenceBuilder eventWithCode(long code) {
        Event event = calendarImpl.getEvents()
                .stream()
                .filter(evt -> evt.getCode() == code)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No event defined yet with code '" + code + "'"));
        return new DayTypeEventOccurrenceBuilderImpl(dataModel, this, dayTypeImpl, event);
    }

    @Override
    public CalendarService.CalendarBuilder add() {
        calendarImpl.addDayType(dayTypeImpl);
        return calendarBuilderImpl;
    }


}
