package com.elster.jupiter.calendar.importers.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.calendar.impl.xmlbinding.Calendar;
import com.elster.jupiter.calendar.impl.xmlbinding.DayType;
import com.elster.jupiter.calendar.impl.xmlbinding.Event;
import com.elster.jupiter.calendar.impl.xmlbinding.Period;
import com.elster.jupiter.calendar.impl.xmlbinding.RangeTime;
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
import java.util.TimeZone;

/**
 * Created by igh on 10/05/2016.
 */
public class CalendarFactory {

    private final CalendarService service;
    private final Thesaurus thesaurus;

    private com.elster.jupiter.calendar.impl.xmlbinding.Calendar calendar;
    private Map<BigInteger, String> dayTypes;

    @Inject
    public CalendarFactory(CalendarService service, Thesaurus thesaurus) {
        this.service = service;
        this.thesaurus = thesaurus;
    }

    public com.elster.jupiter.calendar.Calendar getCalendar(com.elster.jupiter.calendar.impl.xmlbinding.Calendar calendar) {
        this.calendar = calendar;
        CalendarService.CalendarBuilder builder =
            service.newCalendar(
                    getCalendarName(),
                    getTimeZone(),
                    getStartYear())
                   .description(getDescription()).mRID(getMRID());

        for (Event event : calendar.getEvents().getEvent()) {
            builder.addEvent(getEventName(event), getEventCode(event));
        }

        dayTypes = new HashMap<BigInteger, String>(); // needed for periods (has a link to daytypes on id) and builder api requires daytype name
        for (DayType dayType : calendar.getDayTypes().getDayType()) {
            BigInteger id = dayType.getId();
            String dayTypeName = dayType.getName();
            dayTypes.put(id, dayTypeName);
            CalendarService.DayTypeBuilder dayTypeBuilder =  builder.newDayType(dayTypeName);
            for (RangeTime rangeTime : dayType.getRanges().getRangeTime()) {
                int event = rangeTime.getEvent().intValue();
                LocalTime localTime =  LocalTime.of(
                        rangeTime.getFrom().getHour().intValue(),
                        rangeTime.getFrom().getMinute().intValue(),
                        rangeTime.getFrom().getSecond().intValue());
                dayTypeBuilder.eventWithCode(event).startsFrom(localTime);
            }
        }

        for (Period period : calendar.getPeriods().getPeriod()) {
            builder.addPeriod(
                    period.getName(),
                    getDayTypeNameById(period.getWeekTemplate().getMonday().getDayType()),
                    getDayTypeNameById(period.getWeekTemplate().getTuesday().getDayType()),
                    getDayTypeNameById(period.getWeekTemplate().getWednesday().getDayType()),
                    getDayTypeNameById(period.getWeekTemplate().getThursday().getDayType()),
                    getDayTypeNameById(period.getWeekTemplate().getFriday().getDayType()),
                    getDayTypeNameById(period.getWeekTemplate().getSaturday().getDayType()),
                    getDayTypeNameById(period.getWeekTemplate().getSunday().getDayType()));
        }






                /*builder.addPeriod("Summer", "Summer weekday", "Summer weekday", "Summer weekday", "Summer weekday", "Summer weekday", "Weekend", "Weekend")
                .addPeriod("Winter", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day")
                .on(MonthDay.of(5, 1)).transitionTo("Summer")
                .on(MonthDay.of(11, 1)).transitionTo("Winter")
                .except("Holiday")
                .occursOnceOn(LocalDate.of(2016, 1, 18))
                .occursOnceOn(LocalDate.of(2016, 2, 15))
                .occursOnceOn(LocalDate.of(2016, 5, 30))
                .occursAlwaysOn(MonthDay.of(7, 4))
                .occursOnceOn(LocalDate.of(2016, 9, 5))
                .occursOnceOn(LocalDate.of(2016, 10, 10))
                .occursAlwaysOn(MonthDay.of(11, 11))
                .occursOnceOn(LocalDate.of(2016, 11, 24))
                .occursAlwaysOn(MonthDay.of(12, 25))
                .occursAlwaysOn(MonthDay.of(12, 26))
                .add()
                .add();*/
        return null;
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

    private Object findProperty(Event event, String name) {
        Optional<Object> result = event.getContent().stream().filter(e -> e instanceof JAXBElement)
                .filter(e -> ((JAXBElement) e).getName().equals(name))
                .map(e -> ((JAXBElement) e).getValue()).findFirst();
        if (!result.isPresent()) {
            throw new CalendarParserException(thesaurus, MessageSeeds.PROPERTY_NOT_FOUND_ON_EVENT, name);
        }
        return (String) result.get();
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
        String timeZoneId = calendar.getTimezone();
        if (isEmpty(timeZoneId)) {
            throw new CalendarParserException(thesaurus, MessageSeeds.MISSING_TIMEZONE);
        }
        return TimeZone.getTimeZone(timeZoneId);
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

}
