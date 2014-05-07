package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.comserver.time.Clocks;
import com.energyict.comserver.time.FrozenClock;
import com.energyict.mdc.meterdata.DeviceLogBook;
import com.energyict.mdc.meterdata.identifiers.LogBookIdentifierByIdImpl;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.engine.model.ComServer;
import org.junit.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author sva
 * @since 30/09/13 - 9:51
 */
public class CollectedLogBookDeviceCommandTest {

    private final int LOGBOOK_ID = 1;
    private static final int UNKNOWN = 0;

    @After
    public void resetTimeFactory() throws SQLException {
        Clocks.resetAll();
    }

    @Test
    public void testToJournalMessageDescriptionWhenLogBookHasNoMeterEvents() throws Exception {
        final LogBookIdentifier logBookIdentifier = new LogBookIdentifierByIdImpl(LOGBOOK_ID);
        final DeviceLogBook deviceLogBook = new DeviceLogBook(logBookIdentifier);
        CollectedLogBookDeviceCommand command = new CollectedLogBookDeviceCommand(deviceLogBook, issueService, clock);

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        Assertions.assertThat(journalMessage).isEqualTo(CollectedLogBookDeviceCommand.class.getSimpleName() + " {logbook: 1; nr of events: 0}");
    }

    @Test
    public void testToJournalMessageDescriptionWhenLogBookHasMeterEvents() throws Exception {
        final LogBookIdentifier logBookIdentifier = new LogBookIdentifierByIdImpl(LOGBOOK_ID);
        final DeviceLogBook deviceLogBook = new DeviceLogBook(logBookIdentifier);
        List<MeterProtocolEvent> meterEvents = new ArrayList<>(2);
        meterEvents.add(
                        new MeterProtocolEvent(FrozenClock.frozenOn(2013, Calendar.SEPTEMBER, 30, 9, 1, 0, 0).now(),
                                MeterEvent.POWERDOWN,
                                UNKNOWN,
                                EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERDOWN),
                                "Power down",
                                UNKNOWN,
                                UNKNOWN));
        meterEvents.add(
                        new MeterProtocolEvent(FrozenClock.frozenOn(2013, Calendar.SEPTEMBER, 30, 9, 4, 0, 0).now(),
                                MeterEvent.POWERUP,
                                UNKNOWN,
                                EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERUP),
                                "Power up",
                                UNKNOWN,
                                UNKNOWN));
        deviceLogBook.setMeterEvents(meterEvents);
        CollectedLogBookDeviceCommand command = new CollectedLogBookDeviceCommand(deviceLogBook, issueService, clock);

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        Assertions.assertThat(journalMessage).isEqualTo(CollectedLogBookDeviceCommand.class.getSimpleName() + " {logbook: 1; nr of events: 2}");
    }
}
