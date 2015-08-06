package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.engine.impl.meterdata.DeviceLogBook;
import com.energyict.mdc.device.data.impl.identifiers.LogBookIdentifierById;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import java.util.Optional;
import org.joda.time.DateTime;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    @Mock
    private MeteringService meteringService;

    @Before
    public void initializeMocks() {
        EndDeviceEventType powerDownEndDeviceEventType = mock(EndDeviceEventType.class, Mockito.RETURNS_DEEP_STUBS);
        when(this.meteringService.getEndDeviceEventType(EndDeviceEventTypeMapping.POWERDOWN.getEndDeviceEventTypeMRID())).thenReturn(Optional.of(powerDownEndDeviceEventType));
        EndDeviceEventType powerUpEndDeviceEventType = mock(EndDeviceEventType.class, Mockito.RETURNS_DEEP_STUBS);
        when(this.meteringService.getEndDeviceEventType(EndDeviceEventTypeMapping.POWERUP.getEndDeviceEventTypeMRID())).thenReturn(Optional.of(powerUpEndDeviceEventType));
        EndDeviceEventType otherEndDeviceEventType = mock(EndDeviceEventType.class, Mockito.RETURNS_DEEP_STUBS);
        when(this.meteringService.getEndDeviceEventType(EndDeviceEventTypeMapping.OTHER.getEndDeviceEventTypeMRID())).thenReturn(Optional.of(otherEndDeviceEventType));
    }
    @Test
    public void testToJournalMessageDescriptionWhenLogBookHasNoMeterEvents() throws Exception {
        final LogBookIdentifier logBookIdentifier = new LogBookIdentifierById(LOGBOOK_ID, logBookService);
        final DeviceLogBook deviceLogBook = new DeviceLogBook(logBookIdentifier);
        NoDeviceCommandServices serviceProvider = new NoDeviceCommandServices();
        CollectedLogBookDeviceCommand command = new CollectedLogBookDeviceCommand(deviceLogBook, new MeterDataStoreCommandImpl(serviceProvider));

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).contains("{logbook: 1; nr of events: 0}");
    }

    @Test
    public void testToJournalMessageDescriptionWhenLogBookHasMeterEvents() throws Exception {
        initializeMeteringService();
        final LogBookIdentifier logBookIdentifier = new LogBookIdentifierById(LOGBOOK_ID, logBookService);
        final DeviceLogBook deviceLogBook = new DeviceLogBook(logBookIdentifier);
        List<MeterProtocolEvent> meterEvents = new ArrayList<>(2);
        meterEvents.add(
                new MeterProtocolEvent(new DateTime(2013, 9, 30, 9, 1, 0, 0).toDate(),
                        MeterEvent.POWERDOWN,
                        UNKNOWN,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERDOWN, this.meteringService).orElseThrow(() -> new RuntimeException("Powerdown event type was not setup correctly in this unit test")),
                        "Power down",
                        UNKNOWN,
                        UNKNOWN));
        meterEvents.add(
                new MeterProtocolEvent(new DateTime(2013, 9, 30, 9, 4, 0, 0).toDate(),
                        MeterEvent.POWERUP,
                        UNKNOWN,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERUP, this.meteringService).orElseThrow(() -> new RuntimeException("Powerdown event type was not setup correctly in this unit test")),
                        "Power up",
                        UNKNOWN,
                        UNKNOWN));
        deviceLogBook.setMeterEvents(meterEvents);
        NoDeviceCommandServices serviceProvider = new NoDeviceCommandServices();
        CollectedLogBookDeviceCommand command = new CollectedLogBookDeviceCommand(deviceLogBook, new MeterDataStoreCommandImpl(serviceProvider));

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).contains("{logbook: 1; nr of events: 2}");
    }

    private void initializeMeteringService() {
        EndDeviceEventType powerUp = mock(EndDeviceEventType.class);
        String powerUpEventMRID = "0.26.38.49";
        when(powerUp.getMRID()).thenReturn(powerUpEventMRID);
        EndDeviceEventType powerDown = mock(EndDeviceEventType.class);
        String powerDownEventMRID = "0.26.38.47";
        when(powerDown.getMRID()).thenReturn(powerDownEventMRID);
        Optional<EndDeviceEventType> hardwareErrorOptional = Optional.of(powerUp);
        when(this.meteringService.getEndDeviceEventType(powerUpEventMRID)).thenReturn(hardwareErrorOptional);
        Optional<EndDeviceEventType> powerDownEventOptional = Optional.of(powerDown);
        when(this.meteringService.getEndDeviceEventType(powerDownEventMRID)).thenReturn(powerDownEventOptional);
    }

}
