package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Created by igh on 18/04/2016.
 */
public class DayTypeImpl implements DayType {

    public enum Fields {
        ID("id"),
        NAME("name"),
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

    private final CalendarService calendarService;

    @Inject
    DayTypeImpl(CalendarService calendarService) {
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
        return null;
    }

    @Override
    public Calendar getCalendar() {
        return this.calendar.orNull();
    }
}
