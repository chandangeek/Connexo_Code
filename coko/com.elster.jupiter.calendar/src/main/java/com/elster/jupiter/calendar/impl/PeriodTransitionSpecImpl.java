package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransitionSpec;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.util.Map;

/**
 * Created by igh on 19/04/2016.
 */
public abstract class PeriodTransitionSpecImpl implements PeriodTransitionSpec {

    public enum Fields {
        ID("id"),
        CALENDAR("calendar"),
        PERIOD("period"),
        DAY("day"),
        MONTH("month"),
        YEAR("year");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    // ORM inheritance map
    public static final Map<String, Class<? extends PeriodTransitionSpec>> IMPLEMENTERS = getImplementers();

    private long id;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<Calendar> calendar = ValueReference.absent();
    private int day;
    private int month;
    private Reference<Period> period = ValueReference.absent();

    private final CalendarService calendarService;

    @Inject
    PeriodTransitionSpecImpl(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public PeriodTransitionSpecImpl init(Calendar calendar, int day, int month) {
        this.calendar.set(calendar);
        this.day = day;
        this.month = month;
        return this;
    }

    @Override
    public Calendar getCalendar() {
        return this.calendar.orNull();
    }

    static Map<String, Class<? extends PeriodTransitionSpec>> getImplementers() {
        ImmutableMap.Builder<String, Class<? extends PeriodTransitionSpec>> builder = ImmutableMap.builder();
        builder.put(FixedPeriodTransitionSpecImpl.TYPE_IDENTIFIER, FixedPeriodTransitionSpecImpl.class)
                .put(RecurrentPeriodTransitionSpecImpl.TYPE_IDENTIFIER, RecurrentPeriodTransitionSpecImpl.class);
        return builder.build();
    }

    @Override
    public Period getPeriod() {
        return this.period.orNull();
    }

    protected int getDay() {
        return day;
    }

    protected int getMonth() {
        return month;
    }

    void setPeriod(Period period) {
        this.period.set(period);
    }
}
