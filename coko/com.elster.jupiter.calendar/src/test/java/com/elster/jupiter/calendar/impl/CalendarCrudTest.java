/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransitionSpec;
import com.elster.jupiter.calendar.RecurrentExceptionalOccurrence;
import com.elster.jupiter.calendar.RecurrentPeriodTransitionSpec;
import com.elster.jupiter.calendar.impl.importers.CalendarImportResult;
import com.elster.jupiter.calendar.impl.importers.CalendarProcessor;
import com.elster.jupiter.calendar.impl.xmlbinding.Calendars;
import com.elster.jupiter.calendar.impl.xmlbinding.XmlCalendar;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.nls.Thesaurus;

import com.google.common.collect.ImmutableMap;

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
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

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

    @Test
    @Transactional
    public void testCalendarCrudByBuilder() {
        createTestCalendar("Test");

        List<Calendar> calendars = getCalendarService().findAllCalendars();
        assertThat(calendars.size()).isEqualTo(1);

        Calendar calendar = calendars.get(0);

        assertThat(calendar.getName()).isEqualTo("Test");
        assertThat(calendar.getDescription()).isEqualTo("Description remains to be completed :-)");
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

    private Calendar createTestCalendar(String name) {
        Category category = getCalendarService().findCategoryByName(OutOfTheBoxCategory.TOU.getDefaultDisplayName()).orElseThrow(AssertionError::new);
        EventSet testEventSet = createTestEventSet();
        return getCalendarService().newCalendar(name, Year.of(2010), testEventSet)
                .endYear(Year.of(2018))
                .category(category)
                .description("Description remains to be completed :-)")
                .mRID(name + "-mrid")
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
    }

    private EventSet createTestEventSet() {
        return getCalendarService().newEventSet("eventset")
                .addEvent("On peak").withCode(3)
                .addEvent("Off peak").withCode(5)
                .addEvent("Demand response").withCode(97)
                .add();
    }

    @Test
    @Transactional
    public void testNullName() {
        try {
            CalendarService service = getCalendarService();

            EventSet eventSet = createTestEventSet();

            service.newCalendar(null, Year.of(2010), eventSet)
                    .description("Description remains to be completed :-)")
                    .endYear(Year.of(2018))
                    .mRID("Sample-TOU-rates")
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

            EventSet eventSet = createTestEventSet();

            service.newCalendar("", Year.of(2010), eventSet)
                    .description("Description remains to be completed :-)")
                    .endYear(Year.of(2018))
                    .mRID("Sample-TOU-rates")
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
    public void testNoStartYear() {
        try {
            CalendarService service = getCalendarService();

            EventSet eventSet = createTestEventSet();

            service.newCalendar("test", null, eventSet)
                    .description("Description remains to be completed :-)")
                    .endYear(Year.of(2018))
                    .mRID("Sample-TOU-rates")
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

            EventSet eventSet = createTestEventSet();

            service.newCalendar("test", Year.of(2010), eventSet)
                    .description("Description remains to be completed :-)")
                    .endYear(Year.of(2018))
                    .mRID("Sample-TOU-rates")
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

            EventSet eventSet = createTestEventSet();

            service.newCalendar("test", Year.of(2010), eventSet)
                    .description("Description remains to be completed :-)")
                    .endYear(Year.of(2018))
                    .mRID("Sample-TOU-rates")
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
            calendars.get(0);
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
            JAXBContext jaxbContext = JAXBContext.newInstance(XmlCalendar.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            Schema schema = schemaFactory.newSchema(new File(getClass().getClassLoader()
                    .getResource("calendar-import-format.xsd")
                    .toURI()));
            unmarshaller.setSchema(schema);
            in = new FileInputStream(new File(getClass().getClassLoader()
                    .getResource("com.elster.jupiter.calendar.impl/calendar-import-format.xml")
                    .toURI()));
            Calendars xmlContent = (Calendars) unmarshaller.unmarshal(in);
            CalendarProcessor processor = new CalendarProcessor(getCalendarService(), Clock.systemDefaultZone(), getCalendarService().getThesaurus());
            CalendarImportResult calendarImportResult = processor.process(xmlContent);

            assertThat(calendarImportResult.getEventSets()).hasSize(1);
            EventSet eventSet = calendarImportResult.getEventSets().get(0);
            assertThat(eventSet.getName()).isEqualTo("Residential TOU Example");
            assertThat(eventSet.getEvents()).hasSize(3);
            assertThat(eventSet.getEvents()
                    .stream()
                    .collect(Collectors.toMap(
                            Event::getName,
                            Event::getCode
                    ))
            ).isEqualTo(
                    ImmutableMap.of(
                            "On Peak", 3L,
                            "Off Peak", 5L,
                            "Demand response", 97L
                    )
            );
            assertThat(calendarImportResult.getCalendars()).hasSize(1);
            Calendar calendar = calendarImportResult.getCalendars().get(0);
            assertThat(calendar.getCategory()).isNotNull();
            assertThat(calendar.getCategory().getName()).isEqualTo("Time of use");
            assertThat(calendar.getName()).isEqualTo("Residential TOU Example");
            assertThat(calendar.getMRID()).isEqualTo("optional");
            assertThat(calendar.getStartYear()).isEqualTo(Year.of(2010));
            assertThat(calendar.getDayTypes()).hasSize(5);
            DayType summerWeekday = calendar.getDayTypes().get(0);
            assertThat(summerWeekday.getName()).isEqualTo("summer weekday");
            assertThat(summerWeekday.getEventOccurrences()).hasSize(3);
            assertThat(summerWeekday.getEventOccurrences()
                    .stream()
                    .map(EventOccurrence::getFrom)
                    .collect(Collectors.toList())
            ).isEqualTo(
                    Arrays.asList(LocalTime.of(0, 0), LocalTime.of(13, 0), LocalTime.of(20, 0))
            );
            assertThat(summerWeekday.getEventOccurrences()
                    .stream()
                    .map(EventOccurrence::getEvent)
                    .map(Event::getName)
                    .collect(Collectors.toList())
            ).isEqualTo(
                    Arrays.asList("Off Peak", "On Peak", "Off Peak")
            );
            DayType weekend = calendar.getDayTypes().get(1);
            assertThat(weekend.getName()).isEqualTo("weekend");
            assertThat(weekend.getEventOccurrences()).hasSize(1);
            assertThat(weekend.getEventOccurrences()
                    .stream()
                    .map(EventOccurrence::getFrom)
                    .collect(Collectors.toList())
            ).isEqualTo(
                    Arrays.asList(LocalTime.of(0, 0))
            );
            assertThat(weekend.getEventOccurrences()
                    .stream()
                    .map(EventOccurrence::getEvent)
                    .map(Event::getName)
                    .collect(Collectors.toList())
            ).isEqualTo(
                    Arrays.asList("Off Peak")
            );

            assertThat(calendar.getPeriods()).hasSize(2);
            Period summer = calendar.getPeriods().get(0);
            assertThat(summer.getName()).isEqualTo("Summer");
            assertThat(summer.getDayType(DayOfWeek.MONDAY).getName()).isEqualTo("summer weekday");
            assertThat(summer.getDayType(DayOfWeek.TUESDAY).getName()).isEqualTo("summer weekday");
            assertThat(summer.getDayType(DayOfWeek.WEDNESDAY).getName()).isEqualTo("summer weekday");
            assertThat(summer.getDayType(DayOfWeek.THURSDAY).getName()).isEqualTo("summer weekday");
            assertThat(summer.getDayType(DayOfWeek.FRIDAY).getName()).isEqualTo("summer weekday");
            assertThat(summer.getDayType(DayOfWeek.SATURDAY).getName()).isEqualTo("weekend");
            assertThat(summer.getDayType(DayOfWeek.SUNDAY).getName()).isEqualTo("weekend");

            assertThat(calendar.getPeriodTransitionSpecs()).hasSize(2);
            assertThat(calendar.getPeriodTransitionSpecs().get(0)).isInstanceOf(RecurrentPeriodTransitionSpec.class);
            RecurrentPeriodTransitionSpec periodTransition1 = (RecurrentPeriodTransitionSpec) calendar.getPeriodTransitionSpecs().get(0);
            assertThat(periodTransition1.getOccurrence()).isEqualTo(MonthDay.of(5, 1));
            assertThat(periodTransition1.getPeriod().getName()).isEqualTo("Summer");
            assertThat(calendar.getPeriodTransitionSpecs().get(1)).isInstanceOf(RecurrentPeriodTransitionSpec.class);
            RecurrentPeriodTransitionSpec periodTransition2 = (RecurrentPeriodTransitionSpec) calendar.getPeriodTransitionSpecs().get(1);
            assertThat(periodTransition2.getOccurrence()).isEqualTo(MonthDay.of(11, 1));
            assertThat(periodTransition2.getPeriod().getName()).isEqualTo("Winter");

            assertThat(calendar.getExceptionalOccurrences()).hasSize(12);
            assertThat(calendar.getExceptionalOccurrences()
                    .stream()
                    .filter(RecurrentExceptionalOccurrence.class::isInstance)
                    .map(RecurrentExceptionalOccurrence.class::cast)
                    .map(RecurrentExceptionalOccurrence::getOccurrence)
                    .anyMatch(MonthDay.of(11, 11)::equals)
            ).isTrue();
            assertThat(calendar.getExceptionalOccurrences()
                    .stream()
                    .filter(FixedExceptionalOccurrence.class::isInstance)
                    .map(FixedExceptionalOccurrence.class::cast)
                    .map(FixedExceptionalOccurrence::getOccurrence)
                    .anyMatch(LocalDate.of(2016, 11, 24)::equals)
            ).isTrue();

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
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

        createTestCalendar("Test");

        List<Calendar> calendars = service.findAllCalendars();
        assertThat(calendars.size()).isEqualTo(1);

        Calendar calendar = calendars.get(0);

        calendar.delete();
        calendars = service.findAllCalendars();
        assertThat(calendars.size()).isEqualTo(0);
    }

    @Test
    @Transactional
    public void testMakeObsolete() {
        Calendar calendar = createTestCalendar("Test");

        // Business method
        calendar.makeObsolete();

        // Asserts
        Optional<Calendar> foundCalendar;

        // Asserts that calendar is available by id
        foundCalendar = getCalendarService().findCalendar(calendar.getId());
        assertThat(foundCalendar).isPresent();
        assertThat(foundCalendar.get()).isEqualTo(calendar);
        assertThat(foundCalendar.get().getObsoleteTime()).isPresent();

        // Asserts that calendar is NOT available by mrid
        foundCalendar = getCalendarService().findCalendarByMRID(calendar.getMRID());
        assertThat(foundCalendar).isEmpty();

        // Asserts that calendar is NOT available by name
        foundCalendar = getCalendarService().findCalendarByName(calendar.getName());
        assertThat(foundCalendar).isEmpty();
    }

    @Test
    @Transactional
    public void testGetObsoleteTime() {
        Calendar calendar = createTestCalendar("Test_NonObsolete");
        Calendar obsoleteCalendar = createTestCalendar("Test_Obsolete");

        // Business method
        obsoleteCalendar.makeObsolete();

        // Asserts
        assertThat(getCalendarService().findCalendar(calendar.getId()).get().getObsoleteTime()).isEmpty();
        assertThat(getCalendarService().findCalendar(obsoleteCalendar.getId()).get().getObsoleteTime()).isPresent();
    }
}
