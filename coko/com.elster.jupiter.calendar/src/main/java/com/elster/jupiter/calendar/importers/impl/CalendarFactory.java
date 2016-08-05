package com.elster.jupiter.calendar.importers.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.MessageSeeds;
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
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Created by igh on 10/05/2016.
 */
public class CalendarFactory {

    private final CalendarService service;
    private final Thesaurus thesaurus;

    private com.elster.jupiter.calendar.impl.xmlbinding.Calendar calendar;
    private Map<BigInteger, String> events;
    private Map<BigInteger, String> dayTypes;
    private Map<BigInteger, String> periods;

    @Inject
    public CalendarFactory(CalendarService service, Thesaurus thesaurus) {
        this.service = service;
        this.thesaurus = thesaurus;
    }

    public com.elster.jupiter.calendar.Calendar getCalendar(com.elster.jupiter.calendar.impl.xmlbinding.Calendar calendar) {
        this.calendar = calendar;

        CalendarService.CalendarBuilder builder = service.findCalendarByMRID(calendar.getMRID())
                .map(c -> c.redefine()
                        .name(getCalendarName())
                        .timeZone(getTimeZone())
                        .startYear(getStartYear())
                        .description(getDescription())
                        .mRID(getMRID()))
                .orElseGet(() -> service.newCalendar(
                        getCalendarName(),
                        getTimeZone(),
                        getStartYear())
                        .description(getDescription()).mRID(getMRID()));

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
                throw new CalendarParserException(thesaurus, MessageSeeds.YEAR_NOT_ALLOWED_FOR_RECURRING_TRANSITIONS);
            }
            if (!recurring && (transition.getYear() == null)) {
                throw new CalendarParserException(thesaurus, MessageSeeds.YEAR_REQUIRED_FOR_NOT_RECURRING_TRANSITIONS);
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
        try {
            return ((BigInteger) code).intValue();
        } catch (ClassCastException e) {
            throw new CalendarParserException(thesaurus, MessageSeeds.INVALID_EVENT_CODE, code);
        }
    }

    private BigInteger getEventId(Event event) {
        Object code = findProperty(event, "id");
        try {
            return (BigInteger) code;
        } catch (ClassCastException e) {
            throw new CalendarParserException(thesaurus, MessageSeeds.INVALID_EVENT_ID, code);
        }
    }

    private Object findProperty(Event event, String name) {
        Optional<Object> result = event.getContent().stream().filter(e -> e instanceof JAXBElement)
                .filter(e -> ((JAXBElement) e).getName().toString().equals(name))
                .map(e -> ((JAXBElement) e).getValue()).findFirst();
        if (!result.isPresent()) {
            throw new CalendarParserException(thesaurus, MessageSeeds.PROPERTY_NOT_FOUND_ON_EVENT, name);
        }
        return result.get();
    }

    private boolean isEmpty(String value) {
        return (value == null) || ("".equals(value));
    }

    private String getCalendarName() {
        String calendarName = calendar.getName();
        if (isEmpty(calendarName)) {
            throw new CalendarParserException(thesaurus, MessageSeeds.MISSING_CALENAR_NAME);
        }
        return calendarName;
    }

    private TimeZone getTimeZone() {
        String timeZoneName = calendar.getTimezone();
        if (isEmpty(timeZoneName)) {
            throw new CalendarParserException(thesaurus, MessageSeeds.MISSING_TIMEZONE);
        }
        try {
            return TimeZone.getTimeZone(ZoneId.of(timeZoneName));
        } catch (DateTimeException e) {
            throw new CalendarParserException(thesaurus, MessageSeeds.NO_TIMEZONE_FOUND_WITH_ID, timeZoneName);
        }
    }

    private Year getStartYear() {
        BigInteger startYear = calendar.getStartYear();
        if (startYear == null)  {
            throw new CalendarParserException(thesaurus, MessageSeeds.MISSING_STARTYEAR);
        }
        if (startYear.equals(BigInteger.ZERO)) {
            throw new CalendarParserException(thesaurus, MessageSeeds.STARTYEAR_CANNOT_BE_ZERO);
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
            throw new CalendarParserException(thesaurus, MessageSeeds.NO_DAYTYPE_DEFINED_WITH_ID, id.intValue());
        }
        return dayTypeName;
    }

    private String getPeriodNameById(BigInteger id) {
        String periodName = periods.get(id);
        if (periodName == null) {
            throw new CalendarParserException(thesaurus, MessageSeeds.NO_PERIOD_DEFINED_WITH_ID, id.intValue());
        }
        return periodName;
    }

    private String getEventNameById(BigInteger id) {
        String eventName = events.get(id);
        if (eventName == null) {
            throw new CalendarParserException(thesaurus, MessageSeeds.NO_EVENT_DEFINED_WITH_ID, id.intValue());
        }
        return eventName;
    }



}
