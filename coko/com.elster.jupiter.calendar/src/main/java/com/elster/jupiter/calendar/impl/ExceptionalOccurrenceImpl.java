/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Map;

/**
 * Serves as the root for the implementation classes of the
 * {@link com.elster.jupiter.calendar.ExceptionalOccurrence} class hierarchy.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-04-18
 */
abstract class ExceptionalOccurrenceImpl implements ExceptionalOccurrence {

    public enum Fields {
        ID("id"),
        CALENDAR("calendar"),
        DAY("day"),
        MONTH("month"),
        YEAR("year"),
        DAYTYPE("dayType");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    // ORM inheritance map
    public static final Map<String, Class<? extends ExceptionalOccurrence>> IMPLEMENTERS = ImmutableMap.of(
            FixedExceptionalOccurrenceImpl.TYPE_IDENTIFIER, FixedExceptionalOccurrenceImpl.class,
            RecurrentExceptionalOccurrenceImpl.TYPE_IDENTIFIER, RecurrentExceptionalOccurrenceImpl.class);

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<Calendar> calendar = ValueReference.absent();
    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Integer day;
    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Integer month;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<DayType> dayType = ValueReference.absent();

    private final ServerCalendarService calendarService;

    @Inject
    ExceptionalOccurrenceImpl(ServerCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    ExceptionalOccurrenceImpl init(Calendar calendar, DayType dayType, MonthDay monthDay) {
        this.calendar.set(calendar);
        this.dayType.set(dayType);
        this.day = monthDay.getDayOfMonth();
        this.month = monthDay.getMonthValue();
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
    public boolean occursAt(LocalDate localDate) {
        return localDate.getMonthValue() == month && localDate.getDayOfMonth() == day;
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

    void save() {
        dayType.set(this.getCalendar().getDayTypes().stream().filter(type -> type.getName().equals(dayType.get().getName())).findFirst().get());
        Save.CREATE.save(calendarService.getDataModel(), this, Save.Create.class);
    }

    void delete() {
        if (id == 0) {
            return;
        }
        calendarService.getDataModel().remove(this);
    }

}