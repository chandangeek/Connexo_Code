package com.elster.jupiter.calendar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.TimeZone;

/**
 * Demonstrates the usage of the {@link CalendarService.CalendarBuilder} API.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-08 (10:01)
 */
public class ForDemonstrationPurposes {
    public static void main(String[] args) {
        CalendarService service = null;
        Calendar robsExample = service
            .newCalendar("Test", TimeZone.getTimeZone("Europe/Brussels"), Year.of(2010))
            .description("Description remains to be completed :-)")
            .mRID("Sample-TOU-rates")
            .addEvent("On peak", 3)
            .addEvent("Off peak", 5)
            .addEvent("Demand response", 97)
            .newDayType("Summer weekday")
                .eventWithCode(3).startsFrom(LocalTime.of(13, 0, 0))
                .event("Off peak").startsFrom(LocalTime.of(20, 0, 0))
                .add()
            .newDayType("Weekend")
                .event("Off peak").startsFrom(LocalTime.MIDNIGHT)
                .add()
            .newDayType("Holiday")
                .event("Off peak").startsFrom(LocalTime.MIDNIGHT)
                .add()
            .newDayType("Winter day")
                .event("On peak").startsFrom(LocalTime.of(5, 0, 0))
                .event("Off peak").startsFrom(LocalTime.of(21, 0, 0))
                .add()
            .newDayType("Demand response")
                .eventWithCode(97).startsFrom(LocalTime.MIDNIGHT)
                .add()
            .addPeriod("Summer", MonthDay.of(5, 1), "Summer weekday", "Summer weekday", "Summer weekday", "Summer weekday", "Summer weekday", "Weekend", "Weekend")
            .addPeriod("Winter", MonthDay.of(11, 1), "Winter day", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day")
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
            .add();
    }

}