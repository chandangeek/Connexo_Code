package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedPeriodTransitionSpec;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;
import com.elster.jupiter.calendar.PeriodTransitionSpec;
import com.elster.jupiter.calendar.RecurrentExceptionalOccurrence;
import com.elster.jupiter.calendar.RecurrentPeriodTransitionSpec;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Created by igh on 18/04/2016.
 */
public class CalendarImpl implements Calendar {

    public enum Fields {
        ID("id"),
        NAME("name"),
        MRID("mRID"),
        STARTYEAR("startYear"),
        ENDYEAR("endYear"),
        ABSTRACT_CALENDAR("abstractCalendar"),
        DESCRIPTION("description"),
        TIMEZONENAME("timeZoneName"),
        CATEGORY("category"),
        DAYTYPES("dayTypes"),
        PERIODS("periods"),
        EXCEPTIONAL_OCCURRENCES("exceptionalOccurrences"),
        PERIOD_TRANSITION_SPECS("periodTransitionSpecs"),
        EVENTS("events");

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
    private boolean abstractCalendar = false;
    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Integer startYear;
    private Integer endYear;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private TimeZone timeZone;
    private Reference<Category> category = ValueReference.absent();
    @Valid
    private List<Event> events = new ArrayList<>();
    @Size(min = 1, message = "{" + MessageSeeds.Constants.DAYTYPES_REQUIRED + "}")
    @Valid
    private List<DayType> dayTypes = new ArrayList<>();
    @Size(min = 1, message = "{" + MessageSeeds.Constants.PERIODS_REQUIRED + "}")
    @Valid
    private List<Period> periods = new ArrayList<>();
    @Valid
    private List<ExceptionalOccurrence> exceptionalOccurrences = new ArrayList<>();
    @Valid
    private List<PeriodTransitionSpec> periodTransitionSpecs = new ArrayList<>();

    private final ServerCalendarService calendarService;

    @Inject
    CalendarImpl(ServerCalendarService calendarService) {
        this.calendarService = calendarService;
        this.category.set(calendarService.findTimeOfUseCategory().get());
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

    void setDescription(String description) {
        this.description = description;
    }

    void setName(String name) {
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
            saveDayTypes();
            savePeriods();
            savePeriodTransitionSpecs();
            saveExceptionalOccurrences();
        }
    }

    private void saveDayTypes() {
        for (DayType dayType : dayTypes) {
            ((DayTypeImpl) dayType).save();
        }
    }

    private void savePeriodTransitionSpecs() {
        for (PeriodTransitionSpec periodTransitionSpec : periodTransitionSpecs) {
            ((PeriodTransitionSpecImpl) periodTransitionSpec).save();
        }
    }

    private void savePeriods() {
        for (Period period : periods) {
            ((PeriodImpl) period).save();
        }
    }

    private void saveExceptionalOccurrences() {
        for (ExceptionalOccurrence occurrence : exceptionalOccurrences) {
            ((ExceptionalOccurrenceImpl) occurrence).save();
        }
    }

    @Override
    public void delete() {
        calendarService.getDataModel().remove(this);
    }

    @Override
    public TimeZone getTimeZone() {
        if (timeZoneName == null) {
            return null;
        }
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
        return Collections.unmodifiableList(this.events);
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
        return Collections.unmodifiableList(this.periodTransitionSpecs);
    }

    @Override
    public List<PeriodTransition> getTransitions() {
        return (containsFixedTransitions()) ? getFixedPeriodTransitions() : getRecurrentPeriodTransitions();
    }

    private boolean containsFixedTransitions() {
        return this.periodTransitionSpecs.stream()
                .anyMatch(transition -> transition instanceof FixedPeriodTransitionSpec);
    }

