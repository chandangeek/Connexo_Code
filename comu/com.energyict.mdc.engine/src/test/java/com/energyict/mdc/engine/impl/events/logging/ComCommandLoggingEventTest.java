package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPort;

import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
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
 * Tests the {@link com.energyict.mdc.engine.impl.events.logging.ComCommandLoggingEvent} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/11/12
 * Time: 16:21
 */
@RunWith(MockitoJUnitRunner.class)
public class ComCommandLoggingEventTest {

    private static final long COMPORT_ID = 1;
    private static final long DEVICE_ID = 2;
    private static final long CONNECTION_TASK_ID = 3;
    private static final long COMTASK_EXECUTION_ID = 4;

    @Mock
    public Clock clock;
    @Mock
    public DeviceDataService deviceDataService;
    @Mock
    public EngineModelService engineModelService;
    @Mock
    private ServiceProvider serviceProvider;

    @Before
    public void setupServiceProvider () {
        when(this.clock.now()).thenReturn(new DateTime(1969, 5, 2, 1, 40, 0).toDate()); // Set some default
        when(this.serviceProvider.clock()).thenReturn(this.clock);
        when(this.serviceProvider.engineModelService()).thenReturn(this.engineModelService);
        when(this.serviceProvider.deviceDataService()).thenReturn(this.deviceDataService);
        ServiceProvider.instance.set(this.serviceProvider);
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
        Date now = new DateTime(2012, Calendar.NOVEMBER, 22, 16, 23, 12, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(now);

        ComCommandLoggingEvent event = new ComCommandLoggingEvent();

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testOccurrenceTimestamp () {
        Date now = new DateTime(2012, Calendar.NOVEMBER, 22, 16, 23, 12, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(now);

        ComCommandLoggingEvent event = new ComCommandLoggingEvent(null, null, null, LogLevel.INFO, "testOccurrenceTimestamp");

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }


    @Test
    public void testIsLoggingRelated () {
        // Business method
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(null, null, null, LogLevel.DEBUG, "testLogLevel");

        // Asserts
        assertThat(event.isLoggingRelated()).isTrue();
    }


    @Test
    public void testLogLevel () {
        LogLevel expectedLogLevel = LogLevel.DEBUG;
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(null, null, null, LogLevel.DEBUG, "testLogLevel");

        // Business method
        LogLevel logLevel = event.getLogLevel();

        // Asserts
        assertThat(logLevel).isEqualTo(expectedLogLevel);
    }

    @Test
    public void testLogMessage () {
        String expectedLogMessage = "testLogMessage";
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(null, null, null, LogLevel.DEBUG, expectedLogMessage);

        // Business method
        String logMessage = event.getLogMessage();

        // Asserts
        assertThat(logMessage).isEqualTo(expectedLogMessage);
    }

    @Test
    public void testIsDeviceRelated () {
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        Device device = mock(Device.class);
        when(connectionTask.getDevice()).thenReturn(device);

        ComCommandLoggingEvent event = new ComCommandLoggingEvent(null, connectionTask, null, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isTrue();
        assertThat(event.getDevice()).isEqualTo(device);
    }

    @Test
    public void testIsConnectionTaskRelated(){
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        Device device = mock(Device.class);
        when(connectionTask.getDevice()).thenReturn(device);

        ComCommandLoggingEvent event = new ComCommandLoggingEvent(null, connectionTask, null, LogLevel.INFO, "testIsConnectionTaskRelated");

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isTrue();
        assertThat(event.getConnectionTask()).isEqualTo(connectionTask);
    }

    @Test
    public void isComTaskExecutionRelated(){
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(null, null, comTaskExecution, LogLevel.INFO, "testIsComTaskExecutionRelated");

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isTrue();
        assertThat(event.getComTaskExecution()).isEqualTo(comTaskExecution);
    }

    @Test
    public void testIsComPortRelated(){
        ComPort comPort = mock(ComPort.class);
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(comPort, null, null, LogLevel.INFO, "testIsComPortRelated");

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
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(comPort, null, null, LogLevel.INFO, "testIsComPortPoolRelated");

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
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(comPort, null, null, LogLevel.INFO, "testIsNotComPortPoolRelated");

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
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASK_EXECUTION_ID);
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(comPort,  connectionTask, comTaskExecution, LogLevel.INFO, "testSerializationDoesNotFail");

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
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASK_EXECUTION_ID);
        LogLevel expectedLogLevel = LogLevel.INFO;
        String expectedLogMessage = "testRestoreAfterSerialization";
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(comPort, connectionTask, comTaskExecution, expectedLogLevel, expectedLogMessage);
        when(this.engineModelService.findComPort(COMPORT_ID)).thenReturn(comPort);
        when(this.deviceDataService.findConnectionTask(CONNECTION_TASK_ID)).thenReturn(Optional.of(connectionTask));
        when(this.deviceDataService.findComTaskExecution(COMTASK_EXECUTION_ID)).thenReturn(comTaskExecution);

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
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASK_EXECUTION_ID);
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(comPort, connectionTask, comTaskExecution, LogLevel.INFO, "testSerializationDoesNotFail");

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }
}