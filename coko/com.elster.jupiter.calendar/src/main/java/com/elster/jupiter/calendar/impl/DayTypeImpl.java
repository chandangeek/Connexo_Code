/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link com.elster.jupiter.calendar.DayType} interface.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-04-18
 */
class DayTypeImpl implements DayType {

    public enum Fields {
        ID("id"),
        NAME("name"),
        EVENT_OCCURENCES("eventOccurrences"),
        CALENDAR("calendar");

        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.DAYTYPE_NAME_FIELD_TOO_LONG + "}")
    private String name;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<Calendar> calendar = ValueReference.absent();
    private List<EventOccurrence> eventOccurrences = new ArrayList<>();

    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    private final ServerCalendarService calendarService;

    @Inject
    DayTypeImpl(ServerCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    DayTypeImpl init(Calendar calendar, String name) {
        this.name = name;
        this.calendar.set(calendar);
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }


    @Override
    public List<EventOccurrence> getEventOccurrences() {
        return Collections.unmodifiableList(this.eventOccurrences);
    }

    @Override
    public Calendar getCalendar() {
        return this.calendar.orNull();
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public void delete() {
        if (id == 0) {
            return;
        }
        this.eventOccurrences.clear();
        calendarService.getDataModel().remove(this);
    }

    EventOccurrence addEventOccurrence(EventOccurrence eventOccurrence) {
        Save.CREATE.validate(calendarService.getDataModel(), eventOccurrence);
        this.eventOccurrences.add(eventOccurrence);
        return eventOccurrence;
    }

    void setName(String name) {
        this.name = name;
    }

     void save() {
         for (EventOccurrence eventOccurrence : eventOccurrences) {
             Event event = this.getCalendar().getEvents()
                     .stream()
                     .filter(evt -> evt.getName().equals(eventOccurrence.getEvent().getName())).findAny().get();

             ((EventOccurrenceImpl) eventOccurrence).setEvent(event);
         }
         Save.CREATE.save(calendarService.getDataModel(), this, Save.Create.class);
    }

}
