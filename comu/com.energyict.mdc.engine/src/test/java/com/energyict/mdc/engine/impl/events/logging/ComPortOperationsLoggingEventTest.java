package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPort;

import java.time.Clock;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.logging.ComPortOperationsLoggingEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (14:09)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComPortOperationsLoggingEventTest {

    private static final long COMPORT_ID = 1;

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
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider, mock(ComPort.class), LogLevel.DEBUG, "testCategory");

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.LOGGING);
    }

    @Test
    public void testOccurrenceTimestampForDefaultConstructor () {
        Date now = new DateTime(2012, Calendar.NOVEMBER, 15, 14, 10, 01, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(now);

        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider, mock(ComPort.class), LogLevel.DEBUG, "testOccurrenceTimestampForDefaultConstructor");

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testOccurrenceTimestamp () {
        Date now = new DateTime(2012, Calendar.NOVEMBER, 15, 14, 10, 01, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(now);

        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider, null, LogLevel.INFO, "testOccurrenceTimestamp");

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsLoggingRelated () {
        // Business method
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider, null, LogLevel.DEBUG, "testLogLevel");

        // Asserts
        assertThat(event.isLoggingRelated()).isTrue();
    }

    @Test
    public void testLogLevel () {
        LogLevel expectedLogLevel = LogLevel.DEBUG;
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider, null, expectedLogLevel, "testLogLevel");

        // Business method
        LogLevel logLevel = event.getLogLevel();

        // Asserts
        assertThat(logLevel).isEqualTo(expectedLogLevel);
    }

    @Test
    public void testLogMessage () {
        String expectedLogMessage = "testLogMessage";
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider, null, LogLevel.DEBUG, expectedLogMessage);

        // Business method
        String logMessage = event.getLogMessage();

        // Asserts
        assertThat(logMessage).isEqualTo(expectedLogMessage);
    }

    @Test
    public void testIsNotDeviceRelated () {
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider, comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelatedByDefault () {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider, mock(ComPort.class), LogLevel.DEBUG, "testIsNotConnectionTaskRelatedByDefault");

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();

    }

    @Test
    public void testIsComPortRelated () {
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider, comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsNotComPortPoolRelatedByDefault () {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider, mock(ComPort.class), LogLevel.DEBUG, "testIsNotComPortPoolRelatedByDefault");

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();

    }

    @Test
    public void testIsNotComPortPoolRelatedForOutboundComPorts () {
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.isInbound()).thenReturn(false);
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider, comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsComPortPoolRelatedForInboundComPorts () {
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.isInbound()).thenReturn(true);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider, comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComTaskExecutionRelated () {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider, mock(ComPort.class), LogLevel.DEBUG, "testIsNotComTaskExecutionRelated");

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();

    }

    @Test
    public void testToStringDoesNotFail () throws IOException {
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider, comPort, LogLevel.INFO, "testSerializationDoesNotFail");

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}