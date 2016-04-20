package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.LocalTime;

/**
 * Created by igh on 20/04/2016.
 */
public class EventOccurrenceImpl implements EventOccurrence {

    public enum Fields {
        ID("id"),
        HOURS("hours"),
        MINUTES("minutes"),
        SECONDS("seconds"),
        EVENT("event"),
        DAYTYPE("daytype");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private long id;
    private int hours;
    private int minutes;
    private int seconds;

    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<DayType> dayType = ValueReference.absent();

    private final CalendarService calendarService;

    @Inject
    EventOccurrenceImpl(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public EventOccurrenceImpl init(DayType dayType, int hours, int minutes, int seconds) {
        this.dayType.set(dayType);
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Event getEvent() {
        return null;
    }

    @Override
    public LocalTime getFrom() {
        return LocalTime.of(hours, minutes, seconds);
    }

    @Override
    public DayType getDayType() {
        return this.dayType.orNull();
    }
}
