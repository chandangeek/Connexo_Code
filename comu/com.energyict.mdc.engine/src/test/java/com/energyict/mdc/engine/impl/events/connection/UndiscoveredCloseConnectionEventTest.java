package com.energyict.mdc.engine.impl.events.connection;

import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;

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
 * Tests the {@link com.energyict.mdc.engine.impl.events.connection.UndiscoveredCloseConnectionEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (14:04)
 */
@RunWith(MockitoJUnitRunner.class)
public class UndiscoveredCloseConnectionEventTest {

    private static final long DEVICE_ID = 1;
    private static final long COMPORT_ID = DEVICE_ID + 1;

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
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent();

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.CONNECTION);
    }

    @Test
    public void testOccurrenceTimestamp () {
        Date now = new DateTime(2012, Calendar.NOVEMBER, 6, 13, 45, 17, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(now);

        InboundComPort comPort = mock(InboundComPort.class);
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent(comPort);

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsNotEstablishing () {
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent();

        // Business method & asserts
        assertThat(event.isEstablishing()).isFalse();
    }

    @Test
    public void testIsNotFailure () {
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent();

        // Business method & asserts
        assertThat(event.isFailure()).isFalse();
        assertThat(event.getFailureMessage()).isNull();
    }

    @Test
    public void testIsClosed () {
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent();

        // Business method & asserts
        assertThat(event.isClosed()).isTrue();
    }

    @Test
    public void testIsNotLoggingRelatedByDefault () {
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent();

        // Business method & asserts
        assertThat(event.isLoggingRelated()).isFalse();
    }

    @Test
    public void testIsNotComTaskRelatedByDefault () {
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent();

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortRelatedByDefault () {
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent();

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortPoolRelatedByDefault () {
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent();

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelatedByDefault () {
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent();

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();
    }

    @Test
    public void testIsNotDeviceRelatedByDefault () {
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent();

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsComPortRelated () {
        InboundComPort comPort = mock(InboundComPort.class);
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent(comPort);

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
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent(comPort);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComTaskExecutionRelated () {
        InboundComPort comPort = mock(InboundComPort.class);
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent(comPort);

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelated () {
        InboundComPort comPort = mock(InboundComPort.class);
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent(comPort);

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();
        assertThat(event.getConnectionTask()).isNull();
    }

    @Test
    public void testIsNotDeviceRelated () {
        InboundComPort comPort = mock(InboundComPort.class);
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent(comPort);

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
        assertThat(event.getDevice()).isNull();
    }

    @Test
    public void testSerializationDoesNotFailForDefaultObject () throws IOException {
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerializationForDefaultObject () throws IOException, ClassNotFoundException {
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        UndiscoveredCloseConnectionEvent restored = (UndiscoveredCloseConnectionEvent) ois.readObject();

        // Asserts
        assertThat(restored).isNotNull();
    }

    @Test
    public void testSerializationDoesNotFail () throws IOException {
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent(comPort);

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
        when(this.engineModelService.findComPort(COMPORT_ID)).thenReturn(comPort);
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent(comPort);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        UndiscoveredCloseConnectionEvent restored = (UndiscoveredCloseConnectionEvent) ois.readObject();

        // Asserts
        assertThat(restored.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testToStringDoesNotFailForDefaultObject () throws IOException {
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent();

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).doesNotMatch(UndiscoveredCloseConnectionEvent.class.getName() + "@\\d*");
    }

    @Test
    public void testToStringDoesNotFail () throws IOException {
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        UndiscoveredCloseConnectionEvent event = new UndiscoveredCloseConnectionEvent(comPort);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}