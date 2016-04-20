package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Map;

/**
 * Created by igh on 18/04/2016.
 */
public abstract class ExceptionalOccurrenceImpl implements ExceptionalOccurrence {

    public enum Fields {
        ID("id"),
        CALENDAR("calendar"),
        DAY("day"),
        MONTH("month"),
        YEAR("year"),
        DAYTYPE("daytype");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    // ORM inheritance map
    public static final Map<String, Class<? extends ExceptionalOccurrence>> IMPLEMENTERS = getImplementers();

    private long id;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<Calendar> calendar = ValueReference.absent();
    private int day;
    private int month;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<DayType> dayType = ValueReference.absent();

    private final CalendarService calendarService;

    @Inject
    ExceptionalOccurrenceImpl(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public ExceptionalOccurrenceImpl init(Calendar calendar, DayType dayType, int day, int month) {
        this.calendar.set(calendar);
        this.dayType.set(dayType);
        this.day = day;
        this.month = month;
        return this;
    }

    @Override
    public Calendar getCalendar() {
        return this.calendar.orNull();
    }

    @Override
    public DayType getDayType() {
        return this.dayType.orNull();
    }

    @Override
    public boolean occursAt(Instant instant) {
        return false;
    }

    static Map<String, Class<? extends ExceptionalOccurrence>> getImplementers() {
        ImmutableMap.Builder<String, Class<? extends ExceptionalOccurrence>> builder = ImmutableMap.builder();
        builder.put(FixedExceptionalOccurrenceImpl.TYPE_IDENTIFIER, FixedExceptionalOccurrenceImpl.class)
                .put(RecurrentExceptionalOccurrenceImpl.TYPE_IDENTIFIER, RecurrentExceptionalOccurrenceImpl.class);
        return builder.build();
    }

    protected int getDay() {
        return day;
    }

    protected int getMonth() {
        return month;
    }

    @Override
    public long getId() {
        return id;
    }
}
