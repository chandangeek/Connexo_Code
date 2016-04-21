package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
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
import java.time.Year;
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
                .add();
        assertThat(calendar.getName()).isEqualTo("test");
        assertThat(calendar.getDescription()).isEqualTo("Description remains to be completed :-)");
        assertThat(calendar.getTimeZone()).isEqualTo(TimeZone.getTimeZone("Europe/Brussels"));
        assertThat(calendar.getStartYear()).isEqualTo(Year.of(2010));

    }

}
