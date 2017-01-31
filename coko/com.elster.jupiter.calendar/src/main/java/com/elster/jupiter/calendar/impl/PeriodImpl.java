/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.Instant;

/**
 * Provides an implementation for the {@link com.elster.jupiter.calendar.Period} interface.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-04-18
 */
class PeriodImpl implements Period {

    public enum Fields {
        ID("id"),
        NAME("name"),
        MONDAY("monday"),
        TUESDAY("tuesday"),
        WEDNESDAY("wednesday"),
        THURSDAY("thursday"),
        FRIDAY("friday"),
        SATURDAY("saturday"),
        SUNDAY("sunday"),
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
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.PERIOD_NAME_FIELD_TOO_LONG + "}")
    private String name;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<Calendar> calendar = ValueReference.absent();

    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<DayType> monday = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<DayType> tuesday = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<DayType> wednesday = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<DayType> thursday = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<DayType> friday = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<DayType> saturday = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<DayType> sunday = ValueReference.absent();

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
    PeriodImpl(ServerCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    PeriodImpl init(Calendar calendar, String name, DayType monday, DayType tuesday, DayType wednesday, DayType thursday, DayType friday, DayType saturday, DayType sunday) {
        this.name = name;
        this.calendar.set(calendar);
        this.monday.set(monday);
        this.tuesday.set(tuesday);
        this.wednesday.set(wednesday);
        this.thursday.set(thursday);
        this.friday.set(friday);
        this.saturday.set(saturday);
        this.sunday.set(sunday);
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
    public DayType getDayType(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return monday.get();
            case TUESDAY:
                return tuesday.get();
            case WEDNESDAY:
                return wednesday.get();
            case THURSDAY:
                return thursday.get();
            case FRIDAY:
                return friday.get();
            case SATURDAY:
                return saturday.get();
            case SUNDAY:
                return sunday.get();
            default:
                return monday.get();
        }
    }

    void delete() {
        if (id == 0) {
            return;
        }
        calendarService.getDataModel().remove(this);
    }


    void save() {
        monday.set(
                this.getCalendar().getDayTypes().stream().filter(type -> type.getName().equals(monday.get().getName())).findFirst().get());
        tuesday.set(
                this.getCalendar().getDayTypes().stream().filter(type -> type.getName().equals(tuesday.get().getName())).findFirst().get());
        wednesday.set(
                this.getCalendar().getDayTypes().stream().filter(type -> type.getName().equals(wednesday.get().getName())).findFirst().get());
        thursday.set(
                this.getCalendar().getDayTypes().stream().filter(type -> type.getName().equals(thursday.get().getName())).findFirst().get());
        friday.set(
                this.getCalendar().getDayTypes().stream().filter(type -> type.getName().equals(friday.get().getName())).findFirst().get());
        saturday.set(
                this.getCalendar().getDayTypes().stream().filter(type -> type.getName().equals(saturday.get().getName())).findFirst().get());
        sunday.set(
                this.getCalendar().getDayTypes().stream().filter(type -> type.getName().equals(sunday.get().getName())).findFirst().get());
        Save.CREATE.save(calendarService.getDataModel(), this, Save.Create.class);
    }

}

