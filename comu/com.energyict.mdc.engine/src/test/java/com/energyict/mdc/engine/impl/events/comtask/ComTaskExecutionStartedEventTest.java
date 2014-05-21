package com.energyict.mdc.engine.impl.events.comtask;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
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
 * Tests the {@link com.energyict.mdc.engine.impl.events.comtask.ComTaskExecutionStartedEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (15:50)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskExecutionStartedEventTest {

    private static final long DEVICE_ID = 1;
    private static final long COMPORT_ID = DEVICE_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;
    private static final long COM_TASK_EXECUTION_ID = CONNECTION_TASK_ID + 1;

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
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent();

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.COMTASK);
    }

    @Test
    public void testOccurrenceTimestamp () {
        Date now = new DateTime(2012, Calendar.NOVEMBER, 6, 15, 50, 44, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(now);

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent(comTaskExecution, now, comPort, connectionTask);

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testExecutionStartedTimestamp () {
        Date now = new DateTime(2012, Calendar.NOVEMBER, 6, 15, 50, 43, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(now);

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent(comTaskExecution, now, comPort, connectionTask);

        // Business method
        Date timestamp = event.getExecutionStartedTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testExecutionStartedTimestampCopiedFromComTaskExecution () {
        Date now = new DateTime(2012, Calendar.NOVEMBER, 6, 15, 50, 43, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(now);

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getExecutionStartedTimestamp()).thenReturn(now);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent(comTaskExecution, comPort, connectionTask, this.serviceProvider);

        // Business method
        Date timestamp = event.getExecutionStartedTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsStart () {
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent();

        // Business method & asserts
        assertThat(event.isStart()).isTrue();
    }

    @Test
    public void testIsNotCompletion () {
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent();

        // Business method & asserts
        assertThat(event.isCompletion()).isFalse();
    }

    @Test
    public void testIsNotFailure () {
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent();

        // Business method & asserts
        assertThat(event.isFailure()).isFalse();
        assertThat(event.getFailureMessage()).isNull();
    }

    @Test
    public void testIsNotLoggingRelatedByDefault () {
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent();

        // Business method & asserts
        assertThat(event.isLoggingRelated()).isFalse();
    }

    @Test
    public void testIsNotComTaskRelatedByDefault () {
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent();

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortRelatedByDefault () {
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent();

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortPoolRelatedByDefault () {
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent();

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelatedByDefault () {
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent();

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();
    }

    @Test
    public void testIsNotDeviceRelatedByDefault () {
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent();

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsComTaskRelated () {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent(comTaskExecution, comPort, connectionTask, this.serviceProvider);

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isTrue();
        assertThat(event.getComTaskExecution()).isEqualTo(comTaskExecution);
    }

    @Test
    public void testIsComPortRelated () {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent(comTaskExecution, comPort, connectionTask, this.serviceProvider);

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsNotComPortPoolRelatedForOutboundComPorts () {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        OutboundComPort comPort = mock(OutboundComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent(comTaskExecution, comPort, connectionTask, this.serviceProvider);

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
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent(comTaskExecution, comPort, connectionTask, this.serviceProvider);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsConnectionTaskRelated () {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent(comTaskExecution, comPort, connectionTask, this.serviceProvider);

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isTrue();
        assertThat(event.getConnectionTask()).isEqualTo(connectionTask);
    }

    @Test
    public void testIsDeviceRelated () {
        Device device = mock(Device.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent(comTaskExecution, comPort, connectionTask, this.serviceProvider);

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isTrue();
        assertThat(event.getDevice()).isEqualTo(device);
    }

    @Test
    public void testSerializationDoesNotFailForDefaultObject () throws IOException {
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerializationForDefaultObject () throws IOException, ClassNotFoundException {
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        ComTaskExecutionStartedEvent restored = (ComTaskExecutionStartedEvent) ois.readObject();

        // Asserts
        assertThat(restored).isNotNull();
    }

    @Test
    public void testSerializationDoesNotFail () throws IOException {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent(comTaskExecution, comPort, connectionTask, this.serviceProvider);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerialization () throws IOException, ClassNotFoundException {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent(comTaskExecution, comPort, connectionTask, this.serviceProvider);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        ComTaskExecutionStartedEvent restored = (ComTaskExecutionStartedEvent) ois.readObject();

        // Asserts
        assertThat(restored.getComTaskExecution()).isEqualTo(comTaskExecution);
        assertThat(restored.getComPort()).isEqualTo(comPort);
        assertThat(restored.getConnectionTask()).isEqualTo(connectionTask);
    }

    @Test
    public void testToStringDoesNotFailForDefaultObject () throws IOException {
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent();

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).doesNotMatch(ComTaskExecutionStartedEvent.class.getName() + "@\\d*");
    }

    @Test
    public void testToStringDoesNotFail () throws IOException {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecutionStartedEvent event = new ComTaskExecutionStartedEvent(comTaskExecution, comPort, connectionTask, this.serviceProvider);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}