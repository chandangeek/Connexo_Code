package com.energyict.mdc.engine.impl.events.comtask;

import com.energyict.mdc.engine.events.Category;
import com.energyict.comserver.time.Clocks;
import com.energyict.comserver.time.FrozenClock;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.engine.impl.events.comtask.ComTaskExecutionFailureEvent;
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
 * Tests the {@link com.energyict.mdc.engine.impl.events.comtask.ComTaskExecutionFailureEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (17:05)
 */
public class ComTaskExecutionFailureEventTest {

    private static final int DEVICE_ID = 1;
    private static final long COMPORT_ID = DEVICE_ID + 1;
    private static final int CONNECTION_TASK_ID = (int) (COMPORT_ID + 1);
    private static final int COM_TASK_EXECUTION_ID = CONNECTION_TASK_ID + 1;
    private static final String ERROR_MESSAGE = "For testing purposes only";

    @After
    public void resetTimeFactory () throws SQLException {
        Clocks.resetAll();
    }

    @Test
    public void testCategory () {
        ComTaskExecutionFailureEvent event = new ComTaskExecutionFailureEvent();

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.COMTASK);
    }

    @Test
    public void testOccurrenceTimestamp () {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.NOVEMBER, 6, 15, 50, 44, 0);  // Random pick
        Clocks.setAppServerClock(frozenClock);
        Date now = Clocks.getAppServerClock().now();

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionFailureEvent event = this.newEvent(comTaskExecution, comPort, connectionTask);

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsNotStart () {
        ComTaskExecutionFailureEvent event = new ComTaskExecutionFailureEvent();

        // Business method & asserts
        assertThat(event.isStart()).isFalse();
        assertThat(event.getExecutionStartedTimestamp()).isNull();
    }

    @Test
    public void testIsFailure () {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionFailureEvent event = this.newEvent(comTaskExecution, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isFailure()).isTrue();
        assertThat(event.getFailureMessage()).startsWith(ERROR_MESSAGE);
    }

    @Test
    public void testIsNotCompletion () {
        ComTaskExecutionFailureEvent event = new ComTaskExecutionFailureEvent();

        // Business method & asserts
        assertThat(event.isCompletion()).isFalse();
    }

    @Test
    public void testIsNotLoggingRelatedByDefault () {
        ComTaskExecutionFailureEvent event = new ComTaskExecutionFailureEvent();

        // Business method & asserts
        assertThat(event.isLoggingRelated()).isFalse();
    }

    @Test
    public void testIsNotComTaskRelatedByDefault () {
        ComTaskExecutionFailureEvent event = new ComTaskExecutionFailureEvent();

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortRelatedByDefault () {
        ComTaskExecutionFailureEvent event = new ComTaskExecutionFailureEvent();

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortPoolRelatedByDefault () {
        ComTaskExecutionFailureEvent event = new ComTaskExecutionFailureEvent();

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelatedByDefault () {
        ComTaskExecutionFailureEvent event = new ComTaskExecutionFailureEvent();

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();
    }

    @Test
    public void testIsNotDeviceRelatedByDefault () {
        ComTaskExecutionFailureEvent event = new ComTaskExecutionFailureEvent();

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsComTaskRelated () {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionFailureEvent event = this.newEvent(comTaskExecution, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isTrue();
        assertThat(event.getComTaskExecution()).isEqualTo(comTaskExecution);
    }

    @Test
    public void testIsComPortRelated () {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionFailureEvent event = this.newEvent(comTaskExecution, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsNotComPortPoolRelatedForOutboundComPorts () {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        OutboundComPort comPort = mock(OutboundComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionFailureEvent event = this.newEvent(comTaskExecution, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsComPortPoolRelatedForInboundComPorts () {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.isInbound()).thenReturn(true);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionFailureEvent event = this.newEvent(comTaskExecution, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsConnectionTaskRelated () {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionFailureEvent event = this.newEvent(comTaskExecution, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isTrue();
        assertThat(event.getConnectionTask()).isEqualTo(connectionTask);
    }

    @Test
    public void testIsDeviceRelated () {
        BaseDevice device = mock(BaseDevice.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecutionFailureEvent event = this.newEvent(comTaskExecution, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isTrue();
        assertThat(event.getDevice()).isEqualTo(device);
    }

    @Test
    public void testSerializationDoesNotFailForDefaultObject () throws IOException {
        ComTaskExecutionFailureEvent event = new ComTaskExecutionFailureEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerializationForDefaultObject () throws IOException, ClassNotFoundException {
        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
        ComPortFactory comPortFactory = mock(ComPortFactory.class);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        ServerManager manager = mock(ServerManager.class);
        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        ManagerFactory.setCurrent(manager);
        ComTaskExecutionFailureEvent event = new ComTaskExecutionFailureEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        ComTaskExecutionFailureEvent restored = (ComTaskExecutionFailureEvent) ois.readObject();

        // Asserts
        assertThat(restored).isNotNull();
    }

    @Test
    public void testSerializationDoesNotFail () throws IOException {
        BaseDevice device = mock(BaseDevice.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecutionFailureEvent event = this.newEvent(comTaskExecution, comPort, connectionTask);

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
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
        when(comTaskExecutionFactory.find(COM_TASK_EXECUTION_ID)).thenReturn(comTaskExecution);
        ComPortFactory comPortFactory = mock(ComPortFactory.class);
        when(comPortFactory.find((int) COMPORT_ID)).thenReturn(comPort);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        when(connectionTaskFactory.find(CONNECTION_TASK_ID)).thenReturn(connectionTask);
        ServerManager manager = mock(ServerManager.class);
        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        ManagerFactory.setCurrent(manager);
        ComTaskExecutionFailureEvent event = this.newEvent(comTaskExecution, comPort, connectionTask);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        ComTaskExecutionFailureEvent restored = (ComTaskExecutionFailureEvent) ois.readObject();

        // Asserts
        assertThat(restored.getComTaskExecution()).isEqualTo(comTaskExecution);
        assertThat(restored.getComPort()).isEqualTo(comPort);
        assertThat(restored.getConnectionTask()).isEqualTo(connectionTask);
        assertThat(restored.getFailureMessage()).startsWith(ERROR_MESSAGE);
    }

    @Test
    public void testToStringDoesNotFailForDefaultObject () throws IOException {
        ComTaskExecutionFailureEvent event = new ComTaskExecutionFailureEvent();

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).doesNotMatch(ComTaskExecutionFailureEvent.class.getName() + "@\\d*");
    }

    @Test
    public void testToStringDoesNotFail () throws IOException {
        BaseDevice device = mock(BaseDevice.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecutionFailureEvent event = this.newEvent(comTaskExecution, comPort, connectionTask);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    private ComTaskExecutionFailureEvent newEvent (ComTaskExecution comTaskExecution, ComPort comPort, ConnectionTask connectionTask) {
        return new ComTaskExecutionFailureEvent(comTaskExecution, comPort, connectionTask, new RuntimeException(ERROR_MESSAGE, new Exception()));
    }

}