package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.logging.UnrelatedLoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.comserver.time.Clocks;
import com.energyict.comserver.time.FrozenClock;
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

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.logging.UnrelatedLoggingEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (17:20)
 */
public class UnrelatedLoggingEventTest {

    @After
    public void resetTimeFactory () throws SQLException {
        Clocks.resetAll();
    }

    @Test
    public void testCategory () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent();

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.LOGGING);
    }

    @Test
    public void testOccurrenceTimestampForDefaultConstructor () {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.NOVEMBER, 7, 17, 22, 01, 0);  // Random pick
        Clocks.setAppServerClock(frozenClock);
        Date now = Clocks.getAppServerClock().now();

        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent();

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testOccurrenceTimestamp () {
        FrozenClock eventClock = FrozenClock.frozenOn(2012, Calendar.NOVEMBER, 6, 17, 22, 01, 0);  // Random pick
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.NOVEMBER, 7, 17, 22, 01, 0);  // 1 day later
        Clocks.setAppServerClock(frozenClock);
        Date now = eventClock.now();

        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(now, LogLevel.INFO, "testOccurrenceTimestamp");

        // Business method
        Date timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsLoggingRelated () {
        // Business method
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(Clocks.getAppServerClock().now(), LogLevel.DEBUG, "testLogLevel");

        // Asserts
        assertThat(event.isLoggingRelated()).isTrue();
    }

    @Test
    public void testLogLevel () {
        LogLevel expectedLogLevel = LogLevel.DEBUG;
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(Clocks.getAppServerClock().now(), expectedLogLevel, "testLogLevel");

        // Business method
        LogLevel logLevel = event.getLogLevel();

        // Asserts
        assertThat(logLevel).isEqualTo(expectedLogLevel);
    }

    @Test
    public void testLogMessage () {
        String expectedLogMessage = "testLogMessage";
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(Clocks.getAppServerClock().now(), LogLevel.DEBUG, expectedLogMessage);

        // Business method
        String logMessage = event.getLogMessage();

        // Asserts
        assertThat(logMessage).isEqualTo(expectedLogMessage);
    }

    @Test
    public void testIsNotDeviceRelated () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent();

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();

    }

    @Test
    public void testIsNotConnectionTaskRelated () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent();

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();

    }

    @Test
    public void testIsNotComPortRelated () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent();

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();

    }

    @Test
    public void testIsNotComPortPoolRelated () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent();

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();

    }

    @Test
    public void testIsNotComTaskExecutionRelated () {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent();

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();

    }

    @Test
    public void testSerializationDoesNotFailForDefaultObject () throws IOException {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Business method
        oos.writeObject(event);

        // Intend is to test that there are not failures
    }

    @Test
    public void testRestoreAfterSerializationForDefaultObject () throws IOException, ClassNotFoundException {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent();

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
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(Clocks.getAppServerClock().now(), LogLevel.INFO, "testSerializationDoesNotFail");

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
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(Clocks.getAppServerClock().now(), expectedLogLevel, expectedLogMessage);

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
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent();

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).doesNotMatch(UnrelatedLoggingEvent.class.getName() + "@\\d*");
    }

    @Test
    public void testToStringDoesNotFail () throws IOException, ClassNotFoundException {
        UnrelatedLoggingEvent event = new UnrelatedLoggingEvent(Clocks.getAppServerClock().now(), LogLevel.INFO, "testSerializationDoesNotFail");

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}