package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import com.elster.jupiter.util.time.Clock;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.logging.UnrelatedLoggingEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (17:20)
 */
@RunWith(MockitoJUnitRunner.class)
public class UnrelatedLoggingEventTest {

    @Mock
    public Clock clock;
    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void setupServiceProvider () {
        when(this.clock.now()).thenReturn(new DateTime(2014, 5, 2, 1, 40, 0).toDate()); // Set some default
        when(this.serviceProvider.clock()).thenReturn(this.clock);
    }

    @Test
    public void testCategory () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider, LogLevel.DEBUG, "testCategory");

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.LOGGING);
    }

    @Test
    public void testOccurrenceTimestampForDefaultConstructor () {
        Date now = new DateTime(2012, Calendar.NOVEMBER, 7, 17, 22, 01, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(now);
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider, LogLevel.DEBUG, "testOccurrenceTimestampForDefaultConstructor");

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testOccurrenceTimestamp () {
        Date now = new DateTime(2012, Calendar.NOVEMBER, 6, 17, 22, 01, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(now);

        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider, LogLevel.INFO, "testOccurrenceTimestamp");

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsLoggingRelated () {
        // Business method
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider, LogLevel.DEBUG, "testLogLevel");

        // Asserts
        assertThat(event.isLoggingRelated()).isTrue();
    }

    @Test
    public void testLogLevel () {
        LogLevel expectedLogLevel = LogLevel.DEBUG;
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider, expectedLogLevel, "testLogLevel");

        // Business method
        LogLevel logLevel = event.getLogLevel();

        // Asserts
        assertThat(logLevel).isEqualTo(expectedLogLevel);
    }

    @Test
    public void testLogMessage () {
        String expectedLogMessage = "testLogMessage";
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider, LogLevel.DEBUG, expectedLogMessage);

        // Business method
        String logMessage = event.getLogMessage();

        // Asserts
        assertThat(logMessage).isEqualTo(expectedLogMessage);
    }

    @Test
    public void testIsNotDeviceRelated () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider, LogLevel.DEBUG, "testIsNotDeviceRelated");

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();

    }

    @Test
    public void testIsNotConnectionTaskRelated () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider, LogLevel.DEBUG, "testIsNotConnectionTaskRelated");

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();

    }

    @Test
    public void testIsNotComPortRelated () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider, LogLevel.DEBUG, "testIsNotComPortRelated");

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();

    }

    @Test
    public void testIsNotComPortPoolRelated () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider, LogLevel.DEBUG, "testIsNotComPortPoolRelated");

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();

    }

    @Test
    public void testIsNotComTaskExecutionRelated () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider, LogLevel.DEBUG, "testIsNotComTaskExecutionRelated");

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();

    }

    @Test
    public void testToStringDoesNotFail () throws IOException, ClassNotFoundException {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider, LogLevel.INFO, "testSerializationDoesNotFail");

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}