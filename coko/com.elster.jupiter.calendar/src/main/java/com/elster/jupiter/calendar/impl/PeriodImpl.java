package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;

/**
 * Created by igh on 18/04/2016.
 */
public class PeriodImpl implements Period {

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

    private long id;
    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
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


    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private final CalendarService calendarService;

    @Inject
    PeriodImpl(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public PeriodImpl init(Calendar calendar, String name, DayType monday, DayType tuesday, DayType wednesday, DayType thursday, DayType friday, DayType saturday, DayType sunday) {
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
}

