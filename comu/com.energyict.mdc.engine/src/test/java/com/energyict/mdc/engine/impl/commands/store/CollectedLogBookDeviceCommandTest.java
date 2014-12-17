package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.engine.impl.meterdata.DeviceLogBook;
import com.energyict.mdc.device.data.impl.identifiers.LogBookIdentifierById;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeFactory;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 30/09/13 - 9:51
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedLogBookDeviceCommandTest {

    private final int LOGBOOK_ID = 1;
    private static final int UNKNOWN = 0;

    @Mock
    private LogBookService logBookService;

    @Test
    public void testToJournalMessageDescriptionWhenLogBookHasNoMeterEvents() throws Exception {
        final LogBookIdentifier logBookIdentifier = new LogBookIdentifierById(LOGBOOK_ID, logBookService);
        final DeviceLogBook deviceLogBook = new DeviceLogBook(logBookIdentifier);
        CollectedLogBookDeviceCommand command = new CollectedLogBookDeviceCommand(deviceLogBook, new MeterDataStoreCommand());

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).isEqualTo(CollectedLogBookDeviceCommand.class.getSimpleName() + " {logbook: 1; nr of events: 0}");
    }

    @Test
    public void testToJournalMessageDescriptionWhenLogBookHasMeterEvents() throws Exception {
        initializeEndDeviceEventTypeFactory();
        final LogBookIdentifier logBookIdentifier = new LogBookIdentifierById(LOGBOOK_ID, logBookService);
        final DeviceLogBook deviceLogBook = new DeviceLogBook(logBookIdentifier);
        List<MeterProtocolEvent> meterEvents = new ArrayList<>(2);
        meterEvents.add(
                new MeterProtocolEvent(new DateTime(2013, 9, 30, 9, 1, 0, 0).toDate(),
                        MeterEvent.POWERDOWN,
                        UNKNOWN,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERDOWN),
                        "Power down",
                        UNKNOWN,
                        UNKNOWN));
        meterEvents.add(
                new MeterProtocolEvent(new DateTime(2013, 9, 30, 9, 4, 0, 0).toDate(),
                        MeterEvent.POWERUP,
                        UNKNOWN,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERUP),
                        "Power up",
                        UNKNOWN,
                        UNKNOWN));
        deviceLogBook.setMeterEvents(meterEvents);
        CollectedLogBookDeviceCommand command = new CollectedLogBookDeviceCommand(deviceLogBook, new MeterDataStoreCommand());

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).isEqualTo(CollectedLogBookDeviceCommand.class.getSimpleName() + " {logbook: 1; nr of events: 2}");
    }

    private void initializeEndDeviceEventTypeFactory() {
        MeteringService meteringService = mock(MeteringService.class);
        EndDeviceEventType powerUp = mock(EndDeviceEventType.class);
        String powerUpEventMRID = "0.26.38.49";
        when(powerUp.getMRID()).thenReturn(powerUpEventMRID);
        EndDeviceEventType powerDown = mock(EndDeviceEventType.class);
        String powerDownEventMRID = "0.26.38.47";
        when(powerDown.getMRID()).thenReturn(powerDownEventMRID);
        Optional<EndDeviceEventType> hardwareErrorOptional = Optional.of(powerUp);
        when(meteringService.getEndDeviceEventType(powerUpEventMRID)).thenReturn(hardwareErrorOptional);
        Optional<EndDeviceEventType> powerDownEventOptional = Optional.of(powerDown);
        when(meteringService.getEndDeviceEventType(powerDownEventMRID)).thenReturn(powerDownEventOptional);
        EndDeviceEventTypeFactory endDeviceEventTypeFactory = new EndDeviceEventTypeFactory();
        endDeviceEventTypeFactory.setMeteringService(meteringService);
        endDeviceEventTypeFactory.activate();
        // the getEventType will return null, if a specific result is required, then add it ot the meteringService MOCK
    }
}
