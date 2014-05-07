package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.logging.ComCommandLoggingEvent;
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
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.communication.tasks.ServerComTaskExecutionFactory;
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
 * Tests the {@link com.energyict.mdc.engine.impl.events.logging.ComCommandLoggingEvent} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/11/12
 * Time: 16:21
 */
public class ComCommandLoggingEventTest {

    private static final long COMPORT_ID = 1;
    private static final int DEVICE_ID = 2;
    private static final int CONNECTION_TASK_ID = 3;
    private static final int COMTASK_EXECUTION_ID = 4;

    @After
    public void resetTimeFactory () throws SQLException {
        Clocks.resetAll();
    }

    @Test
    public void testCategory () {
        ComCommandLoggingEvent event = new ComCommandLoggingEvent();

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.LOGGING);
    }


    @Test
    public void testOccurrenceTimestampForDefaultConstructor () {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.NOVEMBER, 22, 16, 23, 12, 0);  // Random pick
        Clocks.setAppServerClock(frozenClock);
        Date now = Clocks.getAppServerClock().now();

        ComCommandLoggingEvent event = new ComCommandLoggingEvent();

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testOccurrenceTimestamp () {
        FrozenClock eventClock = FrozenClock.frozenOn(2012, Calendar.NOVEMBER, 22, 16, 23, 12, 0);  // Random pick
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.NOVEMBER, 23, 16, 23, 12, 0);  // 1 day later
        Clocks.setAppServerClock(frozenClock);
        Date now = eventClock.now();

        ComCommandLoggingEvent event = new ComCommandLoggingEvent(now, null, null, null, LogLevel.INFO, "testOccurrenceTimestamp");

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }


    @Test
    public void testIsLoggingRelated () {
        // Business method
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(Clocks.getAppServerClock().now(), null, null, null, LogLevel.DEBUG, "testLogLevel");

        // Asserts
        assertThat(event.isLoggingRelated()).isTrue();
    }


    @Test
    public void testLogLevel () {
        LogLevel expectedLogLevel = LogLevel.DEBUG;
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(Clocks.getAppServerClock().now(), null, null, null, LogLevel.DEBUG, "testLogLevel");

        // Business method
        LogLevel logLevel = event.getLogLevel();

        // Asserts
        assertThat(logLevel).isEqualTo(expectedLogLevel);
    }

    @Test
    public void testLogMessage () {
        String expectedLogMessage = "testLogMessage";
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(Clocks.getAppServerClock().now(), null, null, null, LogLevel.DEBUG, expectedLogMessage);

        // Business method
        String logMessage = event.getLogMessage();

        // Asserts
        assertThat(logMessage).isEqualTo(expectedLogMessage);
    }

    @Test
    public void testIsDeviceRelated () {
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        BaseDevice device = mock(BaseDevice.class);
        when(connectionTask.getDevice()).thenReturn(device);

        ComCommandLoggingEvent event = new ComCommandLoggingEvent(Clocks.getAppServerClock().now(), null, connectionTask, null, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isTrue();
        assertThat(event.getDevice()).isEqualTo(device);
    }

    @Test
    public void testIsConnectionTaskRelated(){
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        BaseDevice device = mock(BaseDevice.class);
        when(connectionTask.getDevice()).thenReturn(device);

        ComCommandLoggingEvent event = new ComCommandLoggingEvent(Clocks.getAppServerClock().now(), null, connectionTask, null, LogLevel.INFO, "testIsConnectionTaskRelated");

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isTrue();
        assertThat(event.getConnectionTask()).isEqualTo(connectionTask);
    }

    @Test
    public void isComTaskExecutionRelated(){
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(Clocks.getAppServerClock().now(), null, null, comTaskExecution, LogLevel.INFO, "testIsComTaskExecutionRelated");

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isTrue();
        assertThat(event.getComTaskExecution()).isEqualTo(comTaskExecution);
    }

    @Test
    public void testIsComPortRelated(){
        ComPort comPort = mock(ComPort.class);
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(Clocks.getAppServerClock().now(), comPort, null, null, LogLevel.INFO, "testIsComPortRelated");

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsComPortPoolRelated(){
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.isInbound()).thenReturn(true);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(Clocks.getAppServerClock().now(), comPort, null, null, LogLevel.INFO, "testIsComPortPoolRelated");

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComPortPoolRelated(){
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.isInbound()).thenReturn(false);
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(Clocks.getAppServerClock().now(), comPort, null, null, LogLevel.INFO, "testIsNotComPortPoolRelated");

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortPoolRelatedByDefault () {
        ComCommandLoggingEvent event = new ComCommandLoggingEvent();

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortRelatedByDefault () {
        ComCommandLoggingEvent event = new ComCommandLoggingEvent();

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelatedByDefault () {
        ComCommandLoggingEvent event = new ComCommandLoggingEvent();

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();
    }

    @Test
    public void testIsNotDeviceRelatedByDefault(){
        ComCommandLoggingEvent event = new ComCommandLoggingEvent();

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsNotComTaskExecutionRelatedByDefault(){
        ComCommandLoggingEvent event = new ComCommandLoggingEvent();

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testSerializationDoesNotFailForDefaultObject () throws IOException {
        ComCommandLoggingEvent event = new ComCommandLoggingEvent();

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
        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
        ServerManager manager = mock(ServerManager.class);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
        ManagerFactory.setCurrent(manager);

        ComCommandLoggingEvent event = new ComCommandLoggingEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        ComCommandLoggingEvent restored = (ComCommandLoggingEvent) ois.readObject();

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
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASK_EXECUTION_ID);
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(Clocks.getAppServerClock().now(), comPort,  connectionTask, comTaskExecution, LogLevel.INFO, "testSerializationDoesNotFail");

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
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASK_EXECUTION_ID);
        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
        when(comTaskExecutionFactory.find(COMTASK_EXECUTION_ID)).thenReturn(comTaskExecution);
        ServerManager manager = mock(ServerManager.class);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
        ManagerFactory.setCurrent(manager);
        LogLevel expectedLogLevel = LogLevel.INFO;
        String expectedLogMessage = "testRestoreAfterSerialization";
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(Clocks.getAppServerClock().now(), comPort, connectionTask, comTaskExecution, expectedLogLevel, expectedLogMessage);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        ComCommandLoggingEvent restored = (ComCommandLoggingEvent) ois.readObject();

        // Asserts
        assertThat(restored.getLogLevel()).isEqualTo(expectedLogLevel);
        assertThat(restored.getLogMessage()).isEqualTo(expectedLogMessage);
        assertThat(restored.getComPort()).isEqualTo(comPort);
        assertThat(restored.getComTaskExecution()).isEqualTo(comTaskExecution);
        assertThat(restored.getConnectionTask()).isEqualTo(connectionTask);
        assertThat(restored.getDevice()).isEqualTo(device);
    }


    @Test
    public void testToStringDoesNotFailForDefaultObject () throws IOException {
        ComCommandLoggingEvent event = new ComCommandLoggingEvent();

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).doesNotMatch(ComCommandLoggingEvent.class.getName() + "@\\d*");
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
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASK_EXECUTION_ID);
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(Clocks.getAppServerClock().now(), comPort, connectionTask, comTaskExecution, LogLevel.INFO, "testSerializationDoesNotFail");

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }
}