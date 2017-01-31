/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.RecurrentExceptionalOccurrence;
import com.elster.jupiter.calendar.impl.CalendarImpl;
import com.elster.jupiter.calendar.impl.xmlbinding.Calendars;
import com.elster.jupiter.calendar.impl.xmlbinding.Event;
import com.elster.jupiter.calendar.impl.xmlbinding.Exception;
import com.elster.jupiter.calendar.impl.xmlbinding.FixedOccurrence;
import com.elster.jupiter.calendar.impl.xmlbinding.Period;
import com.elster.jupiter.calendar.impl.xmlbinding.RangeTime;
import com.elster.jupiter.calendar.impl.xmlbinding.RecurringOccurrence;
import com.elster.jupiter.calendar.impl.xmlbinding.Transition;
import com.elster.jupiter.calendar.impl.xmlbinding.Transitions;
import com.elster.jupiter.calendar.impl.xmlbinding.XmlCalendar;
import com.elster.jupiter.calendar.impl.xmlbinding.XmlDayType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;
import com.elster.jupiter.util.Registration;
import com.elster.jupiter.util.UpdatableHolder;

import javax.inject.Inject;
import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Currying.perform;
import static com.elster.jupiter.util.streams.Currying.use;

public class CalendarProcessor {

    private final CalendarService calendarService;
    private final Clock clock;
    private final Thesaurus thesaurus;

    private final Set<ImportListener> importListeners = new HashSet<>();

    static interface ImportListener {

        void created(String mrid);

        void updated(String mrid);

    }

    @Inject
    public CalendarProcessor(CalendarService calendarService, Clock clock, Thesaurus thesaurus) {
        this.calendarService = calendarService;
        this.clock = clock;
        this.thesaurus = thesaurus;
    }

    public CalendarImportResult process(Calendars calendars) {
        Map<String, EventSet> eventSets = calendars.getEventset()
                .stream()
                .map(eventSetXml -> {
                    CalendarService.EventSetBuilder eventSetBuilder = calendarService.findEventSetByName(eventSetXml.getName())
                            .map(EventSet::redefine)
                            .orElseGet(() -> calendarService.newEventSet(eventSetXml.getName()));
                    eventSetXml.getEvents()
                            .getEvent()
                            .stream()
                            .forEach(event -> eventSetBuilder.addEvent(getEventName(event))
                                    .withCode(getEventCode(event)));
                    return eventSetBuilder.add();
                })
                .collect(Collectors.toMap(
                        EventSet::getName,
                        Function.identity()
                ));

        List<Calendar> calendarList = calendars.getCalendar()
                .stream()
                .map(use(this::buildCalendar).with(eventSets))
                .collect(Collectors.toList());
        return new CalendarImportResult(eventSets.values(), calendarList);
    }

    Registration addListener(ImportListener importListener) {
        importListeners.add(importListener);
        return () -> importListeners.remove(importListener);
    }

    private Calendar buildCalendar(XmlCalendar calendar, Map<String, EventSet> eventSets) {
        Optional<Calendar> calendarByMRID = calendarService.findCalendarByMRID(calendar.getMRID());
        boolean strictUpdate = calendarByMRID.map(Calendar::isActive).orElse(false);

        if (strictUpdate) {
            return doStrictUpdate(calendar, calendarByMRID.get());
        } else {
            return createOrRedefine(calendar, calendarByMRID);
        }
    }

