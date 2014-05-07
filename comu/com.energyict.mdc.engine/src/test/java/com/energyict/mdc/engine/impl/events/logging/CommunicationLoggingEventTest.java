package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.logging.CommunicationLoggingEvent;
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
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.communication.tasks.ServerConnectionTaskFactory;
import com.energyict.mdc.protocol.api.device.BaseDevice;
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
 * Tests the {@link com.energyict.mdc.engine.impl.events.logging.CommunicationLoggingEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (11:47)
 */
public class CommunicationLoggingEventTest {

    private static final int DEVICE_ID = 1;
    private static final long COMPORT_ID = DEVICE_ID + 1;
    private static final int CONNECTION_TASK_ID = (int) (COMPORT_ID + 1);

    @After
    public void resetTimeFactory () throws SQLException {
        Clocks.resetAll();
    }

    @Test
    public void testCategory () {
        CommunicationLoggingEvent event = new CommunicationLoggingEvent();

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.LOGGING);
    }

    @Test
    public void testOccurrenceTimestampForDefaultConstructor () {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.NOVEMBER, 7, 17, 22, 01, 0);  // Random pick
        Clocks.setAppServerClock(frozenClock);
        Date now = Clocks.getAppServerClock().now();

        CommunicationLoggingEvent event = new CommunicationLoggingEvent();

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testOccurrenceTimestamp () {
        FrozenClock eventClock = FrozenClock.frozenOn(2012, Calendar.NOVEMBER, 6, 17, 22, 01, 0);  // Random pick
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.NOVEMBER, 7, 17, 22, 01, 0);  // 1 day later
        Clocks.setAppServerClock(frozenClock);
        Date now = eventClock.now();

        CommunicationLoggingEvent event = new CommunicationLoggingEvent(now, null, null, LogLevel.INFO, "testOccurrenceTimestamp");

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsLoggingRelated () {
        // Business method
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(Clocks.getAppServerClock().now(), null, null, LogLevel.DEBUG, "testLogLevel");

        // Asserts
        assertThat(event.isLoggingRelated()).isTrue();
    }

    @Test
    public void testLogLevel () {
        LogLevel expectedLogLevel = LogLevel.DEBUG;
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(Clocks.getAppServerClock().now(), null, null, expectedLogLevel, "testLogLevel");

        // Business method
        LogLevel logLevel = event.getLogLevel();

        // Asserts
        assertThat(logLevel).isEqualTo(expectedLogLevel);
    }

    @Test
    public void testLogMessage () {
        String expectedLogMessage = "testLogMessage";
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(Clocks.getAppServerClock().now(), null, null, LogLevel.DEBUG, expectedLogMessage);

        // Business method
        String logMessage = event.getLogMessage();

        // Asserts
        assertThat(logMessage).isEqualTo(expectedLogMessage);
    }

    @Test
    public void testIsDeviceRelated () {
        BaseDevice device = mock(BaseDevice.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(Clocks.getAppServerClock().now(), connectionTask, comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isTrue();
        assertThat(event.getDevice()).isEqualTo(device);
    }

    @Test
    public void testIsNotConnectionTaskRelatedByDefault () {
        CommunicationLoggingEvent event = new CommunicationLoggingEvent();

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();

    }

    @Test
    public void testIsConnectionTaskRelated () {
        BaseDevice device = mock(BaseDevice.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(Clocks.getAppServerClock().now(), connectionTask, comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isTrue();
        assertThat(event.getConnectionTask()).isEqualTo(connectionTask);

    }

    @Test
    public void testIsNotComPortRelatedByDefault () {
        CommunicationLoggingEvent event = new CommunicationLoggingEvent();

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();

    }

    @Test
    public void testIsComPortRelated () {
        BaseDevice device = mock(BaseDevice.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(Clocks.getAppServerClock().now(), connectionTask, comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsNotComPortPoolRelatedByDefault () {
        CommunicationLoggingEvent event = new CommunicationLoggingEvent();

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();

    }

    @Test
    public void testIsNotComPortPoolRelatedForOutboundComPorts () {
        BaseDevice device = mock(BaseDevice.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.isInbound()).thenReturn(false);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(Clocks.getAppServerClock().now(), connectionTask, comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsComPortPoolRelatedForInboundComPorts () {
        BaseDevice device = mock(BaseDevice.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.isInbound()).thenReturn(true);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(Clocks.getAppServerClock().now(), connectionTask, comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComTaskExecutionRelated () {
        CommunicationLoggingEvent event = new CommunicationLoggingEvent();

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();

    }

    @Test
    public void testSerializationDoesNotFailForDefaultObject () throws IOException {
        CommunicationLoggingEvent event = new CommunicationLoggingEvent();

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
        CommunicationLoggingEvent event = new CommunicationLoggingEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        CommunicationLoggingEvent restored = (CommunicationLoggingEvent) ois.readObject();

        // Asserts
        assertThat(restored).isNotNull();
    }

    @Test
    public void testSerializationDoesNotFail () throws IOException {
        BaseDevice device = mock(BaseDevice.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(Clocks.getAppServerClock().now(), connectionTask, comPort, LogLevel.INFO, "testSerializationDoesNotFail");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerialization () throws IOException, ClassNotFoundException {
        BaseDevice device = mock(BaseDevice.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        ComPortFactory comPortFactory = mock(ComPortFactory.class);
        when(comPortFactory.find((int) COMPORT_ID)).thenReturn(comPort);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        when(connectionTaskFactory.find(CONNECTION_TASK_ID)).thenReturn(connectionTask);
        ServerManager manager = mock(ServerManager.class);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        ManagerFactory.setCurrent(manager);
        LogLevel expectedLogLevel = LogLevel.INFO;
        String expectedLogMessage = "testRestoreAfterSerialization";
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(Clocks.getAppServerClock().now(), connectionTask, comPort, expectedLogLevel, expectedLogMessage);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        CommunicationLoggingEvent restored = (CommunicationLoggingEvent) ois.readObject();

        // Asserts
        assertThat(restored.getLogLevel()).isEqualTo(expectedLogLevel);
        assertThat(restored.getLogMessage()).isEqualTo(expectedLogMessage);
    }

    @Test
    public void testToStringDoesNotFailForDefaultObject () throws IOException {
        CommunicationLoggingEvent event = new CommunicationLoggingEvent();

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).doesNotMatch(CommunicationLoggingEvent.class.getName() + "@\\d*");
    }

    @Test
    public void testToStringDoesNotFail () throws IOException {
        BaseDevice device = mock(BaseDevice.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(Clocks.getAppServerClock().now(), connectionTask, comPort, LogLevel.INFO, "testSerializationDoesNotFail");

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}