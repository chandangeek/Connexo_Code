package com.energyict.mdc.engine.impl.events.comtask;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPort;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.comtask.ComTaskExecutionCompletionEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (15:50)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskExecutionCompletionEventTest {

    private static final long DEVICE_ID = 1;
    private static final long COMPORT_ID = DEVICE_ID + 1;
    private static final long CONNECTION_TASK_ID = (int) (COMPORT_ID + 1);
    private static final long COM_TASK_EXECUTION_ID = CONNECTION_TASK_ID + 1;

    @Mock
    public Clock clock;
    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void setupServiceProvider () {
        when(this.clock.now()).thenReturn(new DateTime(1969, 5, 2, 1, 40, 0).toDate()); // Set some default
        when(this.serviceProvider.clock()).thenReturn(this.clock);
    }

    @Test
    public void testCategory () {
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, null, null, null);

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
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, comTaskExecution, comPort, connectionTask);

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsNotStart () {
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, null, null, null);

        // Business method & asserts
        assertThat(event.isStart()).isFalse();
        assertThat(event.getExecutionStartedTimestamp()).isNull();
    }

    @Test
    public void testIsCompletion () {
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, null, null, null);

        // Business method & asserts
        assertThat(event.isCompletion()).isTrue();
    }

    @Test
    public void testIsNotFailure () {
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, null, null, null);

        // Business method & asserts
        assertThat(event.isFailure()).isFalse();
        assertThat(event.getFailureMessage()).isNull();
    }

    @Test
    public void testIsNotLoggingRelatedByDefault () {
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, null, null, null);

        // Business method & asserts
        assertThat(event.isLoggingRelated()).isFalse();
    }

    @Test
    public void testIsNotComTaskRelatedByDefault () {
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, null, null, null);

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortRelatedByDefault () {
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, null, null, null);

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortPoolRelatedByDefault () {
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, null, null, null);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelatedByDefault () {
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, null, null, null);

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();
    }

    @Test
    public void testIsNotDeviceRelatedByDefault () {
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, null, null, null);

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsComTaskRelated () {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, comTaskExecution, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isTrue();
        assertThat(event.getComTaskExecution()).isEqualTo(comTaskExecution);
    }

    @Test
    public void testIsComPortRelated () {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, comTaskExecution, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsNotComPortPoolRelatedForOutboundComPorts () {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        OutboundComPort comPort = mock(OutboundComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, comTaskExecution, comPort, connectionTask);

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
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, comTaskExecution, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsConnectionTaskRelated () {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, comTaskExecution, comPort, connectionTask);

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
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, comTaskExecution, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isTrue();
        assertThat(event.getDevice()).isEqualTo(device);
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
        ComTaskExecutionCompletionEvent event = new ComTaskExecutionCompletionEvent(this.serviceProvider, comTaskExecution, comPort, connectionTask);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}