package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.logging.ComPortOperationsLoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.comserver.time.Clocks;
import com.energyict.comserver.time.FrozenClock;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.ports.ComPortFactory;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.communication.tasks.ServerConnectionTaskFactory;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.logging.ComPortOperationsLoggingEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (14:09)
 */
public class ComPortOperationsLoggingEventTest {

    private static final long COMPORT_ID = 1;

    @After
    public void resetTimeFactory () throws SQLException {
        Clocks.resetAll();
    }

    @Test
    public void testCategory () {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent();

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.LOGGING);
    }

    @Test
    public void testOccurrenceTimestampForDefaultConstructor () {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.NOVEMBER, 15, 14, 10, 01, 0);  // Random pick
        Clocks.setAppServerClock(frozenClock);
        Date now = Clocks.getAppServerClock().now();

        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent();

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testOccurrenceTimestamp () {
        FrozenClock eventClock = FrozenClock.frozenOn(2012, Calendar.NOVEMBER, 15, 14, 10, 01, 0);  // Random pick
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.NOVEMBER, 16, 14, 10, 01, 0);  // 1 day later
        Clocks.setAppServerClock(frozenClock);
        Date now = eventClock.now();

        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(now, null, LogLevel.INFO, "testOccurrenceTimestamp");

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsLoggingRelated () {
        // Business method
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(Clocks.getAppServerClock().now(), null, LogLevel.DEBUG, "testLogLevel");

        // Asserts
        assertThat(event.isLoggingRelated()).isTrue();
    }

    @Test
    public void testLogLevel () {
        LogLevel expectedLogLevel = LogLevel.DEBUG;
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(Clocks.getAppServerClock().now(), null, expectedLogLevel, "testLogLevel");

        // Business method
        LogLevel logLevel = event.getLogLevel();

        // Asserts
        assertThat(logLevel).isEqualTo(expectedLogLevel);
    }

    @Test
    public void testLogMessage () {
        String expectedLogMessage = "testLogMessage";
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(Clocks.getAppServerClock().now(), null, LogLevel.DEBUG, expectedLogMessage);

        // Business method
        String logMessage = event.getLogMessage();

        // Asserts
        assertThat(logMessage).isEqualTo(expectedLogMessage);
    }

    @Test
    public void testIsNotDeviceRelated () {
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(Clocks.getAppServerClock().now(), comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelatedByDefault () {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent();

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();

    }

    @Test
    public void testIsNotComPortRelatedByDefault () {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent();

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();

    }

    @Test
    public void testIsComPortRelated () {
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(Clocks.getAppServerClock().now(), comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsNotComPortPoolRelatedByDefault () {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent();

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();

    }

    @Test
    public void testIsNotComPortPoolRelatedForOutboundComPorts () {
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.isInbound()).thenReturn(false);
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(Clocks.getAppServerClock().now(), comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

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
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(Clocks.getAppServerClock().now(), comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComTaskExecutionRelated () {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent();

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();

    }

    @Test
    public void testToStringDoesNotFailForDefaultObject () throws IOException {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent();

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).doesNotMatch(ComPortOperationsLoggingEvent.class.getName() + "@\\d*");
    }

    @Test
    public void testToStringDoesNotFail () throws IOException {
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(Clocks.getAppServerClock().now(), comPort, LogLevel.INFO, "testSerializationDoesNotFail");

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testSerializationDoesNotFailForDefaultObject () throws IOException {
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerializationForDefaultObject () throws IOException, ClassNotFoundException {
        ComPortFactory comPortFactory = mock(ComPortFactory.class);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        ServerManager manager = mock(ServerManager.class);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        ManagerFactory.setCurrent(manager);
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent();

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
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(Clocks.getAppServerClock().now(), comPort, LogLevel.INFO, "testSerializationDoesNotFail");

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
        ComPortFactory comPortFactory = mock(ComPortFactory.class);
        when(comPortFactory.find((int) COMPORT_ID)).thenReturn(comPort);
        ServerManager manager = mock(ServerManager.class);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        ManagerFactory.setCurrent(manager);
        LogLevel expectedLogLevel = LogLevel.INFO;
        String expectedLogMessage = "testRestoreAfterSerialization";
        ComPortOperationsLoggingEvent event = new ComPortOperationsLoggingEvent(Clocks.getAppServerClock().now(), comPort, expectedLogLevel, expectedLogMessage);

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