    private List<PeriodTransition> getFixedPeriodTransitions() {
        return this.periodTransitionSpecs.stream().map(spec -> (FixedPeriodTransitionSpec) spec).sorted(new Comparator<FixedPeriodTransitionSpec>() {
            public int compare(FixedPeriodTransitionSpec o1, FixedPeriodTransitionSpec o2) {
                return o1.getOccurrence().compareTo(o2.getOccurrence());
            }
        }).map(spec -> new PeriodTransitionImpl(spec.getOccurrence(), spec.getPeriod())).collect(Collectors.toList());
    }

    private List<PeriodTransition> getRecurrentPeriodTransitions() {
        List<PeriodTransition> result = new ArrayList<>();
        int year = startYear;
        while (year <= endYear) {
            int finalYear = year;
            result.addAll(this.periodTransitionSpecs.stream()
                    .map(spec ->
                            new PeriodTransitionImpl(
                                    ((RecurrentPeriodTransitionSpec) spec).getOccurrence().atYear(finalYear),
                                    spec.getPeriod()))
                    .collect(Collectors.toList()));
            year ++;
        }
        return result;
    }


    DayType addDayType(DayType dayType) {
        Save.CREATE.validate(calendarService.getDataModel(), dayType);
        this.dayTypes.add(dayType);
        return dayType;
    }

    Period addPeriod(String name, DayType monday, DayType tuesday, DayType wednesday, DayType thursday, DayType friday, DayType saturday, DayType sunday) {
        PeriodImpl period = calendarService.getDataModel().getInstance(PeriodImpl.class).init(this, name, monday, tuesday, wednesday, thursday, friday, saturday, sunday);
        Save.CREATE.validate(calendarService.getDataModel(), period);
        this.periods.add(period);
        return period;
    }

    FixedExceptionalOccurrence addFixedExceptionalOccurrence(DayType dayType, int day, int month, int year) {
        FixedExceptionalOccurrenceImpl fixedExceptionalOccurrence =
                calendarService.getDataModel().getInstance(FixedExceptionalOccurrenceImpl.class).init(this, dayType, day, month, year);
        Save.CREATE.validate(calendarService.getDataModel(), fixedExceptionalOccurrence);
        this.exceptionalOccurrences.add(fixedExceptionalOccurrence);
        return fixedExceptionalOccurrence;
    }

    RecurrentExceptionalOccurrence addRecurrentExceptionalOccurrence(DayType dayType, int day, int month) {
        RecurrentExceptionalOccurrenceImpl recurrentExceptionalOccurrence =
                calendarService.getDataModel().getInstance(RecurrentExceptionalOccurrenceImpl.class).init(this, dayType, day, month);
        Save.CREATE.validate(calendarService.getDataModel(), recurrentExceptionalOccurrence);
        this.exceptionalOccurrences.add(recurrentExceptionalOccurrence);
        return recurrentExceptionalOccurrence;
    }

    PeriodTransitionSpec addPeriodTransitionSpec(PeriodTransitionSpec periodTransitionSpec) {
        Save.CREATE.validate(calendarService.getDataModel(), periodTransitionSpec);
        this.periodTransitionSpecs.add(periodTransitionSpec);
        return periodTransitionSpec;
    }

    Event addEvent(String name, long code) {
        EventImpl event = calendarService.getDataModel().getInstance(EventImpl.class).init(this, name, code);
        Save.CREATE.validate(calendarService.getDataModel(), event);
        this.events.add(event);
        return event;
    }

    public Year getStartYear() {
        return Year.of(startYear);
    }

    public Year getEndYear() {
        return Year.of(endYear);
    }

    public boolean isAbstract() {
        return abstractCalendar;
    }


    void setmRID(String mRID) {
        this.mRID = mRID;
    }

    void setStartYear(Year startYear) {
        if (startYear != null) {
            this.startYear = startYear.getValue();
        }
    }

    void setEndYear(Year endYear) {
        this.endYear = endYear.getValue();
    }

    void setTimeZone(TimeZone timeZone) {
        if (timeZone != null) {
            this.timeZone = timeZone;
            this.timeZoneName = timeZone.getID();
        }
    }
}
