package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransitionSpec;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
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
    public static final Map<String, Class<? extends PeriodTransitionSpec>> IMPLEMENTERS = ImmutableMap.of(
            FixedPeriodTransitionSpecImpl.TYPE_IDENTIFIER, FixedPeriodTransitionSpecImpl.class,
            RecurrentPeriodTransitionSpecImpl.TYPE_IDENTIFIER, RecurrentPeriodTransitionSpecImpl.class);


    private long id;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<Calendar> calendar = ValueReference.absent();
    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Integer day;
    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Integer month;
    private Reference<Period> period = ValueReference.absent();

    private final ServerCalendarService calendarService;

    @Inject
    PeriodTransitionSpecImpl(ServerCalendarService calendarService) {
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

    void save() {
        period.set(this.getCalendar().getPeriods().stream().filter(p -> p.getName().equals(period.get().getName())).findFirst().get());
        Save.CREATE.save(calendarService.getDataModel(), this, Save.Create.class);
    }
}
