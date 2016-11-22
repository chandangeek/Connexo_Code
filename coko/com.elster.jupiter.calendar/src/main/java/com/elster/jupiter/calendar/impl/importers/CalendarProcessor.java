/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.calendar.impl.CalendarImpl;
import com.elster.jupiter.calendar.impl.xmlbinding.DayType;
import com.elster.jupiter.calendar.impl.xmlbinding.Event;
import com.elster.jupiter.calendar.impl.xmlbinding.Exception;
import com.elster.jupiter.calendar.impl.xmlbinding.FixedOccurrence;
import com.elster.jupiter.calendar.impl.xmlbinding.Period;
import com.elster.jupiter.calendar.impl.xmlbinding.RangeTime;
import com.elster.jupiter.calendar.impl.xmlbinding.RecurringOccurrence;
import com.elster.jupiter.calendar.impl.xmlbinding.Transition;
import com.elster.jupiter.calendar.impl.xmlbinding.Transitions;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Registration;
import com.elster.jupiter.util.UpdatableHolder;

import javax.inject.Inject;
import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Currying.perform;
import static com.elster.jupiter.util.streams.Currying.use;

public class CalendarProcessor {

    private final CalendarService calendarService;
    private final Thesaurus thesaurus;

    private Set<ImportListener> importListeners = new HashSet<>();

    static interface ImportListener {

        void created(String mrid);

        void updated(String mrid);

    }

    @Inject
    public CalendarProcessor(CalendarService calendarService, Thesaurus thesaurus) {
        this.calendarService = calendarService;
        this.thesaurus = thesaurus;
    }

    public CalendarImportResult process(com.elster.jupiter.calendar.impl.xmlbinding.Calendars calendars) {
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

    private Calendar buildCalendar(com.elster.jupiter.calendar.impl.xmlbinding.Calendar calendar, Map<String, EventSet> eventSets) {
        UpdatableHolder<EventSet> eventSetHolder = new UpdatableHolder<>(null);
        Category category = calendarService.findCategoryByName(calendar.getCategory())
                .orElseThrow(() -> new CategoryNotFound(thesaurus, calendar.getCategory()));
        CalendarService.CalendarBuilder builder = calendarService.findCalendarByMRID(calendar.getMRID())
                .map(existingCalendar -> {
                    importListeners.forEach(perform(ImportListener::updated).with(calendar.getMRID()));
                    eventSetHolder.update(((CalendarImpl) existingCalendar).getEventSet());
                    return existingCalendar.redefine()
                            .name(getCalendarName(calendar))
                            .startYear(getStartYear(calendar))
                            .description(getDescription(calendar))
                            .mRID(calendar.getMRID());
                })
                .orElseGet(() -> {
                    importListeners.forEach(perform(ImportListener::created).with(calendar.getMRID()));
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
        for (DayType dayType : calendar.getDayTypes().getDayType()) {
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
            for (Object occurrence : exception.getOccurrences().getFixedOccurrenceOrRecurringOccurrence()) {
                if (occurrence instanceof FixedOccurrence) {
                    FixedOccurrence fixedOccurrence = (FixedOccurrence) occurrence;
                    exceptionBuilder.occursOnceOn(LocalDate.of(
                            fixedOccurrence.getYear().intValue(),
                            fixedOccurrence.getMonth().intValue(),
                            fixedOccurrence.getDay().intValue()));
                }
                if (occurrence instanceof RecurringOccurrence) {
                    RecurringOccurrence recurringOccurrence = (RecurringOccurrence) occurrence;
                    exceptionBuilder.occursAlwaysOn(MonthDay.of(
                            recurringOccurrence.getMonth().intValue(),
                            recurringOccurrence.getDay().intValue()));
                }
            }
        }
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

    private String getCalendarName(com.elster.jupiter.calendar.impl.xmlbinding.Calendar calendar) {
        String calendarName = calendar.getName();
        if (isEmpty(calendarName)) {
            throw new MissingCalendarName(thesaurus);
        }
        return calendarName;
    }

    private Year getStartYear(com.elster.jupiter.calendar.impl.xmlbinding.Calendar calendar) {
        BigInteger startYear = calendar.getStartYear();
        if (startYear == null)  {
            throw new MissingStartYear(thesaurus);
        }
        if (startYear.equals(BigInteger.ZERO)) {
            throw new StartYearCannotBeZero(thesaurus);
        }
        return Year.of(startYear.intValue());
    }

    private String getDescription(com.elster.jupiter.calendar.impl.xmlbinding.Calendar calendar) {
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
