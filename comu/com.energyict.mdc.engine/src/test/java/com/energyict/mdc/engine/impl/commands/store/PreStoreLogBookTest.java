/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.engine.DeviceCreator;
import com.energyict.mdc.engine.impl.commands.offline.OfflineLogBookImpl;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.offline.OfflineLogBook;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PreStoreLogBookTest extends AbstractCollectedDataIntegrationTest {

    private static final String DEVICE_NAME = "DeviceName";
    private static final int UNKNOWN = 0;

    Date futureIntervalEndTime1 = new DateTime(2014, 2, 2, 10, 45, 0, 0, DateTimeZone.UTC).toDate();
    Date eventTime1 = new DateTime(2014, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
    Date eventTime2 = new DateTime(2014, 1, 1, 0, 15, 0, 0, DateTimeZone.UTC).toDate();

    Date currentTimeStamp = new DateTime(2014, 1, 13, 10, 0, 0, 0, DateTimeZone.UTC).toDate();
    LogBookType logBookType;
    DeviceCreator deviceCreator;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private LogBookService logBookService;

    @Before
    public void setUp() {
        when(getClock().instant()).thenReturn(currentTimeStamp.toInstant());
        this.deviceCreator = new DeviceCreator(
                getInjector().getInstance(DeviceConfigurationService.class),
                getInjector().getInstance(DeviceService.class)
        );
        this.logBookType = createLogBookType();
        initializeEndDeviceEventTypeFactory();
    }

    private LogBookType createLogBookType() {
        LogBookType logBookType = getInjector().getInstance(MasterDataService.class).newLogBookType("MyLoadProfileType", ObisCode.fromString("0.0.97.98.0.255"));
        logBookType.save();
        return logBookType;
    }

    @Test
    @Transactional
    public void simplePreStoreWithoutIssuesTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("simplePreStoreWithoutIssuesTest").logBookTypes(this.logBookType).create(Instant.ofEpochMilli(currentTimeStamp.getTime()));
        LogBook logBook = device.getLogBooks().get(0);
        CollectedLogBook collectedLogBook = enhanceCollectedLogBook(logBook, createMockedCollectedLogBook());
        OfflineLogBookImpl offlineLogBook = new OfflineLogBookImpl(logBook, this.identificationService);

        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLogBook);

        freezeClock(currentTimeStamp);

        assertThat(collectedLogBook.getCollectedMeterEvents()).overridingErrorMessage("The collected data should contain {0} events to start", 2).hasSize(2);

        PreStoreLogBook preStoreLogBook = new PreStoreLogBook(getClock(), comServerDAO);
        Optional<Pair<DeviceIdentifier, PreStoreLogBook.LocalLogBook>> localLogBook = preStoreLogBook.preStore(collectedLogBook);

        assertThat(localLogBook).isPresent();
        assertThat(localLogBook.get().getLast().getEndDeviceEvents()).hasSize(2);
    }

    @Test
    @Transactional
    public void simplePreStoreWithDataInFutureTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("simplePreStoreWithDataInFutureTest").logBookTypes(this.logBookType).create(Instant.ofEpochMilli(currentTimeStamp.getTime()));
        LogBook logBook = device.getLogBooks().get(0);
        CollectedLogBook collectedLogBook = enhanceCollectedLogBook(logBook, createMockedCollectedLogBookWithEventInFuture());
        OfflineLogBookImpl offlineLogBook = new OfflineLogBookImpl(logBook, identificationService);

        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLogBook);

        freezeClock(currentTimeStamp);

        assertThat(collectedLogBook.getCollectedMeterEvents()).overridingErrorMessage("The collected data should contain {0} events to start", 2).hasSize(2);

        PreStoreLogBook preStoreLogBook = new PreStoreLogBook(getClock(), comServerDAO);
        Optional<Pair<DeviceIdentifier, PreStoreLogBook.LocalLogBook>> localLogBook = preStoreLogBook.preStore(collectedLogBook);

        assertThat(localLogBook).isPresent();
        assertThat(localLogBook.get().getLast().getEndDeviceEvents()).hasSize(1);
    }

    @Test
    @Transactional
    public void preStoreWithDuplicatesTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithDuplicatesTest").logBookTypes(this.logBookType).create(Instant.ofEpochMilli(currentTimeStamp.getTime()));
        LogBook logBook = device.getLogBooks().get(0);
        CollectedLogBook collectedLogBook = enhanceCollectedLogBook(logBook, createMockedCollectedLogBookWithDuplicates());
        OfflineLogBookImpl offlineLogBook = new OfflineLogBookImpl(logBook, identificationService);

        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLogBook);

        freezeClock(currentTimeStamp);

        assertThat(collectedLogBook.getCollectedMeterEvents()).overridingErrorMessage("The collected data should contain {0} events to start", 4).hasSize(4);

        PreStoreLogBook preStoreLogBook = new PreStoreLogBook(getClock(), comServerDAO);
        Optional<Pair<DeviceIdentifier, PreStoreLogBook.LocalLogBook>> localLogBook = preStoreLogBook.preStore(collectedLogBook);

        assertThat(localLogBook).isPresent();
        assertThat(localLogBook.get().getLast().getEndDeviceEvents()).hasSize(2);
    }

    protected ComServerDAOImpl mockComServerDAOWithOfflineLoadProfile(OfflineLogBook offlineLogBook) {
        final ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        doCallRealMethod().when(comServerDAO).storeMeterReadings(any(DeviceIdentifier.class), any(MeterReading.class));
        when(comServerDAO.executeTransaction(any())).thenAnswer(invocation -> ((Transaction<?>) invocation.getArguments()[0]).perform());
        when(comServerDAO.findOfflineLogBook(any(LogBookIdentifier.class))).thenReturn(Optional.of(offlineLogBook));
        DeviceIdentifier deviceIdentifier = offlineLogBook.getDeviceIdentifier();
        when(comServerDAO.getDeviceIdentifierFor(any(LogBookIdentifier.class))).thenReturn(deviceIdentifier);
        doCallRealMethod().when(comServerDAO).updateLastLogBook(any(LogBookIdentifier.class), any(Instant.class));
        return comServerDAO;
    }

    CollectedLogBook createMockedCollectedLogBook() {
        CollectedLogBook collectedLogBook = mock(CollectedLogBook.class, RETURNS_DEEP_STUBS);
        MeterProtocolEvent powerDownEvent = new MeterProtocolEvent(eventTime2,
                MeterEvent.POWERDOWN,
                UNKNOWN,
                EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERDOWN),
                "Power down",
                UNKNOWN,
                UNKNOWN);
        MeterProtocolEvent powerUpEvent = new MeterProtocolEvent(eventTime1,
                MeterEvent.POWERDOWN,
                UNKNOWN,
                EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERUP),
                "Power up",
                UNKNOWN,
                UNKNOWN);
        List<MeterProtocolEvent> meterEvents = Arrays.asList(powerDownEvent, powerUpEvent);
        when(collectedLogBook.getCollectedMeterEvents()).thenReturn(meterEvents);
        return collectedLogBook;
    }

    CollectedLogBook createMockedCollectedLogBookWithEventInFuture() {
        CollectedLogBook collectedLogBook = mock(CollectedLogBook.class, RETURNS_DEEP_STUBS);
        MeterProtocolEvent powerDownEvent = new MeterProtocolEvent(futureIntervalEndTime1,
                MeterEvent.POWERDOWN,
                UNKNOWN,
                EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERDOWN),
                "Power down",
                UNKNOWN,
                UNKNOWN);
        MeterProtocolEvent powerUpEvent = new MeterProtocolEvent(eventTime1,
                MeterEvent.POWERDOWN,
                UNKNOWN,
                EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERUP),
                "Power up",
                UNKNOWN,
                UNKNOWN);
        List<MeterProtocolEvent> meterEvents = Arrays.asList(powerDownEvent, powerUpEvent);
        when(collectedLogBook.getCollectedMeterEvents()).thenReturn(meterEvents);
        return collectedLogBook;
    }

    CollectedLogBook createMockedCollectedLogBookWithDuplicates() {
        CollectedLogBook collectedLogBook = mock(CollectedLogBook.class, RETURNS_DEEP_STUBS);
        MeterProtocolEvent powerDownEvent = new MeterProtocolEvent(eventTime1,
                MeterEvent.POWERDOWN,
                UNKNOWN,
                EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERDOWN),
                "Power down",
                UNKNOWN,
                UNKNOWN);
        MeterProtocolEvent powerUpEvent = new MeterProtocolEvent(eventTime2,
                MeterEvent.POWERDOWN,
                UNKNOWN,
                EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERUP),
                "Power up",
                UNKNOWN,
                UNKNOWN);
        List<MeterProtocolEvent> meterEvents = Arrays.asList(powerDownEvent, powerUpEvent, powerDownEvent, powerDownEvent);
        when(collectedLogBook.getCollectedMeterEvents()).thenReturn(meterEvents);
        return collectedLogBook;
    }

    CollectedLogBook enhanceCollectedLogBook(LogBook logBook, CollectedLogBook collectedLogBook) {
        LogBookIdentifier logBookIdentifier = mock(LogBookIdentifier.class);
        // Todo: figure out where the logBookService needs to be injected into
        when(this.logBookService.findByIdentifier(logBookIdentifier)).thenReturn(Optional.of(logBook));
        when(collectedLogBook.getLogBookIdentifier()).thenReturn(logBookIdentifier);
        return collectedLogBook;
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
    }

}