package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.orm.DataModel;

import java.util.Optional;

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
        Optional<Event> event =
                calendarImpl.getEvents().stream().filter(evt -> evt.getName().equals(eventName)).findAny();
        if (!event.isPresent()) {
            throw new IllegalArgumentException("No event defined yet with name '" + eventName + "'");
        }
        return new DayTypeEventOccurrenceBuilderImpl(dataModel, this, dayTypeImpl, event.get());
    }

    @Override
    public CalendarService.DayTypeEventOccurrenceBuilder eventWithCode(int code) {
        Optional<Event> event =
                calendarImpl.getEvents().stream().filter(evt -> evt.getCode() == code).findAny();
        if (!event.isPresent()) {
            throw new IllegalArgumentException("No event defined yet with code '" + code + "'");
        }
        return new DayTypeEventOccurrenceBuilderImpl(dataModel, this, dayTypeImpl, event.get());
    }

    @Override
    public CalendarService.CalendarBuilder add() {
        calendarImpl.addDayType(dayTypeImpl);
        return calendarBuilderImpl;
    }


}
