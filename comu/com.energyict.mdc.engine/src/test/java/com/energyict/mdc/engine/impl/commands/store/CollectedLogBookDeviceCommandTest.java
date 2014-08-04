package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.impl.meterdata.DeviceLogBook;
import com.energyict.mdc.engine.impl.meterdata.identifiers.LogBookIdentifierByIdImpl;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeFactory;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author sva
 * @since 30/09/13 - 9:51
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedLogBookDeviceCommandTest {

    private final int LOGBOOK_ID = 1;
    private static final int UNKNOWN = 0;
    
    @Mock
    private DeviceDataService deviceDataService;

    @Test
    public void testToJournalMessageDescriptionWhenLogBookHasNoMeterEvents() throws Exception {
        final LogBookIdentifier logBookIdentifier = new LogBookIdentifierByIdImpl(LOGBOOK_ID, deviceDataService);
        final DeviceLogBook deviceLogBook = new DeviceLogBook(logBookIdentifier);
        CollectedLogBookDeviceCommand command = new CollectedLogBookDeviceCommand(deviceLogBook, meterDataStoreCommand);

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).isEqualTo(CollectedLogBookDeviceCommand.class.getSimpleName() + " {logbook: 1; nr of events: 0}");
    }

    @Test
    public void testToJournalMessageDescriptionWhenLogBookHasMeterEvents() throws Exception {
        initializeEndDeviceEventTypeFactory();
        final LogBookIdentifier logBookIdentifier = new LogBookIdentifierByIdImpl(LOGBOOK_ID, deviceDataService);
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
        CollectedLogBookDeviceCommand command = new CollectedLogBookDeviceCommand(deviceLogBook, meterDataStoreCommand);

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).isEqualTo(CollectedLogBookDeviceCommand.class.getSimpleName() + " {logbook: 1; nr of events: 2}");
    }

    private void initializeEndDeviceEventTypeFactory() {
        MeteringService meteringService = mock(MeteringService.class);
        EndDeviceEventTypeFactory endDeviceEventTypeFactory = new EndDeviceEventTypeFactory();
        endDeviceEventTypeFactory.setMeteringService(meteringService);
        endDeviceEventTypeFactory.activate();
        // the getEventType will return null, if a specific result is required, then add it ot the meteringService MOCK
    }
}
