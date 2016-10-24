/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.impl.xmlbinding.DayType;
import com.elster.jupiter.calendar.impl.xmlbinding.Event;
import com.elster.jupiter.calendar.impl.xmlbinding.FixedOccurrence;
import com.elster.jupiter.calendar.impl.xmlbinding.Period;
import com.elster.jupiter.calendar.impl.xmlbinding.RangeTime;
import com.elster.jupiter.calendar.impl.xmlbinding.RecurringOccurrence;
import com.elster.jupiter.calendar.impl.xmlbinding.Transition;
import com.elster.jupiter.calendar.impl.xmlbinding.Transitions;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by igh on 10/05/2016.
 */
public class CalendarFactory {

    private final CalendarService calendarService;
    private final Thesaurus thesaurus;

    private com.elster.jupiter.calendar.impl.xmlbinding.Calendar calendar;
    private Map<BigInteger, String> events;
    private Map<BigInteger, String> dayTypes;
    private Map<BigInteger, String> periods;

    @Inject
    public CalendarFactory(CalendarService calendarService, Thesaurus thesaurus) {
        this.calendarService = calendarService;
        this.thesaurus = thesaurus;
    }

    public com.elster.jupiter.calendar.Calendar getCalendar(com.elster.jupiter.calendar.impl.xmlbinding.Calendar calendar) {
        this.calendar = calendar;

        Category category = calendarService.findCategoryByName(calendar.getCategory())
                .orElseThrow(() -> new CategoryNotFound(thesaurus, calendar.getCategory()));

        CalendarService.CalendarBuilder builder = calendarService.findCalendarByMRID(calendar.getMRID())
                .map(c -> c.redefine()
                        .name(getCalendarName())
                        .startYear(getStartYear())
                        .description(getDescription())
                        .mRID(getMRID()))
                .orElseGet(() -> calendarService.newCalendar(
                        getCalendarName(),
                        getStartYear())
                        .description(getDescription()).mRID(getMRID()));
        builder.category(category);

        events = new HashMap<>();
        for (Event event : calendar.getEvents().getEvent()) {
            BigInteger eventId = getEventId(event);
            String eventName = getEventName(event);
            int eventCode = getEventCode(event);
            events.put(eventId, eventName);
            builder.addEvent(eventName, eventCode);
        }

        dayTypes = new HashMap<>(); // needed for periods (has a link to daytypes on code) and builder api requires daytype name
        for (DayType dayType : calendar.getDayTypes().getDayType()) {
            BigInteger id = dayType.getId();
            String dayTypeName = dayType.getName();
            dayTypes.put(id, dayTypeName);
            CalendarService.DayTypeBuilder dayTypeBuilder =  builder.newDayType(dayTypeName);
            for (RangeTime rangeTime : dayType.getRanges().getRangeTime()) {
                BigInteger eventId = rangeTime.getEvent();
                LocalTime localTime =  LocalTime.of(
                        rangeTime.getFrom().getHour().intValue(),
                        rangeTime.getFrom().getMinute().intValue(),
                        rangeTime.getFrom().getSecond().intValue());
                dayTypeBuilder.event(getEventNameById(eventId)).startsFrom(localTime);
            }
            dayTypeBuilder.add();
        }

        periods = new HashMap<>(); // needed for period transitions
        for (Period period : calendar.getPeriods().getPeriod()) {
            BigInteger id = period.getId();
            String periodName = period.getName();
            periods.put(id, periodName);
            builder.addPeriod(
                    periodName,
                    getDayTypeNameById(period.getWeekTemplate().getMonday().getDayType()),
                    getDayTypeNameById(period.getWeekTemplate().getTuesday().getDayType()),
                    getDayTypeNameById(period.getWeekTemplate().getWednesday().getDayType()),
                    getDayTypeNameById(period.getWeekTemplate().getThursday().getDayType()),
                    getDayTypeNameById(period.getWeekTemplate().getFriday().getDayType()),
                    getDayTypeNameById(period.getWeekTemplate().getSaturday().getDayType()),
                    getDayTypeNameById(period.getWeekTemplate().getSunday().getDayType()));
        }

        Transitions transitions =  calendar.getPeriods().getTransitions();
        boolean recurring = transitions.isRecurring();
        for (Transition transition : transitions.getTransition()) {
            if (recurring && (transition.getYear() != null)) {
                throw new YearNotAllowedForRecurringTransitions(thesaurus);
            }
            if (!recurring && (transition.getYear() == null)) {
                throw new YearRequiredForNotRecurringTransitions(thesaurus);
            }
            if (transition.getYear() != null) {
                builder.on(LocalDate.of(transition.getYear().intValue(), transition.getMonth().intValue(), transition.getDay().intValue()))
                        .transitionTo(getPeriodNameById(transition.getToPeriod()));
            } else {
                builder.on(MonthDay.of(transition.getMonth().intValue(), transition.getDay().intValue()))
                        .transitionTo(getPeriodNameById(transition.getToPeriod()));
            }
        }

        for (com.elster.jupiter.calendar.impl.xmlbinding.Exception exception : calendar.getExceptions().getException()) {
            String dayTypeName = getDayTypeNameById(exception.getDayType());
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

    private BigInteger getEventId(Event event) {
        Object id = findProperty(event, "id");
        if (id instanceof BigInteger) {
            return (BigInteger) id;
        }
        throw new InvalidEventId(thesaurus, id);
    }

    private Object findProperty(Event event, String name) {
        Optional<Object> result = event.getContent().stream().filter(e -> e instanceof JAXBElement)
                .filter(e -> ((JAXBElement) e).getName().toString().equals(name))
                .map(e -> ((JAXBElement) e).getValue()).findFirst();
        if (!result.isPresent()) {
            throw new PropertyNotFoundOnEvent(thesaurus, name);
        }
        return result.get();
    }

    private boolean isEmpty(String value) {
        return (value == null) || ("".equals(value));
    }

    private String getCalendarName() {
        String calendarName = calendar.getName();
        if (isEmpty(calendarName)) {
            throw new MissingCalendarName(thesaurus);
        }
        return calendarName;
    }

    private Year getStartYear() {
        BigInteger startYear = calendar.getStartYear();
        if (startYear == null)  {
            throw new MissingStartYear(thesaurus);
        }
        if (startYear.equals(BigInteger.ZERO)) {
            throw new StartYearCannotBeZero(thesaurus);
        }
        return Year.of(startYear.intValue());
    }

    private String getDescription() {
        return calendar.getDescription();
    }

    private String getMRID() {
        return calendar.getMRID();
    }

    private String getDayTypeNameById(BigInteger id) {
        String dayTypeName = dayTypes.get(id);
        if (dayTypeName == null) {
            throw new NoDayTypeForId(thesaurus, id.intValue());
        }
        return dayTypeName;
    }

    private String getPeriodNameById(BigInteger id) {
        String periodName = periods.get(id);
        if (periodName == null) {
            throw new NoPeriodForId(thesaurus, id.intValue());
        }
        return periodName;
    }

    private String getEventNameById(BigInteger id) {
        String eventName = events.get(id);
        if (eventName == null) {
            throw new NoEventWithId(thesaurus, id.intValue());
        }
        return eventName;
    }



}
