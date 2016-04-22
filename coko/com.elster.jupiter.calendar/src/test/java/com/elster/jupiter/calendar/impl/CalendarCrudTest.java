package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by igh on 21/04/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarCrudTest {

    private static CalendarInMemoryBootstrapModule inMemoryBootstrapModule = new CalendarInMemoryBootstrapModule();

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    private CalendarService getCalendarService() {
        return inMemoryBootstrapModule.getCalendarService();
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testCalendarCrudByBuilder() {
        CalendarService service = getCalendarService();
        service.newCalendar("Test", TimeZone.getTimeZone("Europe/Brussels"), Year.of(2010))
                .description("Description remains to be completed :-)")
                .endYear(Year.of(2018))
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
                .addPeriod("Summer", "Summer weekday", "Summer weekday", "Summer weekday", "Summer weekday", "Summer weekday", "Weekend", "Weekend")
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
                .add();


        List<Calendar> calendars = service.findAllCalendars();
        assertThat(calendars.size()).isEqualTo(1);

        Calendar calendar = calendars.get(0);


        assertThat(calendar.getName()).isEqualTo("Test");
        assertThat(calendar.getDescription()).isEqualTo("Description remains to be completed :-)");
        assertThat(calendar.getTimeZone()).isEqualTo(TimeZone.getTimeZone("Europe/Brussels"));
        assertThat(calendar.getStartYear()).isEqualTo(Year.of(2010));
        assertThat(calendar.getEndYear()).isEqualTo(Year.of(2018));
        List<Event> events = calendar.getEvents();
        assertThat(events.size()).isEqualTo(3);
        Event onPeak = events.get(0);
        assertThat(onPeak.getName()).isEqualTo("On peak");
        assertThat(onPeak.getCode()).isEqualTo(3);
        assertThat(events.get(1).getName()).isEqualTo("Off peak");
        assertThat(events.get(1).getCode()).isEqualTo(5);
        assertThat(events.get(2).getName()).isEqualTo("Demand response");
        assertThat(events.get(2).getCode()).isEqualTo(97);

        List<DayType> dayTypes = calendar.getDayTypes();
        assertThat(dayTypes.size()).isEqualTo(5);
        DayType daytype1 = dayTypes.get(0);
        assertThat(daytype1.getName()).isEqualTo("Summer weekday");
        List<EventOccurrence> eventOccurrences = daytype1.getEventOccurrences();
        assertThat(eventOccurrences.size()).isEqualTo(2);
        EventOccurrence occurrence1 = eventOccurrences.get(0);
        assertThat(occurrence1.getFrom()).isEqualTo(LocalTime.of(13, 0, 0));
        assertThat(occurrence1.getEvent().getName()).isEqualTo("On peak");

        EventOccurrence occurrence2 = eventOccurrences.get(1);
        assertThat(occurrence2.getFrom()).isEqualTo(LocalTime.of(20, 0, 0));
        assertThat(occurrence2.getEvent().getName()).isEqualTo("Off peak");

        List<Period> periods = calendar.getPeriods();
        assertThat(periods.size()).isEqualTo(2);
        Period period1 = periods.get(0);
        assertThat(period1.getName()).isEqualTo("Summer");
        assertThat(period1.getDayType(DayOfWeek.MONDAY).getName()).isEqualTo("Summer weekday");
        assertThat(period1.getDayType(DayOfWeek.TUESDAY).getName()).isEqualTo("Summer weekday");
        assertThat(period1.getDayType(DayOfWeek.WEDNESDAY).getName()).isEqualTo("Summer weekday");
        assertThat(period1.getDayType(DayOfWeek.THURSDAY).getName()).isEqualTo("Summer weekday");
        assertThat(period1.getDayType(DayOfWeek.FRIDAY).getName()).isEqualTo("Summer weekday");
        assertThat(period1.getDayType(DayOfWeek.SATURDAY).getName()).isEqualTo("Weekend");
        assertThat(period1.getDayType(DayOfWeek.SUNDAY).getName()).isEqualTo("Weekend");
        //todo check period transitions and exceptional occurrences


    }

}