    private Calendar doStrictUpdate(XmlCalendar calendar, Calendar toUpdate) {
        Map<BigInteger, DayType> dayTypes = mapDayTypes(calendar, toUpdate);

        /* We must determine that no exceptions are added in the past,
         * yet we allow for repeating existing definitions in the past,
         * so for repeated definitions we remove them from this set.
         * If it is a exception in the past, and we cannot remove it from this set, the processing should fail.
         */
        HashSet<ExceptionalOccurrence> existingExceptions = toUpdate.getExceptionalOccurrences()
                .stream()
                .collect(Collectors.toCollection(HashSet::new));

        Holder<CalendarService.StrictCalendarBuilder> lazyBuilder =  HolderBuilder.lazyInitialize(toUpdate::update);
        UpdatableHolder<Function<Holder<CalendarService.StrictCalendarBuilder>, Calendar>> calendarUpdateFinisher = new UpdatableHolder<>(holder -> toUpdate);

        for (Exception exception : calendar.getExceptions().getException()) {

            exception.getOccurrences().getRecurringOccurrence()
                    .stream()
                    .forEach(recurringOccurrence -> {
                        if (!existingExceptions.removeIf(exceptionalOccurrence -> matches(dayTypes, exception, recurringOccurrence, exceptionalOccurrence))) {
                            throw new IllegalArgumentException("No new recurring occurrences allowed for updating an active calendar");
                        }
                    });

            DayType dayType = dayTypes.get(exception.getDayType());
            Holder<CalendarService.StrictExceptionBuilder> lazyExceptionBuilder = HolderBuilder.lazyInitialize(() -> lazyBuilder.get().except(dayType.getName()));
            UpdatableHolder<Consumer<Holder<CalendarService.StrictExceptionBuilder>>> exceptionFinisher = new UpdatableHolder<>(holder -> {});
            exception.getOccurrences().getFixedOccurrence()
                    .stream()
                    .filter(fixedOccurrence -> !existingExceptions.removeIf(exceptionalOccurrence -> matches(dayTypes, exception, fixedOccurrence, exceptionalOccurrence)))
                    .forEach(newFixedOccurrence -> {
                        lazyExceptionBuilder.get()
                                .occursOnceOn(localDate(newFixedOccurrence));
                        exceptionFinisher.update((Holder<CalendarService.StrictExceptionBuilder> holder) -> holder.get().add());
                        calendarUpdateFinisher.update((Holder<CalendarService.StrictCalendarBuilder> holder) -> holder.get().add());
                    });
            exceptionFinisher.get().accept(lazyExceptionBuilder);
        }

        return calendarUpdateFinisher.get().apply(lazyBuilder);
    }

    private LocalDate localDate(FixedOccurrence fixedOccurrence) {
        return LocalDate.of(fixedOccurrence.getYear().intValue(), fixedOccurrence.getMonth().intValue(), fixedOccurrence
                .getDay()
                .intValue());
    }

    private Map<BigInteger, DayType> mapDayTypes(XmlCalendar calendar, Calendar toUpdate) {
        return calendar.getDayTypes()
                    .getDayType()
                    .stream()
                    .collect(Collectors.toMap(
                            XmlDayType::getId,
                            xmlDayType -> toUpdate.getDayTypes()
                                    .stream()
                                    .filter(existingDayType -> existingDayType.getName().equals(xmlDayType.getName()))
                                    .findAny()
                                    .orElseThrow(() -> new IllegalArgumentException("dayType not found"))
                    ));
    }

    private boolean matches(Map<BigInteger, DayType> dayTypes, Exception exception, RecurringOccurrence recurringOccurrence, ExceptionalOccurrence exceptionalOccurrence) {
        boolean isRecurring = exceptionalOccurrence instanceof RecurrentExceptionalOccurrence;
        return isSameDayType(dayTypes, exception, exceptionalOccurrence) && isRecurring && ((RecurrentExceptionalOccurrence) exceptionalOccurrence)
                .getOccurrence()
                .equals(MonthDay.of(
                        recurringOccurrence.getMonth().intValue(),
                        recurringOccurrence.getDay().intValue()
                ));
    }

    private boolean isSameDayType(Map<BigInteger, DayType> dayTypes, Exception exception, ExceptionalOccurrence exceptionalOccurrence) {
        DayType dayType = dayTypes.get(exception.getDayType());
        return exceptionalOccurrence.getDayType()
                .getName()
                .equals(dayType.getName());
    }

    private boolean matches(Map<BigInteger, DayType> dayTypes, Exception exception, FixedOccurrence fixedOccurrence, ExceptionalOccurrence exceptionalOccurrence) {
        boolean isFixed = exceptionalOccurrence instanceof FixedExceptionalOccurrence;
        return isSameDayType(dayTypes, exception, exceptionalOccurrence) && isFixed && ((FixedExceptionalOccurrence) exceptionalOccurrence)
                .getOccurrence()
                .equals(LocalDate.of(fixedOccurrence.getYear().intValue(), fixedOccurrence.getMonth()
                        .intValue(), fixedOccurrence
                        .getDay()
                        .intValue()));
    }


