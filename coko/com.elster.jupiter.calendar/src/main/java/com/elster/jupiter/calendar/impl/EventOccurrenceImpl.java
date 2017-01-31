/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalTime;

/**
 * Provides an implementation for the {@link EventOccurrence} interface.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-04-20
 */
class EventOccurrenceImpl implements EventOccurrence {

    public enum Fields {
        ID("id"),
        HOURS("hours"),
        MINUTES("minutes"),
        SECONDS("seconds"),
        EVENT("event"),
        DAYTYPE("dayType");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Integer hours;
    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Integer minutes;
    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Integer seconds;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<DayType> dayType = ValueReference.absent();
    private Reference<Event> event = ValueReference.absent();

    EventOccurrenceImpl init(Event event, DayType dayType) {
        this.event.set(event);
        this.dayType.set(dayType);
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Event getEvent() {
        return this.event.orNull();
    }

    @Override
    public LocalTime getFrom() {
        return LocalTime.of(hours, minutes, seconds);
    }

    @Override
    public DayType getDayType() {
        return this.dayType.orNull();
    }


    void setFrom(LocalTime from) {
        if (from != null) {
            this.hours = from.getHour();
            this.minutes = from.getMinute();
            this.seconds = from.getSecond();
        }
    }

    void setEvent(Event event) {
        this.event.set(event);
    }
}
