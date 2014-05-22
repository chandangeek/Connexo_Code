package com.energyict.mdc.engine.impl.events.io;

import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;

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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.io.ReadEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-12 (12:27)
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadEventTest {

    private static final long COMPORT_ID = 1;

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
        when(this.clock.now()).thenReturn(new DateTime(2014, 5, 2, 1, 40, 0).toDate()); // Set some default
        when(this.serviceProvider.clock()).thenReturn(this.clock);
        when(this.serviceProvider.engineModelService()).thenReturn(this.engineModelService);
        when(this.serviceProvider.deviceDataService()).thenReturn(this.deviceDataService);
        ServiceProvider.instance.set(this.serviceProvider);
    }

    @Test
    public void testIsRead () {
        ReadEvent event = new ReadEvent();
        assertThat(event.isRead()).isTrue();
    }

    @Test
    public void testIsNotWrite () {
        ReadEvent event = new ReadEvent();
        assertThat(event.isWrite()).isFalse();
    }

    @Test
    public void testConstructor () {
        Date occurrenceTimestamp = new DateTime(2012, Calendar.NOVEMBER, 7, 17, 22, 01, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(occurrenceTimestamp);
        byte[] bytes = "testConstructor".getBytes();
        ComPort comPort = mock(ComPort.class);

        // Business method
        ReadEvent event = new ReadEvent(comPort, bytes);

        // Asserts
        assertThat(event.getOccurrenceTimestamp()).isEqualTo(occurrenceTimestamp);
        assertThat(event.getBytes()).isEqualTo(bytes);
    }

    @Test
    public void testGetCategory () {
        ReadEvent event = new ReadEvent();
        assertThat(event.getCategory()).isEqualTo(Category.CONNECTION);
    }

    @Test
    public void testIsNotEstablishing () {
        ReadEvent event = new ReadEvent();
        assertThat(event.isEstablishing()).isFalse();
    }

    @Test
    public void testIsNotFailure () {
        ReadEvent event = new ReadEvent();
        assertThat(event.isFailure()).isFalse();
    }

    @Test
    public void testIsNotClosed () {
        ReadEvent event = new ReadEvent();
        assertThat(event.isClosed()).isFalse();
    }

    @Test
    public void testIsNotLoggingRelatedByDefault () {
        ReadEvent event = new ReadEvent();

        // Business method & asserts
        assertThat(event.isLoggingRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortRelatedByDefault () {
        ReadEvent event = new ReadEvent();
        assertThat(event.isComPortRelated()).isFalse();
        assertThat(event.getComPort()).isNull();
    }

    @Test
    public void testIsComPortRelated () {
        ComPort comPort = mock(ComPort.class);
        when(comPort.isInbound()).thenReturn(false);
        ReadEvent event = new ReadEvent(comPort, "testIsComPortRelated".getBytes());
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsNotConnectionTaskRelated () {
        ReadEvent event = new ReadEvent();
        assertThat(event.isConnectionTaskRelated()).isFalse();
        assertThat(event.getConnectionTask()).isNull();
    }

    @Test
    public void testIsNotDeviceRelated () {
        ReadEvent event = new ReadEvent();
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortPoolRelatedByDefault () {
        ReadEvent event = new ReadEvent();
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsComPortPoolRelatedForOutboundComPort () {
        ComPort comPort = mock(ComPort.class);
        when(comPort.isInbound()).thenReturn(false);
        ReadEvent event = new ReadEvent(comPort, "testIsComPortPoolRelatedForOutboundComPort".getBytes());
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsComPortPoolRelatedForInboundComPort () {
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.isInbound()).thenReturn(true);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPort.getComPortPool()).thenReturn(comPortPool);

        ReadEvent event = new ReadEvent(comPort, "testIsComPortPoolRelatedForInboundComPort".getBytes());
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComTaskExecutionRelated () {
        ReadEvent event = new ReadEvent();
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testSerializationDoesNotFailForDefaultObject () throws IOException {
        ReadEvent event = new ReadEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerializationForDefaultObject () throws IOException, ClassNotFoundException {
        ReadEvent event = new ReadEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        ReadEvent restored = (ReadEvent) ois.readObject();

        // Asserts
        assertThat(restored).isNotNull();
    }

    @Test
    public void testSerializationDoesNotFail () throws IOException {
        Date occurrenceTimestamp = new DateTime(2012, Calendar.NOVEMBER, 7, 17, 22, 01, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(occurrenceTimestamp);

        byte[] bytes = "testSerializationDoesNotFail".getBytes();
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ReadEvent event = new ReadEvent(comPort, bytes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerialization () throws IOException, ClassNotFoundException {
        Date occurrenceTimestamp = new DateTime(2012, Calendar.NOVEMBER, 7, 17, 22, 01, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(occurrenceTimestamp);
        byte[] bytes = "testRestoreAfterSerialization".getBytes();
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ReadEvent event = new ReadEvent(comPort, bytes);
        when(this.deviceDataService.findConnectionTask(anyInt())).thenReturn(Optional.<ConnectionTask>absent());
        when(this.engineModelService.findComPort(COMPORT_ID)).thenReturn(comPort);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        ReadEvent restored = (ReadEvent) ois.readObject();

        // Asserts
        assertThat(restored.getBytes()).isEqualTo(bytes);
    }

    @Test
    public void testToStringDoesNotFailForDefaultObject () throws IOException {
        ReadEvent event = new ReadEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).doesNotMatch(ReadEvent.class.getName() + "@\\d*");
    }
    @Test
    public void testToStringDoesNotFail () throws IOException {
        Date occurrenceTimestamp = new DateTime(2012, Calendar.NOVEMBER, 7, 17, 22, 01, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(occurrenceTimestamp);
        byte[] bytes = "testToStringDoesNotFail".getBytes();
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ReadEvent event = new ReadEvent(comPort, bytes);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}