    private boolean exists(HashSet<ExceptionalOccurrence> existingExceptions, Exception exceptional) {
       //TODO automatically generated method body, provide implementation.
        return false;
    }

    private Calendar createOrRedefine(XmlCalendar calendar, Optional<Calendar> calendarByMRID) {
        UpdatableHolder<EventSet> eventSetHolder = new UpdatableHolder<>(null);
        Category category = calendarService.findCategoryByName(calendar.getCategory())
                .orElseThrow(() -> new CategoryNotFound(thesaurus, calendar.getCategory()));
        UpdatableHolder<Consumer<ImportListener>> listenerNotification = new UpdatableHolder<>(null);
        CalendarService.CalendarBuilder builder = calendarByMRID
                .map(existingCalendar -> {
                    listenerNotification.update(perform(ImportListener::updated).with(calendar.getMRID()));
                    eventSetHolder.update(((CalendarImpl) existingCalendar).getEventSet());
                    return existingCalendar.redefine()
                            .name(getCalendarName(calendar))
                            .startYear(getStartYear(calendar))
                            .description(getDescription(calendar))
                            .mRID(calendar.getMRID());
                })
                .orElseGet(() -> {
                    listenerNotification.update(perform(ImportListener::created).with(calendar.getMRID()));
                    EventSet eventSet = calendarService.findEventSetByName(calendar.getEventset())
                            .orElseThrow(() -> new IllegalArgumentException("illegal eventset name " + calendar
                                    .getEventset()));
                    eventSetHolder.update(eventSet);
                    return calendarService.newCalendar(
                            getCalendarName(calendar),
                            getStartYear(calendar), eventSet)
                            .description(getDescription(calendar)).mRID(calendar.getMRID());
                });
        builder.category(category);

        Set<String> allowedEventIds = eventSetHolder.get().getEvents()
                .stream()
                .map(com.elster.jupiter.calendar.Event::getName)
                .collect(Collectors.toSet());

        Map<BigInteger, String> dayTypes = new HashMap<>(); // needed for periods (has a link to daytypes on code) and builder api requires daytype name
        for (XmlDayType dayType : calendar.getDayTypes().getDayType()) {
            BigInteger id = dayType.getId();
            String dayTypeName = dayType.getName();
            dayTypes.put(id, dayTypeName);
            CalendarService.DayTypeBuilder dayTypeBuilder = builder.newDayType(dayTypeName);
            for (RangeTime rangeTime : dayType.getRanges().getRangeTime()) {
                String eventId = rangeTime.getEvent();
                if (!allowedEventIds.contains(eventId)) {
                    throw new NoEventWithId(thesaurus, eventId);
                }
                LocalTime localTime = LocalTime.of(
                        rangeTime.getFrom().getHour().intValue(),
                        rangeTime.getFrom().getMinute().intValue(),
                        rangeTime.getFrom().getSecond().intValue());
                dayTypeBuilder.event(eventId).startsFrom(localTime);
            }
            dayTypeBuilder.add();
        }

        Map<BigInteger, String> periods = new HashMap<>(); // needed for period transitions
        for (Period period : calendar.getPeriods().getPeriod()) {
            BigInteger id = period.getId();
            String periodName = period.getName();
            periods.put(id, periodName);
            builder.addPeriod(
                    periodName,
                    getDayTypeNameById(period.getWeekTemplate().getMonday().getDayType(), dayTypes),
                    getDayTypeNameById(period.getWeekTemplate().getTuesday().getDayType(), dayTypes),
                    getDayTypeNameById(period.getWeekTemplate().getWednesday().getDayType(), dayTypes),
                    getDayTypeNameById(period.getWeekTemplate().getThursday().getDayType(), dayTypes),
                    getDayTypeNameById(period.getWeekTemplate().getFriday().getDayType(), dayTypes),
                    getDayTypeNameById(period.getWeekTemplate().getSaturday().getDayType(), dayTypes),
                    getDayTypeNameById(period.getWeekTemplate().getSunday().getDayType(), dayTypes));
        }

        Transitions transitions = calendar.getPeriods().getTransitions();
        boolean recurring = transitions.isRecurring();
        for (Transition transition : transitions.getTransition()) {
            if (recurring && (transition.getYear() != null)) {
                throw new YearNotAllowedForRecurringTransitions(thesaurus);
            }
            if (!recurring && (transition.getYear() == null)) {
                throw new YearRequiredForNotRecurringTransitions(thesaurus);
            }
            if (transition.getYear() != null) {
                builder.on(LocalDate.of(transition.getYear().intValue(), transition.getMonth()
                        .intValue(), transition.getDay().intValue()))
                        .transitionTo(getPeriodNameById(transition.getToPeriod(), periods));
            } else {
                builder.on(MonthDay.of(transition.getMonth().intValue(), transition.getDay().intValue()))
                        .transitionTo(getPeriodNameById(transition.getToPeriod(), periods));
            }
        }

        for (Exception exception : calendar.getExceptions().getException()) {
            String dayTypeName = getDayTypeNameById(exception.getDayType(), dayTypes);
            CalendarService.ExceptionBuilder exceptionBuilder = builder.except(dayTypeName);
            for (RecurringOccurrence recurringOccurrence : exception.getOccurrences().getRecurringOccurrence()) {
                exceptionBuilder.occursAlwaysOn(MonthDay.of(
                        recurringOccurrence.getMonth().intValue(),
                        recurringOccurrence.getDay().intValue()));
            }
            for (FixedOccurrence fixedOccurrence : exception.getOccurrences().getFixedOccurrence()) {
                exceptionBuilder.occursOnceOn(LocalDate.of(
                        fixedOccurrence.getYear().intValue(),
                        fixedOccurrence.getMonth().intValue(),
                        fixedOccurrence.getDay().intValue()));
            }
        }
        importListeners.forEach(listenerNotification.get());
        return builder.add();
    }

