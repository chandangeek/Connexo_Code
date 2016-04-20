package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.calendar.Period;
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
import java.util.Objects;

/**
 * Created by igh on 18/04/2016.
 */
public class DayTypeImpl implements DayType {

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

    private long id;
    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String name;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<Calendar> calendar = ValueReference.absent();
    private List<EventOccurrence> eventOccurrences = new ArrayList<>();

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private final ServerCalendarService calendarService;

    @Inject
    DayTypeImpl(ServerCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public DayTypeImpl init(Calendar calendar, String name) {
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

    @Override
    public EventOccurrence addEventOccurrence(int hours, int minutes, int seconds) {
        EventOccurrenceImpl eventOccurrence = calendarService.getDataModel().getInstance(EventOccurrenceImpl.class).init(this, hours, minutes, seconds);
        Save.CREATE.validate(calendarService.getDataModel(), eventOccurrence);
        this.eventOccurrences.add(eventOccurrence);
        touch();
        return eventOccurrence;
    }


    @Override
    public void removeEventOccurrence(EventOccurrence eventOccurrence) {
        Objects.requireNonNull(eventOccurrence);
        eventOccurrences.remove(eventOccurrence);
        touch();
    }

    void touch() {
        this.calendarService.getDataModel().touch(this);
    }

}
