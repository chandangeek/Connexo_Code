/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.CalendarTimeSeries;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.calendar.EventType;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedPeriodTransitionSpec;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;
import com.elster.jupiter.calendar.PeriodTransitionSpec;
import com.elster.jupiter.calendar.RecurrentExceptionalOccurrence;
import com.elster.jupiter.calendar.RecurrentPeriodTransitionSpec;
import com.elster.jupiter.calendar.Status;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

@UniqueMRID(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_CALENDAR_MRID + "}")
@UniqueCalendarName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_CALENDAR_NAME + "}")
@ValidTransitions(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.VALID_TRANSITIONS + "}")
public class CalendarImpl implements ServerCalendar {

    public enum Fields {
        ID("id"),
        NAME("name"),
        MRID("mRID"),
        STARTYEAR("startYear"),
        ENDYEAR("endYear"),
        ABSTRACT_CALENDAR("abstractCalendar"),
        DESCRIPTION("description"),
        CATEGORY("category"),
        DAYTYPES("dayTypes"),
        PERIODS("periods"),
        EXCEPTIONAL_OCCURRENCES("exceptionalOccurrences"),
        PERIOD_TRANSITION_SPECS("periodTransitionSpecs"),
        STATUS("status"),
        EVENTSET("eventSet"),
        OBSOLETETIME("obsoleteTime");

