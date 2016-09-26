package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransitionSpec;
import com.elster.jupiter.calendar.RecurrentExceptionalOccurrence;
import com.elster.jupiter.calendar.RecurrentPeriodTransitionSpec;
import com.elster.jupiter.calendar.impl.importers.CalendarFactory;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;

import javax.validation.ConstraintViolationException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.List;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by igh on 21/04/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarCrudTest {

    private static CalendarInMemoryBootstrapModule inMemoryBootstrapModule = new CalendarInMemoryBootstrapModule();

    @Mock
    private Thesaurus thesaurus;
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

    private ServerCalendarService getCalendarService() {
        return inMemoryBootstrapModule.getCalendarService();
    }

    private TransactionService getTransactionService() {
        return inMemoryBootstrapModule.getTransactionService();
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

        List<? extends PeriodTransitionSpec> periodTransitionSpecs = calendar.getPeriodTransitionSpecs();
        assertThat(periodTransitionSpecs.size()).isEqualTo(2);

        PeriodTransitionSpec periodTransitionSpec1 = periodTransitionSpecs.get(0);
        PeriodTransitionSpec periodTransitionSpec2 = periodTransitionSpecs.get(1);

        assertThat(periodTransitionSpec1).isInstanceOf(RecurrentPeriodTransitionSpec.class);
        assertThat(periodTransitionSpec2).isInstanceOf(RecurrentPeriodTransitionSpec.class);

        assertThat(((RecurrentPeriodTransitionSpec) periodTransitionSpec1).getOccurrence()).isEqualTo(MonthDay.of(5, 1));
        assertThat(((RecurrentPeriodTransitionSpec) periodTransitionSpec2).getOccurrence()).isEqualTo(MonthDay.of(11, 1));

        List<ExceptionalOccurrence> exceptionalOccurrences = calendar.getExceptionalOccurrences();
        ExceptionalOccurrence exceptionalOccurrence1 = exceptionalOccurrences.get(0);
        ExceptionalOccurrence exceptionalOccurrence2 = exceptionalOccurrences.get(1);
        ExceptionalOccurrence exceptionalOccurrence3 = exceptionalOccurrences.get(2);
        ExceptionalOccurrence exceptionalOccurrence4 = exceptionalOccurrences.get(3);
        ExceptionalOccurrence exceptionalOccurrence5 = exceptionalOccurrences.get(4);
        ExceptionalOccurrence exceptionalOccurrence6 = exceptionalOccurrences.get(5);
        ExceptionalOccurrence exceptionalOccurrence7 = exceptionalOccurrences.get(6);
        ExceptionalOccurrence exceptionalOccurrence8 = exceptionalOccurrences.get(7);
        ExceptionalOccurrence exceptionalOccurrence9 = exceptionalOccurrences.get(8);
        ExceptionalOccurrence exceptionalOccurrence10 = exceptionalOccurrences.get(9);

        assertThat(exceptionalOccurrence1).isInstanceOf(FixedExceptionalOccurrence.class);
        assertThat(exceptionalOccurrence2).isInstanceOf(FixedExceptionalOccurrence.class);
        assertThat(exceptionalOccurrence3).isInstanceOf(FixedExceptionalOccurrence.class);
        assertThat(exceptionalOccurrence4).isInstanceOf(RecurrentExceptionalOccurrence.class);
        assertThat(exceptionalOccurrence5).isInstanceOf(FixedExceptionalOccurrence.class);
        assertThat(exceptionalOccurrence6).isInstanceOf(FixedExceptionalOccurrence.class);
        assertThat(exceptionalOccurrence7).isInstanceOf(RecurrentExceptionalOccurrence.class);
        assertThat(exceptionalOccurrence8).isInstanceOf(FixedExceptionalOccurrence.class);
        assertThat(exceptionalOccurrence9).isInstanceOf(RecurrentExceptionalOccurrence.class);
        assertThat(exceptionalOccurrence10).isInstanceOf(RecurrentExceptionalOccurrence.class);

        assertThat(((FixedExceptionalOccurrence) exceptionalOccurrence1).getOccurrence()).isEqualTo(LocalDate.of(2016, 1, 18));
        assertThat(((FixedExceptionalOccurrence) exceptionalOccurrence2).getOccurrence()).isEqualTo(LocalDate.of(2016, 2, 15));
        assertThat(((FixedExceptionalOccurrence) exceptionalOccurrence3).getOccurrence()).isEqualTo(LocalDate.of(2016, 5, 30));
        assertThat(((RecurrentExceptionalOccurrence) exceptionalOccurrence4).getOccurrence()).isEqualTo(MonthDay.of(7, 4));
        assertThat(((FixedExceptionalOccurrence) exceptionalOccurrence5).getOccurrence()).isEqualTo(LocalDate.of(2016, 9, 5));
        assertThat(((FixedExceptionalOccurrence) exceptionalOccurrence6).getOccurrence()).isEqualTo(LocalDate.of(2016, 10, 10));
        assertThat(((RecurrentExceptionalOccurrence) exceptionalOccurrence7).getOccurrence()).isEqualTo(MonthDay.of(11, 11));
        assertThat(((FixedExceptionalOccurrence) exceptionalOccurrence8).getOccurrence()).isEqualTo(LocalDate.of(2016, 11, 24));
        assertThat(((RecurrentExceptionalOccurrence) exceptionalOccurrence9).getOccurrence()).isEqualTo(MonthDay.of(12, 25));
        assertThat(((RecurrentExceptionalOccurrence) exceptionalOccurrence10).getOccurrence()).isEqualTo(MonthDay.of(12, 26));

    }

    @Test
    @Transactional
    public void testNullName() {
        try {
            CalendarService service = getCalendarService();
            service.newCalendar(null, TimeZone.getTimeZone("Europe/Brussels"), Year.of(2010))
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
                    .on(MonthDay.of(11, 1)).transitionTo("Winter").add();
        } catch (ConstraintViolationException e) {
            assertThat(e.getMessage()).isEqualTo("\n" +
                    "Constraint violation : \n" +
                    "\tMessage : isRequired\n" +
                    "\tClass : com.elster.jupiter.calendar.impl.CalendarImpl\n" +
                    "\tElement : name\n");
        }
    }

    @Test
    @Transactional
    public void testEmptyName() {
        try {
            CalendarService service = getCalendarService();
            service.newCalendar("", TimeZone.getTimeZone("Europe/Brussels"), Year.of(2010))
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
                    .on(MonthDay.of(11, 1)).transitionTo("Winter").add();
        } catch (ConstraintViolationException e) {
            assertThat(e.getMessage()).isEqualTo("\n" +
                    "Constraint violation : \n" +
                    "\tMessage : isRequired\n" +
                    "\tClass : com.elster.jupiter.calendar.impl.CalendarImpl\n" +
                    "\tElement : name\n");
        }
    }

    @Test
    @Transactional
    public void testNullTimeZoneValue() {
        try {
            CalendarService service = getCalendarService();
            service.newCalendar("test", null, Year.of(2010))
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
                    .on(MonthDay.of(11, 1)).transitionTo("Winter").add();
            List<Calendar> calendars = service.findAllCalendars();
            assertThat(calendars.size()).isEqualTo(1);
            Calendar calendar = calendars.get(0);
            assertThat(calendar.getTimeZone()).isEqualTo(null);
        } catch (ConstraintViolationException e) {
            fail("No ConstraintViolationException expected");
        }
    }

    @Test
    @Transactional
    public void testNoStartYear() {
        try {
            CalendarService service = getCalendarService();
            service.newCalendar("test", TimeZone.getTimeZone("Europe/Brussels"), null) .description("Description remains to be completed :-)")
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
                    .on(MonthDay.of(11, 1)).transitionTo("Winter").add();
            fail("ConstraintViolationException expected");
        } catch (ConstraintViolationException e) {
            assertThat(e.getMessage()).isEqualTo("\n" +
                    "Constraint violation : \n" +
                    "\tMessage : isRequired\n" +
                    "\tClass : com.elster.jupiter.calendar.impl.CalendarImpl\n" +
                    "\tElement : startYear\n");
        }
    }

    @Test
    @Transactional
    public void testNoPeriods() {
        try {
            CalendarService service = getCalendarService();
            service.newCalendar("test", TimeZone.getTimeZone("Europe/Brussels"), Year.of(2010)) .description("Description remains to be completed :-)")
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
                    .add();
            fail("ConstraintViolationException expected");
        } catch (ConstraintViolationException e) {
            assertThat(e.getMessage()).isEqualTo("\n" +
                    "Constraint violation : \n" +
                    "\tMessage : periods.required\n" +
                    "\tClass : com.elster.jupiter.calendar.impl.CalendarImpl\n" +
                    "\tElement : periods\n");
        }
    }

    @Test
    @Transactional
    public void testInvalidDayTypeForWednesday() {
        try {
            CalendarService service = getCalendarService();
            service.newCalendar("test", TimeZone.getTimeZone("Europe/Brussels"), Year.of(2010))
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
                    .addPeriod("Summer", "Summer weekday", "Special day", "Summer weekday", "Summer weekday", "Summer weekday", "Weekend", "Weekend")
                    .addPeriod("Winter", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day")
                    .on(MonthDay.of(5, 1)).transitionTo("Summer")
                    .on(MonthDay.of(11, 1)).transitionTo("Winter").add();
            List<Calendar> calendars = service.findAllCalendars();
            assertThat(calendars.size()).isEqualTo(1);
            Calendar calendar = calendars.get(0);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("No daytype defined yet with name 'Special day'");
        }
    }


    @Test
    @Transactional
    public void testFromXml() {
        InputStream in = null;
        try {
            JAXBContext jc = JAXBContext.newInstance(com.elster.jupiter.calendar.impl.xmlbinding.Calendar.class);
            Unmarshaller u = jc.createUnmarshaller();
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            Schema schema =
                    sf.newSchema(new File(getClass().getClassLoader().getResource("calendar-import-format.xsd").toURI()));
            u.setSchema(schema);
            in = new FileInputStream(new File(getClass().getClassLoader().getResource("com.elster.jupiter.calendar.impl/calendar-import-format.xml").toURI()));
            com.elster.jupiter.calendar.impl.xmlbinding.Calendar result =
                    (com.elster.jupiter.calendar.impl.xmlbinding.Calendar) u.unmarshal(in);
            CalendarFactory factory = new CalendarFactory(getCalendarService(), getCalendarService().getThesaurus());
            Calendar calendar = factory.getCalendar(result);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        fail(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
    }

    @Test
    @Transactional
    public void testRemove() {
        CalendarService service = getCalendarService();
        service.newCalendar("Test", TimeZone.getTimeZone("Europe/Brussels"), Year.of(2010))
                .endYear(Year.of(2018))
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

        calendar.delete();
        calendars = service.findAllCalendars();
        assertThat(calendars.size()).isEqualTo(0);


    }


}
