package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPort;

import com.elster.jupiter.util.time.Clock;
import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    public DeviceDataService deviceDataService;
    @Mock
    public EngineModelService engineModelService;
    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void setupServiceProvider () {
        when(this.serviceProvider.clock()).thenReturn(this.clock);
        when(this.serviceProvider.engineModelService()).thenReturn(this.engineModelService);
        when(this.serviceProvider.deviceDataService()).thenReturn(this.deviceDataService);
    }

    @Test
    public void testCategory () {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider);

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.LOGGING);
    }

    @Test
    public void testOccurrenceTimestampForDefaultConstructor () {
        Date now = new DateTime(2012, Calendar.NOVEMBER, 15, 14, 10, 01, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(now);

        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider);

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testOccurrenceTimestamp () {
        Date now = new DateTime(2012, Calendar.NOVEMBER, 15, 14, 10, 01, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(now);

        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(null, LogLevel.INFO, "testOccurrenceTimestamp", this.serviceProvider);

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsLoggingRelated () {
        // Business method
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(null, LogLevel.DEBUG, "testLogLevel", this.serviceProvider);

        // Asserts
        assertThat(event.isLoggingRelated()).isTrue();
    }

    @Test
    public void testLogLevel () {
        LogLevel expectedLogLevel = LogLevel.DEBUG;
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(null, expectedLogLevel, "testLogLevel", this.serviceProvider);

        // Business method
        LogLevel logLevel = event.getLogLevel();

        // Asserts
        assertThat(logLevel).isEqualTo(expectedLogLevel);
    }

    @Test
    public void testLogMessage () {
        String expectedLogMessage = "testLogMessage";
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(null, LogLevel.DEBUG, expectedLogMessage, this.serviceProvider);

        // Business method
        String logMessage = event.getLogMessage();

        // Asserts
        assertThat(logMessage).isEqualTo(expectedLogMessage);
    }

    @Test
    public void testIsNotDeviceRelated () {
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(comPort, LogLevel.INFO, "testIsDeviceRelatedBy", this.serviceProvider);

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelatedByDefault () {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider);

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();

    }

    @Test
    public void testIsNotComPortRelatedByDefault () {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider);

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();

    }

    @Test
    public void testIsComPortRelated () {
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(comPort, LogLevel.INFO, "testIsDeviceRelatedBy", this.serviceProvider);

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsNotComPortPoolRelatedByDefault () {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();

    }

    @Test
    public void testIsNotComPortPoolRelatedForOutboundComPorts () {
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.isInbound()).thenReturn(false);
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(comPort, LogLevel.INFO, "testIsDeviceRelatedBy", this.serviceProvider);

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
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(comPort, LogLevel.INFO, "testIsDeviceRelatedBy", this.serviceProvider);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComTaskExecutionRelated () {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider);

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();

    }

    @Test
    public void testToStringDoesNotFailForDefaultObject () throws IOException {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).doesNotMatch(ComPortOperationsLoggingEvent.class.getName() + "@\\d*");
    }

    @Test
    public void testToStringDoesNotFail () throws IOException {
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(comPort, LogLevel.INFO, "testSerializationDoesNotFail", this.serviceProvider);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testSerializationDoesNotFailForDefaultObject () throws IOException {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerializationForDefaultObject () throws IOException, ClassNotFoundException {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(this.serviceProvider);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        ComPortOperationsLoggingEvent restored = (ComPortOperationsLoggingEvent) ois.readObject();

        // Asserts
        assertThat(restored).isNotNull();
    }

    @Test
    public void testSerializationDoesNotFail () throws IOException {
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(comPort, LogLevel.INFO, "testSerializationDoesNotFail", this.serviceProvider);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerialization () throws IOException, ClassNotFoundException {
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        LogLevel expectedLogLevel = LogLevel.INFO;
        String expectedLogMessage = "testRestoreAfterSerialization";
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(comPort, expectedLogLevel, expectedLogMessage, this.serviceProvider);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        ComPortOperationsLoggingEvent restored = (ComPortOperationsLoggingEvent) ois.readObject();

        // Asserts
        assertThat(restored.getLogLevel()).isEqualTo(expectedLogLevel);
        assertThat(restored.getLogMessage()).isEqualTo(expectedLogMessage);
    }

}