        private final String javaFieldName;

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
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.CAL_NAME_FIELD_TOO_LONG + "}")
    private String name;
    @Size(max = Table.DESCRIPTION_LENGTH, message = "{" + MessageSeeds.Constants.DESCRIPTION_FIELD_TOO_LONG + "}")
    private String description;
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.CAL_MRID_FIELD_TOO_LONG + "}")
    private String mRID;
    private boolean abstractCalendar = false;
    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Integer startYear;
    private Integer endYear;
    private Status status;
    private Instant obsoleteTime;

    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    private Reference<Category> category = ValueReference.absent();
    @IsPresent
    private Reference<EventSet> eventSet = ValueReference.absent();
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
    private List<CalendarTimeSeriesEntity> timeSeries = new ArrayList<>();    // Composition managed by ORM

    private final ServerCalendarService calendarService;
    private final EventService eventService;
    private final Clock clock;
    private final Thesaurus thesaurus;

    @Inject
    CalendarImpl(ServerCalendarService calendarService, EventService eventService, Clock clock, Thesaurus thesaurus) {
        this.calendarService = calendarService;
        this.eventService = eventService;
        this.clock = clock;
        this.thesaurus = thesaurus;
    }

    static CalendarImpl from(DataModel dataModel, EventSet eventSet) {
        return dataModel.getInstance(CalendarImpl.class).init(eventSet);
    }

    private CalendarImpl init(EventSet eventSet) {
        this.eventSet.set(eventSet);
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
    public String getMRID() {
        return mRID;
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

    void setCategory(Category category) {
        this.category.set(category);
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
            if (isActive()) {
                doUpdate();
            } else {
                doRedefine();
            }
        } else {
            doSave();
        }
    }

    @Override
    public boolean isActive() {
        return Status.ACTIVE.equals(status);
    }

    private void doSave() {
        Save.CREATE.save(calendarService.getDataModel(), this, Save.Create.class);
        saveDayTypes();
        savePeriods();
        savePeriodTransitionSpecs();
        saveExceptionalOccurrences();
        eventService.postEvent(EventType.CALENDAR_CREATE.topic(), this);
    }

    private void doRedefine() {
        Calendar savedCalendar = calendarService.findCalendar(this.getId()).get();
        for (ExceptionalOccurrence occurrence : savedCalendar.getExceptionalOccurrences()) {
            ((ExceptionalOccurrenceImpl) occurrence).delete();
        }

        for (PeriodTransitionSpec periodTransitionSpec : savedCalendar.getPeriodTransitionSpecs()) {
            ((PeriodTransitionSpecImpl) periodTransitionSpec).delete();
        }

        for (Period period : savedCalendar.getPeriods()) {
            ((PeriodImpl) period).delete();
        }

        for (DayType dayType : savedCalendar.getDayTypes()) {
            ((DayTypeImpl) dayType).delete();
        }

        Save.UPDATE.save(calendarService.getDataModel(), this, Save.Update.class);
        saveDayTypes();
        savePeriods();
        savePeriodTransitionSpecs();
        saveExceptionalOccurrences();
        this.regenerateCachedTimeSeries();
        eventService.postEvent(EventType.CALENDAR_UPDATE.topic(), this);
    }

    private void doUpdate() {
        decorate(exceptionalOccurrences
                .stream())
                .filter(exceptionalOccurrence -> exceptionalOccurrence.getId() == 0)
                .filterSubType(ExceptionalOccurrenceImpl.class)
                .forEach(ExceptionalOccurrenceImpl::save);
        eventService.postEvent(EventType.CALENDAR_UPDATE.topic(), this);
    }

    private void saveActivation() {
        Save.UPDATE.save(calendarService.getDataModel(), this, Save.Update.class);
        eventService.postEvent(EventType.CALENDAR_UPDATE.topic(), this);
    }

    @Override
    public CalendarService.CalendarBuilder redefine() {
        if (isActive()) {
            throw new IllegalStateException("Cannot redefine an active calendar; calendar : " + this.getName());
        }
        exceptionalOccurrences.clear();
        periodTransitionSpecs.clear();
        periods.clear();
        dayTypes.clear();
        return new CalendarBuilderImpl(calendarService.getDataModel(), this);
    }

    @Override
    public CalendarService.StrictCalendarBuilder update() {
        return new StrictCalendarBuilderImpl(clock, this, thesaurus);
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
    public boolean mayBeDeleted() {
        return Status.INACTIVE.equals(this.status);
    }

    @Override
    public void delete() {
        Calendar savedCalendar = calendarService.findCalendar(this.getId()).get();
        for (ExceptionalOccurrence occurrence : savedCalendar.getExceptionalOccurrences()) {
            ((ExceptionalOccurrenceImpl) occurrence).delete();
        }

        for (PeriodTransitionSpec periodTransitionSpec : savedCalendar.getPeriodTransitionSpecs()) {
            ((PeriodTransitionSpecImpl) periodTransitionSpec).delete();
        }

        for (Period period : savedCalendar.getPeriods()) {
            ((PeriodImpl) period).delete();
        }

        for (DayType dayType : savedCalendar.getDayTypes()) {
            ((DayTypeImpl) dayType).delete();
        }
        this.timeSeries.clear();
        calendarService.getDataModel().remove(this);
        eventService.postEvent(EventType.CALENDAR_DELETE.topic(), this);
    }

    @Override
    public Category getCategory() {
        return this.category.orNull();
    }

    @Override
    public List<Event> getEvents() {
        return eventSet.get().getEvents();
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

    @Override
    public void deactivate() {
        this.status = Status.INACTIVE;
        saveActivation();
    }

    @Override
    public void activate() {
        this.status = Status.ACTIVE;
        saveActivation();
    }

    private boolean containsFixedTransitions() {
        return this.periodTransitionSpecs.stream()
                .anyMatch(transition -> transition instanceof FixedPeriodTransitionSpec);
    }

    private List<PeriodTransition> getFixedPeriodTransitions() {
        return this.periodTransitionSpecs
                .stream()
                .map(spec -> (FixedPeriodTransitionSpec) spec)
                .sorted((o1, o2) -> o1.getOccurrence().compareTo(o2.getOccurrence()))
                .map(spec -> new PeriodTransitionImpl(spec.getOccurrence(), spec.getPeriod()))
                .collect(Collectors.toList());
    }

    private List<PeriodTransition> getRecurrentPeriodTransitions() {
        List<PeriodTransition> result = new ArrayList<>();
        int year = startYear;
        int toYear = (endYear == null || endYear == 0) ? Year.now(this.clock).getValue() : endYear;
        while (year <= toYear) {
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

    public EventSet getEventSet() {
        return eventSet.get();
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

    FixedExceptionalOccurrence addFixedExceptionalOccurrence(DayType dayType, LocalDate localDate) {
        FixedExceptionalOccurrenceImpl fixedExceptionalOccurrence =
                calendarService.getDataModel().getInstance(FixedExceptionalOccurrenceImpl.class).init(this, dayType, localDate);
        Save.CREATE.validate(calendarService.getDataModel(), fixedExceptionalOccurrence);
        this.exceptionalOccurrences.add(fixedExceptionalOccurrence);
        return fixedExceptionalOccurrence;
    }

    RecurrentExceptionalOccurrence addRecurrentExceptionalOccurrence(DayType dayType, MonthDay monthDay) {
        RecurrentExceptionalOccurrenceImpl recurrentExceptionalOccurrence =
                calendarService.getDataModel().getInstance(RecurrentExceptionalOccurrenceImpl.class).init(this, dayType, monthDay);
        Save.CREATE.validate(calendarService.getDataModel(), recurrentExceptionalOccurrence);
        this.exceptionalOccurrences.add(recurrentExceptionalOccurrence);
        return recurrentExceptionalOccurrence;
    }

    PeriodTransitionSpec addPeriodTransitionSpec(PeriodTransitionSpec periodTransitionSpec) {
        Save.CREATE.validate(calendarService.getDataModel(), periodTransitionSpec);
        this.periodTransitionSpecs.add(periodTransitionSpec);
        return periodTransitionSpec;
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

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getStatusDisplayName() {
        return CalendarStatusTranslationKeys.translationFor(this.getStatus(), this.thesaurus);
    }

    void setStatus(Status status) {
        this.status = status;
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

    @Override
    public void makeObsolete() {
        this.obsoleteTime = this.clock.instant();
        this.timeSeries.clear();
        calendarService.getDataModel().update(this, Fields.OBSOLETETIME.fieldName());
    }

    @Override
    public Optional<Instant> getObsoleteTime() {
        return Optional.ofNullable(this.obsoleteTime);
    }

    @Override
    public synchronized CalendarTimeSeries toTimeSeries(TemporalAmount interval, ZoneId zoneId) {
        // Synchronize to avoid that two threads create the same time series
        return new CalendarTimeSeriesImpl(
                this.timeSeries
                    .stream()
                    .filter(timeSeries -> timeSeries.matches(interval, zoneId))
                    .findAny()
                    .orElseGet(() -> this.createTimeSeries(interval, zoneId)));
    }

    private CalendarTimeSeriesEntity createTimeSeries(TemporalAmount interval, ZoneId zoneId) {
        Year endYear;
        if (this.endYear == null) {
            // First time a time series is created
            Year thisYear = Year.now(this.calendarService.getClock());
            LocalDate dec1stThisYear = thisYear.atMonthDay(MonthDay.of(Month.DECEMBER, 1));
            LocalDate now = LocalDate.now(this.calendarService.getClock());
            if (now.isAfter(dec1stThisYear)) {
                /* Recurrent task that extends time series has already run
                 * so generate data until end of next year.*/
                endYear = thisYear.plusYears(1);
            } else {
                endYear = thisYear;
            }
        } else {
            endYear = Year.of(this.endYear);
        }
        CalendarTimeSeriesEntity generated = this.calendarService
                .getDataModel()
                .getInstance(CalendarTimeSeriesEntityImpl.class)
                .initialize(this, interval, zoneId)
                .generate(endYear);
        this.timeSeries.add(generated);
        if (this.endYear == null) {
            // First generation
            this.setEndYear(Year.of(generated.timeSeries().getLastDateTime().atZone(zoneId).getYear()));
            this.calendarService.getDataModel().update(this, Fields.ENDYEAR.fieldName());
        }
        return generated;
    }

    @Override
    public ZonedView forZone(ZoneId zoneId, Year year) {
        return new ZonedCalenderViewImpl(this, this.clock, zoneId, year);
    }

    @Override
    public List<CalendarTimeSeriesEntity> getCachedTimeSeries() {
        return this.timeSeries;
    }

    @Override
    public void extend(long timeSeriesId) {
        this.timeSeries
                .stream()
                .filter(each -> each.timeSeries().getId() == timeSeriesId)
                .findAny()
                .ifPresent(CalendarTimeSeriesEntity::extend);
    }

    @Override
    public void bumpEndYear() {
        if (this.endYear == null) {
            /* Should not occur since bumping is done from the recurring task
             * that extends all calendars with cached time series
             * and endYear is initialized on the first creation of a time series
             * but hey, I am a defensive programmer. */
            this.endYear = Year.now(this.calendarService.getClock()).plusYears(1).getValue();
        } else {
            this.endYear++;
        }
        this.calendarService.getDataModel().update(this, Fields.ENDYEAR.fieldName());
    }

    @Override
    public void regenerateCachedTimeSeries() {
        this.timeSeries.forEach(CalendarTimeSeriesEntity::regenerate);
    }

}