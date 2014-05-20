package com.energyict.mdc.engine.impl.events.connection;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.ProgrammableClock;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPort;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.connection.EstablishConnectionEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (13:26)
 */
public class EstablishConnectionEventTest {

    private static final long DEVICE_ID = 1;
    private static final long COMPORT_ID = DEVICE_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;
    private FakeServiceProvider serviceProvider = new FakeServiceProvider();
    private final AbstractComServerEventImpl.ServiceProviderAdapter serviceProviderAdapter = new AbstractComServerEventImpl.ServiceProviderAdapter(serviceProvider);

    @Test
    public void testCategory () {
        EstablishConnectionEvent event = new EstablishConnectionEvent(serviceProviderAdapter);

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.CONNECTION);
    }

    @Test
    public void testOccurrenceTimestamp () {
        Clock frozenClock = new ProgrammableClock().frozenAt(new DateTime(2012, 11, 6, 13, 45, 17, 0).toDate());  // Random pick
        Date now = frozenClock.now();

        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        EstablishConnectionEvent event = new EstablishConnectionEvent(comPort, connectionTask, serviceProviderAdapter);

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsEstablishing () {
        EstablishConnectionEvent event = new EstablishConnectionEvent(serviceProviderAdapter);

        // Business method & asserts
        assertThat(event.isEstablishing()).isTrue();
    }

    @Test
    public void testIsNotFailure () {
        EstablishConnectionEvent event = new EstablishConnectionEvent(serviceProviderAdapter);

        // Business method & asserts
        assertThat(event.isFailure()).isFalse();
        assertThat(event.getFailureMessage()).isNull();
    }

    @Test
    public void testIsNotClosed () {
        EstablishConnectionEvent event = new EstablishConnectionEvent(serviceProviderAdapter);

        // Business method & asserts
        assertThat(event.isClosed()).isFalse();
    }

    @Test
    public void testIsNotLoggingRelatedByDefault () {
        EstablishConnectionEvent event = new EstablishConnectionEvent(serviceProviderAdapter);

        // Business method & asserts
        assertThat(event.isLoggingRelated()).isFalse();
    }

    @Test
    public void testIsNotComTaskRelatedByDefault () {
        EstablishConnectionEvent event = new EstablishConnectionEvent(serviceProviderAdapter);

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortRelatedByDefault () {
        EstablishConnectionEvent event = new EstablishConnectionEvent(serviceProviderAdapter);

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortPoolRelatedByDefault () {
        EstablishConnectionEvent event = new EstablishConnectionEvent(serviceProviderAdapter);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelatedByDefault () {
        EstablishConnectionEvent event = new EstablishConnectionEvent(serviceProviderAdapter);

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();
    }

    @Test
    public void testIsNotDeviceRelatedByDefault () {
        EstablishConnectionEvent event = new EstablishConnectionEvent(serviceProviderAdapter);

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsComPortRelated () {
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        EstablishConnectionEvent event = new EstablishConnectionEvent(comPort, connectionTask, serviceProviderAdapter);

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsNotComPortPoolRelatedForOutboundComPorts () {
        OutboundComPort comPort = mock(OutboundComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        EstablishConnectionEvent event = new EstablishConnectionEvent(comPort, connectionTask, serviceProviderAdapter);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsComPortPoolRelatedForInboundComPorts () {
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.isInbound()).thenReturn(true);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        EstablishConnectionEvent event = new EstablishConnectionEvent(comPort, connectionTask, serviceProviderAdapter);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComTaskRelated () {
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        EstablishConnectionEvent event = new EstablishConnectionEvent(comPort, connectionTask, serviceProviderAdapter);

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testIsConnectionTaskRelated () {
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        EstablishConnectionEvent event = new EstablishConnectionEvent(comPort, connectionTask, serviceProviderAdapter);

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isTrue();
        assertThat(event.getConnectionTask()).isEqualTo(connectionTask);
    }

    @Test
    public void testIsDeviceRelated () {
        Device device = mock(Device.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getDevice()).thenReturn(device);
        EstablishConnectionEvent event = new EstablishConnectionEvent(comPort, connectionTask, serviceProviderAdapter);

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isTrue();
        assertThat(event.getDevice()).isEqualTo(device);
    }

    @Test
    public void testSerializationDoesNotFailForDefaultObject () throws IOException {
        EstablishConnectionEvent event = new EstablishConnectionEvent(serviceProviderAdapter);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerializationForDefaultObject () throws IOException, ClassNotFoundException {
//        ComPortFactory comPortFactory = mock(ComPortFactory.class);
//        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
//        ServerManager manager = mock(ServerManager.class);
//        when(manager.getComPortFactory()).thenReturn(comPortFactory);
//        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
//        ManagerFactory.setCurrent(manager);
        EstablishConnectionEvent event = new EstablishConnectionEvent(serviceProviderAdapter);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        EstablishConnectionEvent restored = (EstablishConnectionEvent) ois.readObject();

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
        EstablishConnectionEvent event = new EstablishConnectionEvent(comPort, connectionTask, serviceProviderAdapter);

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
//        ComPortFactory comPortFactory = mock(ComPortFactory.class);
//        when(comPortFactory.find(COMPORT_ID)).thenReturn(comPort);
//        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
//        when(connectionTaskFactory.find(CONNECTION_TASK_ID)).thenReturn(connectionTask);
//        ServerManager manager = mock(ServerManager.class);
//        when(manager.getComPortFactory()).thenReturn(comPortFactory);
//        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
//        ManagerFactory.setCurrent(manager);
        EstablishConnectionEvent event = new EstablishConnectionEvent(comPort, connectionTask, serviceProviderAdapter);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        EstablishConnectionEvent restored = (EstablishConnectionEvent) ois.readObject();

        // Asserts
        assertThat(restored.getComPort()).isEqualTo(comPort);
        assertThat(restored.getConnectionTask()).isEqualTo(connectionTask);
    }

    @Test
    public void testToStringDoesNotFailForDefaultObject () throws IOException {
        EstablishConnectionEvent event = new EstablishConnectionEvent(serviceProviderAdapter);

        // Business method
        String eventString = event.toString();

        //Asserts
        assertThat(eventString).doesNotMatch(EstablishConnectionEvent.class.getName() + "@\\d*");
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
        EstablishConnectionEvent event = new EstablishConnectionEvent(comPort, connectionTask, serviceProviderAdapter);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}