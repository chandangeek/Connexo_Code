package com.energyict.mdc.engine.impl.events.connection;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.ProgrammableClock;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
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
 * Tests the {@link com.energyict.mdc.engine.impl.events.connection.UndiscoveredEstablishConnectionEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (14:04)
 */
public class UndiscoveredEstablishConnectionEventTest {

    private static final long DEVICE_ID = 1;
    private static final long COMPORT_ID = DEVICE_ID + 1;
    private FakeServiceProvider serviceProvider = new FakeServiceProvider();
    private AbstractComServerEventImpl.ServiceProvider eventServiceProvider = new AbstractComServerEventImpl.ServiceProviderAdapter(serviceProvider);

    @Test
    public void testCategory () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(eventServiceProvider);

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.CONNECTION);
    }

    @Test
    public void testOccurrenceTimestamp () {
        Clock frozenClock = new ProgrammableClock().frozenAt(new DateTime(2012, 11, 6, 13, 45, 17, 0).toDate());  // Random pick
        serviceProvider.setClock(frozenClock);
        Date now = frozenClock.now();

        InboundComPort comPort = mock(InboundComPort.class);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(comPort, eventServiceProvider);

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsEstablishd () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(eventServiceProvider);

        // Business method & asserts
        assertThat(event.isEstablishing()).isTrue();
    }

    @Test
    public void testIsNotClosed () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(eventServiceProvider);

        // Business method & asserts
        assertThat(event.isClosed()).isFalse();
    }

    @Test
    public void testIsNotFailure () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(eventServiceProvider);

        // Business method & asserts
        assertThat(event.isFailure()).isFalse();
        assertThat(event.getFailureMessage()).isNull();
    }

    @Test
    public void testIsNotLoggingRelatedByDefault () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(eventServiceProvider);

        // Business method & asserts
        assertThat(event.isLoggingRelated()).isFalse();
    }

    @Test
    public void testIsNotComTaskRelatedByDefault () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(eventServiceProvider);

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortRelatedByDefault () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(eventServiceProvider);

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortPoolRelatedByDefault () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(eventServiceProvider);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelatedByDefault () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(eventServiceProvider);

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();
    }

    @Test
    public void testIsNotDeviceRelatedByDefault () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(eventServiceProvider);

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsComPortRelated () {
        InboundComPort comPort = mock(InboundComPort.class);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(comPort, eventServiceProvider);

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsComPortPoolRelated () {
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.isInbound()).thenReturn(true);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(comPort, eventServiceProvider);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComTaskExecutionRelated () {
        InboundComPort comPort = mock(InboundComPort.class);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(comPort, eventServiceProvider);

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelated () {
        InboundComPort comPort = mock(InboundComPort.class);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(comPort, eventServiceProvider);

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();
        assertThat(event.getConnectionTask()).isNull();
    }

    @Test
    public void testIsNotDeviceRelated () {
        InboundComPort comPort = mock(InboundComPort.class);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(comPort, eventServiceProvider);

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
        assertThat(event.getDevice()).isNull();
    }

    @Test
    public void testSerializationDoesNotFailForDefaultObject () throws IOException {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(eventServiceProvider);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerializationForDefaultObject () throws IOException, ClassNotFoundException {
//        ServerManager manager = mock(ServerManager.class);
//        when(manager.getComPortFactory()).thenReturn(comPortFactory);
//        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
//        ManagerFactory.setCurrent(manager);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(eventServiceProvider);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        UndiscoveredEstablishConnectionEvent restored = (UndiscoveredEstablishConnectionEvent) ois.readObject();

        // Asserts
        assertThat(restored).isNotNull();
    }

    @Test
    public void testSerializationDoesNotFail () throws IOException {
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(comPort, eventServiceProvider);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerialization () throws IOException, ClassNotFoundException {
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
//        ComPortFactory comPortFactory = mock(ComPortFactory.class);
//        when(comPortFactory.find(COMPORT_ID)).thenReturn(comPort);
//        ServerManager manager = mock(ServerManager.class);
//        when(manager.getComPortFactory()).thenReturn(comPortFactory);
//        ManagerFactory.setCurrent(manager);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(comPort, eventServiceProvider);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        UndiscoveredEstablishConnectionEvent restored = (UndiscoveredEstablishConnectionEvent) ois.readObject();

        // Asserts
        assertThat(restored.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testToStringDoesNotFailForDefaultObject () throws IOException {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(eventServiceProvider);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).doesNotMatch(UndiscoveredEstablishConnectionEvent.class.getName() + "@\\d*");
    }

    @Test
    public void testToStringDoesNotFail () throws IOException {
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(comPort,eventServiceProvider);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}