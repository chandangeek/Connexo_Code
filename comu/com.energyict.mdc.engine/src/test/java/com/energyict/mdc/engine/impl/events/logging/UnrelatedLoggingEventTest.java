package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.model.EngineModelService;

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
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.logging.UnrelatedLoggingEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (17:20)
 */
@RunWith(MockitoJUnitRunner.class)
public class UnrelatedLoggingEventTest {

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
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider);

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.LOGGING);
    }

    @Test
    public void testOccurrenceTimestampForDefaultConstructor () {
        Date now = new DateTime(2012, Calendar.NOVEMBER, 7, 17, 22, 01, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(now);
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider);

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testOccurrenceTimestamp () {
        Date now = new DateTime(2012, Calendar.NOVEMBER, 6, 17, 22, 01, 0).toDate();  // Random pick
        when(this.clock.now()).thenReturn(now);

        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(LogLevel.INFO, "testOccurrenceTimestamp", this.serviceProvider);

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsLoggingRelated () {
        // Business method
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(LogLevel.DEBUG, "testLogLevel", this.serviceProvider);

        // Asserts
        assertThat(event.isLoggingRelated()).isTrue();
    }

    @Test
    public void testLogLevel () {
        LogLevel expectedLogLevel = LogLevel.DEBUG;
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(expectedLogLevel, "testLogLevel", this.serviceProvider);

        // Business method
        LogLevel logLevel = event.getLogLevel();

        // Asserts
        assertThat(logLevel).isEqualTo(expectedLogLevel);
    }

    @Test
    public void testLogMessage () {
        String expectedLogMessage = "testLogMessage";
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(LogLevel.DEBUG, expectedLogMessage, this.serviceProvider);

        // Business method
        String logMessage = event.getLogMessage();

        // Asserts
        assertThat(logMessage).isEqualTo(expectedLogMessage);
    }

    @Test
    public void testIsNotDeviceRelated () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider);

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();

    }

    @Test
    public void testIsNotConnectionTaskRelated () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider);

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();

    }

    @Test
    public void testIsNotComPortRelated () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider);

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();

    }

    @Test
    public void testIsNotComPortPoolRelated () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();

    }

    @Test
    public void testIsNotComTaskExecutionRelated () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider);

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();

    }

    @Test
    public void testSerializationDoesNotFailForDefaultObject () throws IOException {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerializationForDefaultObject () throws IOException, ClassNotFoundException {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        UnrelatedLoggingEvent restored = (UnrelatedLoggingEvent) ois.readObject();

        // Asserts
        assertThat(restored).isNotNull();
    }

    @Test
    public void testSerializationDoesNotFail () throws IOException {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(LogLevel.INFO, "testSerializationDoesNotFail", this.serviceProvider);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerialization () throws IOException, ClassNotFoundException {
        LogLevel expectedLogLevel = LogLevel.INFO;
        String expectedLogMessage = "testRestoreAfterSerialization";
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(expectedLogLevel, expectedLogMessage, this.serviceProvider);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Business method
        UnrelatedLoggingEvent restored = (UnrelatedLoggingEvent) ois.readObject();

        // Asserts
        assertThat(restored.getLogLevel()).isEqualTo(expectedLogLevel);
        assertThat(restored.getLogMessage()).isEqualTo(expectedLogMessage);
    }

    @Test
    public void testToStringDoesNotFailForDefaultObject () throws IOException {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(this.serviceProvider);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).doesNotMatch(UnrelatedLoggingEvent.class.getName() + "@\\d*");
    }

    @Test
    public void testToStringDoesNotFail () throws IOException, ClassNotFoundException {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(LogLevel.INFO, "testSerializationDoesNotFail", this.serviceProvider);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}