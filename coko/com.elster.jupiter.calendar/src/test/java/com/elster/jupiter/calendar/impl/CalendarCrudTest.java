package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.assertj.core.api.Assertions.assertThat;

import javax.validation.constraints.AssertTrue;
import java.time.LocalTime;
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
    public void testRequirementNodeCrud() {
        CalendarService service = getCalendarService();
        Calendar calendar = service.newCalendar("test", TimeZone.getTimeZone("Europe/Brussels"), Year.of(2010))
                .description("Description remains to be completed :-)")
                .mRID("Sample-TOU-rates")
                .endYear(Year.of(2018))
                .addEvent("On peak", 3)
                .addEvent("Off peak", 5)
                .addEvent("Demand response", 97)
                .newDayType("Summer weekday")
                    .eventWithCode(3).startsFrom(LocalTime.of(13, 0, 0))
                    .event("Off peak").startsFrom(LocalTime.of(20, 0, 0))
                    .add()
                .add();
        assertThat(calendar.getName()).isEqualTo("test");
        assertThat(calendar.getDescription()).isEqualTo("Description remains to be completed :-)");
        assertThat(calendar.getTimeZone()).isEqualTo(TimeZone.getTimeZone("Europe/Brussels"));
        assertThat(calendar.getStartYear()).isEqualTo(Year.of(2010));
        assertThat(calendar.getEndYear()).isEqualTo(Year.of(2018));
        List<Event> events = calendar.getEvents();
        assertThat(events.size()).isEqualTo(3);
        assertThat(events.get(0).getName()).isEqualTo("On peak");
        assertThat(events.get(0).getCode()).isEqualTo(3);
        assertThat(events.get(1).getName()).isEqualTo("Off peak");
        assertThat(events.get(1).getCode()).isEqualTo(5);
        assertThat(events.get(2).getName()).isEqualTo("Demand response");
        assertThat(events.get(2).getCode()).isEqualTo(97);
    }

}