    private String getEventName(Event event) {
        return (String) findProperty(event, "name");
    }

    private int getEventCode(Event event) {
        Object code = findProperty(event, "code");
        if (code instanceof BigInteger) {
            return ((BigInteger) code).intValue();
        }
        throw new InvalidEventCode(thesaurus, code);
    }

    private Object findProperty(Event event, String name) {
        Optional<Object> result = event.getContent().stream().filter(e -> e instanceof JAXBElement)
                .filter(e -> ((JAXBElement) e).getName().getLocalPart().equals(name))
                .map(e -> ((JAXBElement) e).getValue()).findFirst();
        if (!result.isPresent()) {
            throw new PropertyNotFoundOnEvent(thesaurus, name);
        }
        return result.get();
    }

    private boolean isEmpty(String value) {
        return (value == null) || ("".equals(value));
    }

    private String getCalendarName(XmlCalendar calendar) {
        String calendarName = calendar.getName();
        if (isEmpty(calendarName)) {
            throw new MissingCalendarName(thesaurus);
        }
        return calendarName;
    }

    private Year getStartYear(XmlCalendar calendar) {
        BigInteger startYear = calendar.getStartYear();
        if (startYear == null) {
            throw new MissingStartYear(thesaurus);
        }
        if (startYear.equals(BigInteger.ZERO)) {
            throw new StartYearCannotBeZero(thesaurus);
        }
        return Year.of(startYear.intValue());
    }

    private String getDescription(XmlCalendar calendar) {
        return calendar.getDescription();
    }

    private String getDayTypeNameById(BigInteger id, Map<BigInteger, String> dayTypes) {
        String dayTypeName = dayTypes.get(id);
        if (dayTypeName == null) {
            throw new NoDayTypeForId(thesaurus, id.intValue());
        }
        return dayTypeName;
    }

    private String getPeriodNameById(BigInteger id, Map<BigInteger, String> periods) {
        String periodName = periods.get(id);
        if (periodName == null) {
            throw new NoPeriodForId(thesaurus, id.intValue());
        }
        return periodName;
    }

}
