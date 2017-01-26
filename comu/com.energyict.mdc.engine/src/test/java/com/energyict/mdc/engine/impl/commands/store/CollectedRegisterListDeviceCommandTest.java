package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.engine.DeviceCreator;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceRegisterList;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceImpl;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.google.common.collect.Range;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the execution of the CollectedRegisterListDeviceCommand.
 *
 * @author sva
 * @since 21/01/13 - 11:21
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedRegisterListDeviceCommandTest {

    private static final long DEVICE_ID = 1;
    private static final long REGISTER_ID = 2;
    private static ObisCode REGISTER_OBIS = ObisCode.fromString("0.0.96.10.2.255");

    @Mock
    private ComServerDAO comServerDAO;
    @Mock
    private OfflineDevice offlineDevice;
    @Mock
    private OfflineRegister offlineRegister;
    @Mock
    private CollectedRegister collectedRegister;
    @Mock
    private RegisterIdentifier collectedRegisterIdentifier;
    @Mock
    private Device device;
    @Mock
    private DeviceCommand.ExecutionLogger executionLogger;
    @Mock
    private DeviceService deviceService;
    @Mock
    private DeviceCommand.ServiceProvider serviceProvider;
    @Mock
    private MeteringService meteringService;

    @Before
    public void initializeMocksAndFactories() {
        when(this.offlineDevice.getId()).thenReturn(DEVICE_ID);
        when(comServerDAO.findOfflineRegister(any(RegisterIdentifier.class), any(Instant.class))).thenReturn(Optional.of(offlineRegister));
        when(offlineRegister.getRegisterId()).thenReturn(REGISTER_ID);
        when(offlineRegister.getObisCode()).thenReturn(REGISTER_OBIS);
        when(offlineRegister.getOverFlowValue()).thenReturn(new BigDecimal(DeviceCreator.CHANNEL_OVERFLOW_VALUE));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        when(offlineRegister.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        when(this.collectedRegister.getCollectedQuantity()).thenReturn(new Quantity("2", Unit.getUndefined()));
        when(this.collectedRegister.getEventTime()).thenReturn(new Date(1358757000000L)); // 21 januari 2013 9:30:00
        when(this.collectedRegister.getFromTime()).thenReturn(new Date(1358755200000L));  // 21 januari 2013 9:00:00
        when(this.collectedRegister.getToTime()).thenReturn(new Date(1358758800000L));    // 21 januari 2013 10:00:00
        when(this.collectedRegister.getReadTime()).thenReturn(new Date(1358758920000L));  // 21 januari 2013 10:02:00
        when(this.collectedRegister.getText()).thenReturn("CollectedRegister text");
        when(this.collectedRegister.getResultType()).thenReturn(ResultType.Supported);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.3.72.0");
        when(this.offlineRegister.getReadingTypeMRID()).thenReturn(readingType.getMRID());
        when(this.collectedRegisterIdentifier.getRegisterObisCode()).thenReturn(REGISTER_OBIS);
        when(this.collectedRegister.getRegisterIdentifier()).thenReturn(this.collectedRegisterIdentifier);
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.meteringService.getReadingType(Matchers.<String>any())).thenReturn(Optional.empty());
        when(serviceProvider.mdcReadingTypeUtilService()).thenReturn(new MdcReadingTypeUtilServiceImpl(this.meteringService));
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void testExecutionOfDeviceCommand() {
        MeterDataStoreCommand meterDataStoreCommand = new MeterDataStoreCommandImpl(null, this.serviceProvider);
        CollectedRegisterListDeviceCommand command = new CollectedRegisterListDeviceCommand(getDeviceRegisterList(), null, meterDataStoreCommand, this.serviceProvider);
        command.logExecutionWith(this.executionLogger);

        // Business methods
        command.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // asserts
        ArgumentCaptor<MeterReading> argument = ArgumentCaptor.forClass(MeterReading.class);
        verify(comServerDAO).storeMeterReadings(any(DeviceIdentifier.class), argument.capture());
        MeterReading readingData = argument.getValue();

        Assert.assertEquals("Expecting only 1 registerValue", 1, readingData.getReadings().size());
        Reading registerValue = readingData.getReadings().get(0);
        Assert.assertEquals(collectedRegister.getCollectedQuantity().getAmount(), registerValue.getValue());
        Assert.assertEquals(collectedRegister.getEventTime(), registerValue.getTimeStamp());
        Assert.assertEquals(collectedRegister.getFromTime(), registerValue.getTimePeriod().filter(Range::hasLowerBound).map(Range::lowerEndpoint).orElse(null));
        Assert.assertEquals(collectedRegister.getToTime(), registerValue.getTimePeriod().filter(Range::hasUpperBound).map(Range::upperEndpoint).orElse(null));
    }
//
//    @Test
//    public void testUnlinkedDataLogger(){
//
//        Device dataLogger = this.deviceCreator
//                .name("DataLogger")
//                .mRDI("unLinkedDataLogger")
//                .loadProfileTypes(this.loadProfileType)
//                .deviceTypeName(DeviceCreator.DATA_LOGGER_DEVICE_TYPE_NAME)
//                .deviceConfigName(DeviceCreator.DATA_LOGGER_DEVICE_CONFIGURATION_NAME)
//                .dataLoggerEnabled(true)
//                .create(Instant.ofEpochMilli(fromClock.getTime()));
//        LoadProfile loadProfile = dataLogger.getLoadProfiles().get(0);
//        CollectedLoadProfile collectedLoadProfile =
//                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithTwoChannelsAndDataInFuture(loadProfile.getInterval()));
//        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(dataLogger);
//
//        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);
//
//        freezeClock(currentTimeStamp);
//
//        // Assert That the channels are not linked
//        assertThat(getTopologyService().getSlaveChannel(loadProfile.getOfflineChannels().get(0), fromClock.toInstant()).isPresent()).isFalse();
//        assertThat(getTopologyService().getSlaveChannel(loadProfile.getOfflineChannels().get(1), fromClock.toInstant()).isPresent()).isFalse();
//
//        assertThat(collectedLoadProfile.getCollectedIntervalData()).overridingErrorMessage("The collected data should contain {0} intervals to start", 6).hasSize(6);
//
//        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
//        PreStoreLoadProfile.CompositePreStoredLoadProfile preStoredLoadProfile = (PreStoreLoadProfile.CompositePreStoredLoadProfile) loadProfilePreStorer.preStore(collectedLoadProfile);
//
//        assertThat(preStoredLoadProfile.getPreStoreResult()).isEqualTo(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK);
//        assertThat(preStoredLoadProfile.getIntervalBlocks()).hasSize(2);
//
//        // All data should be 'Prestored' on the data logger channel
//        assertThat(preStoredLoadProfile.getPreStoredLoadProfiles()).hasSize(1);
//        PreStoreLoadProfile.PreStoredLoadProfile singlePreStoredLoadProfile = preStoredLoadProfile.getPreStoredLoadProfiles().get(0);
//        assertThat(singlePreStoredLoadProfile.getDeviceIdentifier().findDevice().getId()).isEqualTo(dataLogger.getId());
//
//        assertThat(singlePreStoredLoadProfile.getIntervalBlocks()).hasSize(2);
//        assertThat(singlePreStoredLoadProfile.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(loadProfile.getOfflineChannels().get(0).getReadingTypeMRID().getMRID());
//        assertThat(singlePreStoredLoadProfile.getIntervalBlocks().get(1).getReadingTypeCode()).isEqualTo(loadProfile.getOfflineChannels().get(1).getReadingTypeMRID().getMRID());
//        assertThat(singlePreStoredLoadProfile.getIntervalBlocks().get(0).getIntervals()).hasSize(4);
//        assertThat(singlePreStoredLoadProfile.getIntervalBlocks().get(1).getIntervals()).hasSize(4);
//
//    }

    @Test
    public void testToJournalMessageDescription() {
        CollectedRegisterListDeviceCommand command = new CollectedRegisterListDeviceCommand(getDeviceRegisterList(), null, new MeterDataStoreCommandImpl(null, this.serviceProvider), this.serviceProvider);
        command.logExecutionWith(this.executionLogger);

        // Business methods
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.DEBUG);

        // asserts
        assertThat(journalMessage).contains("{deviceIdentifier: device having id 1; nr of collected registers: 1}");
    }

    private DeviceRegisterList getDeviceRegisterList() {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        DeviceRegisterList deviceRegisterList = new DeviceRegisterList(deviceIdentifier);
        when(this.collectedRegisterIdentifier.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        deviceRegisterList.addCollectedRegister(collectedRegister);
        return deviceRegisterList;
    }
}
