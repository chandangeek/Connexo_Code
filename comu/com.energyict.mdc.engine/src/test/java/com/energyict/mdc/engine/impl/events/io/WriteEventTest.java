package com.energyict.mdc.engine.impl.events.io;

import com.energyict.mdc.engine.events.Category;
import com.energyict.comserver.time.Clocks;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.engine.impl.events.io.WriteEvent;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.ports.ComPortFactory;
import com.energyict.mdc.engine.model.InboundComPort;
import org.junit.*;

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
 * Tests the {@link com.energyict.mdc.engine.impl.events.io.WriteEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-13 (14:08)
 */
public class WriteEventTest {

    private static final int COMPORT_ID = 1;

    @Test
    public void testIsWrite () {
        WriteEvent event = new WriteEvent();
        assertThat(event.isWrite()).isTrue();
    }

    @Test
    public void testIsNotRead () {
        WriteEvent event = new WriteEvent();
        assertThat(event.isRead()).isFalse();
    }

    @Test
    public void testConstructor () {
        Date occurrenceTimestamp = Clocks.getAppServerClock().now();
        byte[] bytes = "testConstructor".getBytes();
        ComPort comPort = mock(ComPort.class);

        // Business method
        WriteEvent event = new WriteEvent(occurrenceTimestamp, comPort, bytes);

        // Asserts
        assertThat(event.getOccurrenceTimestamp()).isEqualTo(occurrenceTimestamp);
        assertThat(event.getBytes()).isEqualTo(bytes);
    }

    @Test
    public void testGetCategory () {
        WriteEvent event = new WriteEvent();
        assertThat(event.getCategory()).isEqualTo(Category.CONNECTION);
    }

    @Test
    public void testIsNotEstablishing () {
        WriteEvent event = new WriteEvent();
        assertThat(event.isEstablishing()).isFalse();
    }

    @Test
    public void testIsNotFailure () {
        WriteEvent event = new WriteEvent();
        assertThat(event.isFailure()).isFalse();
    }

    @Test
    public void testIsNotClosed () {
        WriteEvent event = new WriteEvent();
        assertThat(event.isClosed()).isFalse();
    }

    @Test
    public void testIsNotLoggingRelatedByDefault () {
        WriteEvent event = new WriteEvent();

        // Business method & asserts
        assertThat(event.isLoggingRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortRelatedByDefault () {
        WriteEvent event = new WriteEvent();
        assertThat(event.isComPortRelated()).isFalse();
        assertThat(event.getComPort()).isNull();
    }

    @Test
    public void testIsComPortRelated () {
        ComPort comPort = mock(ComPort.class);
        when(comPort.isInbound()).thenReturn(false);
        WriteEvent event = new WriteEvent(Clocks.getAppServerClock().now(), comPort, "testIsComPortRelated".getBytes());
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsNotConnectionTaskRelated () {
        WriteEvent event = new WriteEvent();
        assertThat(event.isConnectionTaskRelated()).isFalse();
        assertThat(event.getConnectionTask()).isNull();
    }

    @Test
    public void testIsNotDeviceRelated () {
        WriteEvent event = new WriteEvent();
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortPoolRelatedByDefault () {
        WriteEvent event = new WriteEvent();
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsComPortPoolRelatedForOutboundComPort () {
        ComPort comPort = mock(ComPort.class);
        when(comPort.isInbound()).thenReturn(false);
        WriteEvent event = new WriteEvent(Clocks.getAppServerClock().now(), comPort, "testIsComPortPoolRelatedForOutboundComPort".getBytes());
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsComPortPoolRelatedForInboundComPort () {
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.isInbound()).thenReturn(true);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPort.getComPortPool()).thenReturn(comPortPool);

        WriteEvent event = new WriteEvent(Clocks.getAppServerClock().now(), comPort, "testIsComPortPoolRelatedForInboundComPort".getBytes());
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComTaskExecutionRelated () {
        WriteEvent event = new WriteEvent();
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testSerializationDoesNotFailForDefaultObject () throws IOException {
        WriteEvent event = new WriteEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerializationForDefaultObject () throws IOException, ClassNotFoundException {
        WriteEvent event = new WriteEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        WriteEvent restored = (WriteEvent) ois.readObject();

        // Asserts
        assertThat(restored).isNotNull();
    }

    @Test
    public void testSerializationDoesNotFail () throws IOException {
        Date occurrenceTimestamp = Clocks.getAppServerClock().now();
        byte[] bytes = "testSerializationDoesNotFail".getBytes();
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ComPortFactory comPortFactory = mock(ComPortFactory.class);
        when(comPortFactory.find(COMPORT_ID)).thenReturn(comPort);
        ServerManager manager = mock(ServerManager.class);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        ManagerFactory.setCurrent(manager);
        WriteEvent event = new WriteEvent(occurrenceTimestamp, comPort, bytes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerialization () throws IOException, ClassNotFoundException {
        Date occurrenceTimestamp = Clocks.getAppServerClock().now();
        byte[] bytes = "testRestoreAfterSerialization".getBytes();
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ComPortFactory comPortFactory = mock(ComPortFactory.class);
        when(comPortFactory.find(COMPORT_ID)).thenReturn(comPort);
        ServerManager manager = mock(ServerManager.class);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        ManagerFactory.setCurrent(manager);
        WriteEvent event = new WriteEvent(occurrenceTimestamp, comPort, bytes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        WriteEvent restored = (WriteEvent) ois.readObject();

        // Asserts
        assertThat(restored.getBytes()).isEqualTo(bytes);
    }

    @Test
    public void testToStringDoesNotFailForDefaultObject () throws IOException {
        WriteEvent event = new WriteEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).doesNotMatch(WriteEvent.class.getName() + "@\\d*");
    }
    @Test
    public void testToStringDoesNotFail () throws IOException {
        Date occurrenceTimestamp = Clocks.getAppServerClock().now();
        byte[] bytes = "testSerializationDoesNotFail".getBytes();
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ComPortFactory comPortFactory = mock(ComPortFactory.class);
        when(comPortFactory.find(COMPORT_ID)).thenReturn(comPort);
        ServerManager manager = mock(ServerManager.class);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        ManagerFactory.setCurrent(manager);
        WriteEvent event = new WriteEvent(occurrenceTimestamp, comPort, bytes);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}