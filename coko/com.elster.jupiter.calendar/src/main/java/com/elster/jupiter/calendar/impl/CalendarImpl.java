package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;
import com.elster.jupiter.calendar.PeriodTransitionSpec;
import com.elster.jupiter.calendar.RecurrentExceptionalOccurrence;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Created by igh on 18/04/2016.
 */
public class CalendarImpl implements Calendar {

    public enum Fields {
        ID("id"),
        NAME("name"),
        MRID("mRID"),
        DESCRIPTION("description"),
        TIMEZONENAME("timeZoneName"),
        CATEGORY("category"),
        DAYTYPES("dayTypes"),
        PERIODS("periods"),
        EXCEPTIONAL_OCCURRENCES("exceptionalOccurrences");

        private final String javaFieldName;

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
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String description;
    private String mRID;
    private String timeZoneName;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private TimeZone timeZone;
    private Reference<Category> category = ValueReference.absent();
    private List<DayType> dayTypes = new ArrayList<>();
    private List<Period> periods = new ArrayList<>();
    private List<ExceptionalOccurrence> exceptionalOccurrences = new ArrayList<>();

    private final ServerCalendarService calendarService;

    @Inject
    CalendarImpl(ServerCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    CalendarImpl init(String name, String description, TimeZone timeZone, Category category) {
        this.name = name;
        this.description = description;
        this.timeZone = timeZone;
        this.category.set(category);
        return this;
    }

    static CalendarImpl from(DataModel dataModel, String description, String name, TimeZone timeZone, Category category) {
        return dataModel.getInstance(CalendarImpl.class).init(name, description, timeZone, category);
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
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Calendar)) {
            return false;
        }
        Calendar that = (Calendar) o;
        return id == that.getId();
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void save() {
        if (this.getId() > 0) {
            Save.UPDATE.save(calendarService.getDataModel(), this, Save.Update.class);
        } else {
            Save.CREATE.save(calendarService.getDataModel(), this, Save.Create.class);
        }
    }

    @Override
    public void delete() {
        calendarService.getDataModel().remove(this);
    }

    @Override
    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone =  TimeZone.getTimeZone(ZoneId.of(timeZoneName));
        }
        return timeZone;
    }

    @Override
    public Category getCategory() {
        return this.category.orNull();
    }

    @Override
    public List<Event> getEvents() {
        return null;
    }

    @Override
    public List<Period> getPeriods() {
        return Collections.unmodifiableList(this.periods);
    }

    @Override
    public List<DayType> getDayTypes() {
        return Collections.unmodifiableList(this.dayTypes);
    }

    @Override
    public List<ExceptionalOccurrence> getExceptionalOccurrences() {
        return Collections.unmodifiableList(this.exceptionalOccurrences);
    }

    @Override
    public List<? extends PeriodTransitionSpec> getPeriodTransitionSpecs() {
        return null;
    }

    @Override
    public List<PeriodTransition> getTransitions() {
        return null;
    }

    @Override
    public DayType addDayType(String name) {
        DayTypeImpl dayType = calendarService.getDataModel().getInstance(DayTypeImpl.class).init(this, name);
        Save.CREATE.validate(calendarService.getDataModel(), dayType);
        this.dayTypes.add(dayType);
        touch();
        return dayType;
    }


    @Override
    public void removeDayType(DayType dayType) {
        Objects.requireNonNull(dayType);
        dayTypes.remove(dayType);
        touch();
    }

    @Override
    public Period addPeriod(String name) {
        PeriodImpl period = calendarService.getDataModel().getInstance(PeriodImpl.class).init(this, name);
        Save.CREATE.validate(calendarService.getDataModel(), period);
        this.periods.add(period);
        touch();
        return period;
    }


    @Override
    public void removePeriod(Period period) {
        Objects.requireNonNull(period);
        periods.remove(period);
        touch();
    }

    @Override
    public FixedExceptionalOccurrence addFixedExceptionalOccurrence(int day, int month, int year) {
        FixedExceptionalOccurrenceImpl fixedExceptionalOccurrence =
                calendarService.getDataModel().getInstance(FixedExceptionalOccurrenceImpl.class).init(this, day, month, year);
        Save.CREATE.validate(calendarService.getDataModel(), fixedExceptionalOccurrence);
        this.exceptionalOccurrences.add(fixedExceptionalOccurrence);
        touch();
        return fixedExceptionalOccurrence;
    }


    @Override
    public void removeFixedExceptionalOccurrence(FixedExceptionalOccurrence fixedExceptionalOccurrence) {
        Objects.requireNonNull(fixedExceptionalOccurrence);
        exceptionalOccurrences.remove(fixedExceptionalOccurrence);
        touch();
    }

    @Override
    public RecurrentExceptionalOccurrence addFixedExceptionalOccurrence(int day, int month) {
        RecurrentExceptionalOccurrenceImpl recurrentExceptionalOccurrence =
                calendarService.getDataModel().getInstance(RecurrentExceptionalOccurrenceImpl.class).init(this, day, month);
        Save.CREATE.validate(calendarService.getDataModel(), recurrentExceptionalOccurrence);
        this.exceptionalOccurrences.add(recurrentExceptionalOccurrence);
        touch();
        return recurrentExceptionalOccurrence;
    }


    @Override
    public void removeFixedExceptionalOccurrence(RecurrentExceptionalOccurrence recurrentExceptionalOccurrence) {
        Objects.requireNonNull(recurrentExceptionalOccurrence);
        exceptionalOccurrences.remove(recurrentExceptionalOccurrence);
        touch();
    }

    void touch() {
        this.calendarService.getDataModel().touch(this);
    }



}
