/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.orm.DataModel;

import java.time.LocalTime;

/**
 * Created by igh on 22/04/2016.
 */
public class DayTypeEventOccurrenceBuilderImpl implements CalendarService.DayTypeEventOccurrenceBuilder {

    private DataModel dataModel;
    private DayTypeBuilderImpl dayTypeBuilderImpl;
    private DayTypeImpl dayTypeImpl;
    private EventOccurrenceImpl eventOccurrenceImpl;

    public DayTypeEventOccurrenceBuilderImpl(DataModel dataModel, DayTypeBuilderImpl dayTypeBuilderImpl, DayTypeImpl dayTypeImpl, Event event) {
        this.dataModel = dataModel;
        this.dayTypeBuilderImpl = dayTypeBuilderImpl;
        this.dayTypeImpl = dayTypeImpl;
        this.eventOccurrenceImpl = this.dataModel.getInstance(EventOccurrenceImpl.class).init(event, dayTypeImpl);
    }

    @Override
    public CalendarService.DayTypeBuilder startsFrom(LocalTime localTime) {
        eventOccurrenceImpl.setFrom(localTime);
        dayTypeImpl.addEventOccurrence(eventOccurrenceImpl);
        return dayTypeBuilderImpl;
    }
}
