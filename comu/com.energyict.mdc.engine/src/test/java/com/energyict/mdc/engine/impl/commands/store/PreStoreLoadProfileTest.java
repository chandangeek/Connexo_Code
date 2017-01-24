package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.energyict.cbo.Unit;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierForAlreadyKnownDeviceByMrID;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierForAlreadyKnownLoadProfile;
import com.energyict.mdc.engine.DeviceCreator;
import com.energyict.mdc.engine.impl.commands.offline.OfflineLoadProfileImpl;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifierType;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.google.common.collect.Range;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests the store functionality of the {@link CollectedLoadProfileDeviceCommand}.
 * <p>
 * Copyrights EnergyICT
 * Date: 13/01/14
 * Time: 15:30
 */
@RunWith(MockitoJUnitRunner.class)
public class PreStoreLoadProfileTest extends AbstractCollectedDataIntegrationTest {

    static final String DEVICE_NAME = "DeviceName";

    final int intervalValueOne = 123;
    final int intervalValueTwo = 651;
    final Unit kiloWattHours = Unit.get("kWh");
    final Unit wattHours = Unit.get("Wh");
    private final ObisCode obisCodeActiveImport = ObisCode.fromString("1.0.1.8.0.255");
    private final ObisCode obisCodeActiveExport = ObisCode.fromString("1.0.2.8.0.255");
    private final TimeDuration loadProfileInterval = TimeDuration.minutes(15);

    Date verificationTimeStamp = new DateTime(2015, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
    Date currentTimeStamp = new DateTime(2014, 1, 13, 10, 0, 0, 0, DateTimeZone.UTC).toDate();

    Date fromClock = new DateTime(2013, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
    Date intervalEndTime1 = new DateTime(2014, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
    Date intervalEndTime2 = new DateTime(2014, 1, 1, 0, 15, 0, 0, DateTimeZone.UTC).toDate();
    Date intervalEndTime3 = new DateTime(2014, 1, 1, 0, 30, 0, 0, DateTimeZone.UTC).toDate();
    Date intervalEndTime4 = new DateTime(2014, 1, 1, 0, 45, 0, 0, DateTimeZone.UTC).toDate();

    Date futureIntervalEndTime1 = new DateTime(2014, 2, 2, 10, 45, 0, 0, DateTimeZone.UTC).toDate();
    Date futureIntervalEndTime2 = new DateTime(2014, 2, 2, 11, 0, 0, 0, DateTimeZone.UTC).toDate();

    DeviceCreator deviceCreator, slaveDeviceCreator;
    LoadProfileType loadProfileType;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private ComServerDAOImpl.ServiceProvider serviceProvider;
    @Mock
    private User comServerUser;
    @Mock
    private IssueService issueService;

    protected ComServerDAOImpl.ServiceProvider getComServerDAOServiceProvider() {
        return serviceProvider;
    }

    public List<IntervalValue> getIntervalValues(int addition) {
        List<IntervalValue> intervalValues = new ArrayList<>();
        intervalValues.add(new IntervalValue(intervalValueOne + addition, 0, new HashSet<>()));
        intervalValues.add(new IntervalValue(intervalValueTwo + addition, 0, new HashSet<>()));
        return intervalValues;
    }

    @Before
    public void setUp() {
        when(getClock().instant()).thenReturn(fromClock.toInstant());
        this.deviceCreator = new DeviceCreator(
                getInjector().getInstance(DeviceConfigurationService.class),
                getInjector().getInstance(DeviceService.class)
        );
        this.slaveDeviceCreator = (DeviceCreator) new DeviceCreator(
                getInjector().getInstance(DeviceConfigurationService.class),
                getInjector().getInstance(DeviceService.class)
        ).dataLoggerSlaveDevice();

        this.loadProfileType = createLoadProfileType();
        when(this.identificationService.createDeviceIdentifierForAlreadyKnownDevice(any())).thenAnswer(invocationOnMock -> new DeviceIdentifierForAlreadyKnownDeviceByMrID((Device) invocationOnMock.getArguments()[0]));
        when(this.identificationService.createLoadProfileIdentifierForAlreadyKnownLoadProfile(any(), any(ObisCode.class))).thenAnswer(
                invocationOnMock -> new LoadProfileIdentifierForAlreadyKnownLoadProfile(
                        (LoadProfile) invocationOnMock.getArguments()[0], ObisCode.fromString("1.0.99.1.0.255"))
        );
        when(this.serviceProvider.topologyService()).thenReturn(getTopologyService());
        when(this.serviceProvider.identificationService()).thenReturn(this.identificationService);
    }

    private LoadProfileType createLoadProfileType() {
        RegisterType registerType = getMasterDataService().findRegisterTypeByReadingType(getMeteringService().getReadingType(getMdcReadingTypeUtilService().getReadingTypeMridFrom(obisCodeActiveImport, kiloWattHours))
                .get()).get();
        RegisterType registerType1 = getMasterDataService().findRegisterTypeByReadingType(getMeteringService().getReadingType(getMdcReadingTypeUtilService().getReadingTypeMridFrom(obisCodeActiveExport, kiloWattHours))
                .get()).get();
        LoadProfileType loadProfileType = getInjector().getInstance(MasterDataService.class)
                .newLoadProfileType("MyLoadProfileType", ObisCode.fromString("1.0.99.1.0.255"), loadProfileInterval, Arrays.asList(registerType, registerType1));
        loadProfileType.save();
        return loadProfileType;
    }

    @Test
    @Transactional
    public void simplePreStoreWithDataInFutureTest() {

        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("simplePreStoreWithDataInFutureTest").loadProfileTypes(this.loadProfileType).create(Instant.ofEpochMilli(fromClock.getTime()));
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithTwoChannelsAndDataInFuture(loadProfile.getInterval()));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(currentTimeStamp);

        assertThat(collectedLoadProfile.getCollectedIntervalData()).overridingErrorMessage("The collected data should contain {0} intervals to start", 6).hasSize(6);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.PreStoredLoadProfile preStoredLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(preStoredLoadProfile.getPreStoreResult()).isEqualTo(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK);
        assertThat(preStoredLoadProfile.getIntervalBlocks()).hasSize(2);
        assertThat(preStoredLoadProfile.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(loadProfile.getChannels().get(0).getReadingType().getMRID());
        assertThat(preStoredLoadProfile.getIntervalBlocks().get(1).getReadingTypeCode()).isEqualTo(loadProfile.getChannels().get(1).getReadingType().getMRID());
        assertThat(preStoredLoadProfile.getIntervalBlocks().get(0).getIntervals()).hasSize(4);
        assertThat(preStoredLoadProfile.getIntervalBlocks().get(1).getIntervals()).hasSize(4);
    }

    @Test
    @Transactional
    public void preStoreWithMultiplierTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithMultiplierTest").loadProfileTypes(this.loadProfileType).create(Instant.ofEpochMilli(fromClock.getTime()));
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        BigDecimal multiplier = BigDecimal.valueOf(50L);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithMultiplier(loadProfile.getInterval(), multiplier));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(currentTimeStamp);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.PreStoredLoadProfile preStoredLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(preStoredLoadProfile.getPreStoreResult()).isEqualTo(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK);
        for (int i = 0; i < collectedLoadProfile.getCollectedIntervalData().size(); i++) {
            IntervalData intervalData = collectedLoadProfile.getCollectedIntervalData().get(i);
            for (int j = 0; j < intervalData.getIntervalValues().size(); j++) {
                IntervalValue intervalValue = intervalData.getIntervalValues().get(j);
                IntervalReading intervalReading = preStoredLoadProfile.getIntervalBlocks().get(j).getIntervals().get(i);
                // Calculate the expected value, don't forget the scaler that was mocked
                BigDecimal expectedValue = new BigDecimal(intervalValue.getNumber().toString()).scaleByPowerOfTen(3).multiply(multiplier);
                assertThat(intervalReading.getValue()).isEqualByComparingTo(expectedValue);
            }
        }
    }

    @Test
    @Transactional
    public void preStoreWithPositiveUnitConversionTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithPositiveUnitConversionTest").loadProfileTypes(this.loadProfileType).create(Instant.ofEpochMilli(fromClock.getTime()));
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithPositiveScaler(loadProfile.getInterval()));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(currentTimeStamp);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.PreStoredLoadProfile preStoredLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(preStoredLoadProfile.getPreStoreResult()).isEqualTo(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK);
        for (int i = 0; i < collectedLoadProfile.getCollectedIntervalData().size(); i++) {
            IntervalData intervalData = collectedLoadProfile.getCollectedIntervalData().get(i);
            for (int j = 0; j < intervalData.getIntervalValues().size(); j++) {
                IntervalValue intervalValue = intervalData.getIntervalValues().get(j);
                IntervalReading intervalReading = preStoredLoadProfile.getIntervalBlocks().get(j).getIntervals().get(i);
                assertThat(new BigDecimal(intervalValue.getNumber().toString()).multiply(BigDecimal.valueOf(1000)).compareTo(intervalReading.getValue()))
                        .overridingErrorMessage("Values are not the same -> %s and %s",
                                new BigDecimal(intervalValue.getNumber().toString()).multiply(BigDecimal.valueOf(1000)),
                                intervalReading.getValue())
                        .isEqualTo(0);
            }
        }
    }

    @Test
    @Transactional
    public void preStoreWithNegativeUnitConversionTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithNegativeUnitConversionTest").loadProfileTypes(this.loadProfileType).create(Instant.ofEpochMilli(fromClock.getTime()));
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithNegativeScaler(loadProfile.getInterval()));

        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);
        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(currentTimeStamp);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.PreStoredLoadProfile preStoredLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(preStoredLoadProfile.getPreStoreResult()).isEqualTo(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK);
        for (int i = 0; i < collectedLoadProfile.getCollectedIntervalData().size(); i++) {
            IntervalData intervalData = collectedLoadProfile.getCollectedIntervalData().get(i);
            for (int j = 0; j < intervalData.getIntervalValues().size(); j++) {
                IntervalValue intervalValue = intervalData.getIntervalValues().get(j);
                IntervalReading intervalReading = preStoredLoadProfile.getIntervalBlocks().get(j).getIntervals().get(i);
                assertThat(new BigDecimal(intervalValue.getNumber().toString()).divide(BigDecimal.valueOf(1000)).compareTo(intervalReading.getValue()))
                        .overridingErrorMessage("Values are not the same -> %s and %s",
                                new BigDecimal(intervalValue.getNumber().toString()).divide(BigDecimal.valueOf(1000)),
                                intervalReading.getValue())
                        .isEqualTo(0);
            }
        }
    }


    public OfflineLoadProfile createMockedOfflineLoadProfile(Device device) {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        when(deviceIdentifier.getDeviceIdentifierType()).thenReturn(DeviceIdentifierType.ActualDevice);
        when(deviceIdentifier.getIdentifier()).thenReturn(String.valueOf(device.getId()));
        when(this.identificationService.createDeviceIdentifierForAlreadyKnownDevice(device)).thenReturn(deviceIdentifier);
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        return new OfflineLoadProfileImpl(loadProfile, getTopologyService(), this.identificationService);
    }

    @Test
    @Transactional
    public void removeLastReadingIntervalTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME)
                .mRDI("preStoreWithNegativeScalingAndOverflowExceededTest")
                .loadProfileTypes(this.loadProfileType)
                .create(Instant.ofEpochMilli(fromClock.getTime()));
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        device.getLoadProfileUpdaterFor(loadProfile).setLastReading(intervalEndTime1.toInstant()).update();
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createCollectedLoadProfile(loadProfile));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);
        freezeClock(currentTimeStamp);
        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.PreStoredLoadProfile preStoredLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(preStoredLoadProfile.getPreStoreResult()).isEqualTo(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK);
        assertThat(preStoredLoadProfile.getIntervalBlocks().get(0).getIntervals()).hasSize(3);
        assertThat(preStoredLoadProfile.getIntervalBlocks().get(0).getIntervals().get(0).getTimeStamp()).isEqualTo(intervalEndTime2.toInstant());
        assertThat(preStoredLoadProfile.getIntervalBlocks().get(0).getIntervals().get(1).getTimeStamp()).isEqualTo(intervalEndTime3.toInstant());
        assertThat(preStoredLoadProfile.getIntervalBlocks().get(0).getIntervals().get(2).getTimeStamp()).isEqualTo(intervalEndTime4.toInstant());
    }

    @Test
    @Transactional
    public void testUpperRangeValueWithCurrentTimeStamp() {
        Device device = this.deviceCreator.name(DEVICE_NAME)
                .mRDI("preStoreWithNegativeScalingAndOverflowExceededTest")
                .loadProfileTypes(this.loadProfileType)
                .create(Instant.ofEpochMilli(fromClock.getTime()));
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        device.getLoadProfileUpdaterFor(loadProfile).setLastReading(intervalEndTime1.toInstant()).update();
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createCollectedLoadProfile(loadProfile));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);
        freezeClock(intervalEndTime4);
        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.PreStoredLoadProfile preStoredLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(preStoredLoadProfile.getPreStoreResult()).isEqualTo(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK);
        assertThat(preStoredLoadProfile.getIntervalBlocks().get(0).getIntervals()).hasSize(3);
        assertThat(preStoredLoadProfile.getIntervalBlocks().get(0).getIntervals().get(0).getTimeStamp()).isEqualTo(intervalEndTime2.toInstant());
        assertThat(preStoredLoadProfile.getIntervalBlocks().get(0).getIntervals().get(1).getTimeStamp()).isEqualTo(intervalEndTime3.toInstant());
        assertThat(preStoredLoadProfile.getIntervalBlocks().get(0).getIntervals().get(2).getTimeStamp()).isEqualTo(intervalEndTime4.toInstant());
    }

    @Test
    @Transactional
    public void testUpperRangeValueAfterCurrentTimeStamp() {
        Device device = this.deviceCreator.name(DEVICE_NAME)
                .mRDI("preStoreWithNegativeScalingAndOverflowExceededTest")
                .loadProfileTypes(this.loadProfileType)
                .create(Instant.ofEpochMilli(fromClock.getTime()));
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        device.getLoadProfileUpdaterFor(loadProfile).setLastReading(intervalEndTime1.toInstant()).update();
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createCollectedLoadProfile(loadProfile));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);
        freezeClock(intervalEndTime3);
        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.PreStoredLoadProfile preStoredLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(preStoredLoadProfile.getPreStoreResult()).isEqualTo(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK);
        assertThat(preStoredLoadProfile.getIntervalBlocks().get(0).getIntervals()).hasSize(2);
        assertThat(preStoredLoadProfile.getIntervalBlocks().get(0).getIntervals().get(0).getTimeStamp()).isEqualTo(intervalEndTime2.toInstant());
        assertThat(preStoredLoadProfile.getIntervalBlocks().get(0).getIntervals().get(1).getTimeStamp()).isEqualTo(intervalEndTime3.toInstant());
    }

    @Test
    @Transactional
    public void testUnlinkedDataLogger() {
        Device dataLogger = this.deviceCreator
                .name("DataLogger")
                .mRDI("unLinkedDataLogger")
                .loadProfileTypes(this.loadProfileType)
                .deviceTypeName(DeviceCreator.DATA_LOGGER_DEVICE_TYPE_NAME)
                .deviceConfigName(DeviceCreator.DATA_LOGGER_DEVICE_CONFIGURATION_NAME)
                .dataLoggerEnabled(true)
                .create(Instant.ofEpochMilli(fromClock.getTime()));
        LoadProfile loadProfile = dataLogger.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithTwoChannelsAndDataInFuture(loadProfile.getInterval()));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(dataLogger);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(currentTimeStamp);

        // Assert That the channels are not linked
        assertThat(getTopologyService().getSlaveChannel(loadProfile.getChannels().get(0), fromClock.toInstant()).isPresent()).isFalse();
        assertThat(getTopologyService().getSlaveChannel(loadProfile.getChannels().get(1), fromClock.toInstant()).isPresent()).isFalse();

        assertThat(collectedLoadProfile.getCollectedIntervalData()).overridingErrorMessage("The collected data should contain {0} intervals to start", 6).hasSize(6);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.CompositePreStoredLoadProfile preStoredLoadProfile = (PreStoreLoadProfile.CompositePreStoredLoadProfile) loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(preStoredLoadProfile.getPreStoreResult()).isEqualTo(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK);
        assertThat(preStoredLoadProfile.getIntervalBlocks()).hasSize(2);

        // All data should be 'Prestored' on the data logger channel
        assertThat(preStoredLoadProfile.getPreStoredLoadProfiles()).hasSize(1);
        PreStoreLoadProfile.PreStoredLoadProfile singlePreStoredLoadProfile = preStoredLoadProfile.getPreStoredLoadProfiles().get(0);
        Device device = (Device) singlePreStoredLoadProfile.getDeviceIdentifier().findDevice(); //Downcast to Connexo Device
        assertThat(device.getId()).isEqualTo(dataLogger.getId());

        assertThat(singlePreStoredLoadProfile.getIntervalBlocks()).hasSize(2);
        assertThat(singlePreStoredLoadProfile.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(loadProfile.getChannels().get(0).getReadingType().getMRID());
        assertThat(singlePreStoredLoadProfile.getIntervalBlocks().get(1).getReadingTypeCode()).isEqualTo(loadProfile.getChannels().get(1).getReadingType().getMRID());
        assertThat(singlePreStoredLoadProfile.getIntervalBlocks().get(0).getIntervals()).hasSize(4);
        assertThat(singlePreStoredLoadProfile.getIntervalBlocks().get(1).getIntervals()).hasSize(4);

    }

    @Test
    @Transactional
    public void testLinkedDataLoggerForWholePeriod() {
        Device dataLogger = this.deviceCreator
                .name("DataLogger")
                .mRDI("DataLoggerLinked")
                .loadProfileTypes(this.loadProfileType)
                .deviceTypeName(DeviceCreator.DATA_LOGGER_DEVICE_TYPE_NAME)
                .deviceConfigName(DeviceCreator.DATA_LOGGER_DEVICE_CONFIGURATION_NAME)
                .dataLoggerEnabled(true)
                .create(Instant.ofEpochMilli(fromClock.getTime()));

        Device slave = this.slaveDeviceCreator
                .name("slave")
                .mRDI("simplePreStoreWithDataInFutureTest")
                .loadProfileTypes(this.loadProfileType)
                .create(Instant.ofEpochMilli(fromClock.getTime()));

        HashMap<Channel, Channel> channelMap = new HashMap<>();
        // Linking the slave
        LoadProfile dataLoggerLoadProfile = dataLogger.getLoadProfiles().get(0);
        LoadProfile slaveLoggerLoadProfile = slave.getLoadProfiles().get(0);
        slaveLoggerLoadProfile.getChannels().stream().forEach(slaveChannel -> {
            channelMap.put(slaveChannel, dataLoggerLoadProfile.getChannels().get(channelMap.size()));
        });

        getTopologyService().setDataLogger(slave, dataLogger, fromClock.toInstant(), channelMap, new HashMap<>());
        //Assert the linking of the data logger channels with the slave channels
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(0), fromClock.toInstant()).get().getId()).isEqualTo(slaveLoggerLoadProfile.getChannels()
                .get(0)
                .getId());
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(1), fromClock.toInstant()).get().getId()).isEqualTo(slaveLoggerLoadProfile.getChannels()
                .get(1)
                .getId());

        // Collect Data
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(dataLoggerLoadProfile, createMockLoadProfileWithTwoChannelsAndDataInFuture(dataLoggerLoadProfile.getInterval()));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(dataLogger);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(currentTimeStamp);

        assertThat(collectedLoadProfile.getCollectedIntervalData()).overridingErrorMessage("The collected data should contain {0} intervals to start", 6).hasSize(6);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.CompositePreStoredLoadProfile preStoredLoadProfile = (PreStoreLoadProfile.CompositePreStoredLoadProfile) loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(preStoredLoadProfile.getPreStoreResult()).isEqualTo(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK);
        assertThat(preStoredLoadProfile.getIntervalBlocks()).hasSize(2);

        // All data should be 'Prestored' on the slave channel
        assertThat(preStoredLoadProfile.getPreStoredLoadProfiles()).hasSize(1);
        PreStoreLoadProfile.PreStoredLoadProfile singlePreStoredLoadProfile = preStoredLoadProfile.getPreStoredLoadProfiles().get(0);
        assertThat(singlePreStoredLoadProfile.getOfflineLoadProfile().getDeviceId()).isEqualTo(slave.getId());

        assertThat(singlePreStoredLoadProfile.getIntervalBlocks()).hasSize(2);
        assertThat(singlePreStoredLoadProfile.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(slaveLoggerLoadProfile.getChannels().get(0).getReadingType().getMRID());
        assertThat(singlePreStoredLoadProfile.getIntervalBlocks().get(1).getReadingTypeCode()).isEqualTo(slaveLoggerLoadProfile.getChannels().get(1).getReadingType().getMRID());
        assertThat(singlePreStoredLoadProfile.getIntervalBlocks().get(0).getIntervals()).hasSize(4);
        assertThat(singlePreStoredLoadProfile.getIntervalBlocks().get(1).getIntervals()).hasSize(4);
    }

    @Test
    @Transactional
    public void testLinkedDataLoggerAtStartOfPeriod() {
        Device dataLogger = this.deviceCreator
                .name("DataLogger")
                .mRDI("DataLoggerLinked")
                .loadProfileTypes(this.loadProfileType)
                .deviceTypeName(DeviceCreator.DATA_LOGGER_DEVICE_TYPE_NAME)
                .deviceConfigName(DeviceCreator.DATA_LOGGER_DEVICE_CONFIGURATION_NAME)
                .dataLoggerEnabled(true)
                .create(Instant.ofEpochMilli(fromClock.getTime()));

        Device slave = this.slaveDeviceCreator
                .name("slave")
                .mRDI("simplePreStoreWithDataInFutureTest")
                .loadProfileTypes(this.loadProfileType)
                .create(Instant.ofEpochMilli(fromClock.getTime()));

        HashMap<Channel, Channel> channelMap = new HashMap<>();
        // Linking the slave
        LoadProfile dataLoggerLoadProfile = dataLogger.getLoadProfiles().get(0);
        LoadProfile slaveLoadProfile = slave.getLoadProfiles().get(0);
        slaveLoadProfile.getChannels().stream().forEach(slaveChannel -> {
            channelMap.put(slaveChannel, dataLoggerLoadProfile.getChannels().get(channelMap.size()));
        });

        getTopologyService().setDataLogger(slave, dataLogger, fromClock.toInstant(), channelMap, new HashMap<>());
        getTopologyService().clearDataLogger(slave, Instant.ofEpochMilli(intervalEndTime3.getTime()));

        // Assert the linking (and unlinking) of the data logger channels with the slave channels
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(0), fromClock.toInstant()).get().getId()).isEqualTo(slaveLoadProfile.getChannels().get(0).getId());
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(1), fromClock.toInstant()).get().getId()).isEqualTo(slaveLoadProfile.getChannels().get(1).getId());
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(0), intervalEndTime3.toInstant()).isPresent()).isFalse();
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(1), intervalEndTime3.toInstant()).isPresent()).isFalse();

        // Collect Data
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(dataLoggerLoadProfile, createMockLoadProfileWithTwoChannelsAndDataInFuture(dataLoggerLoadProfile.getInterval()));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(dataLogger);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(currentTimeStamp);

        assertThat(collectedLoadProfile.getCollectedIntervalData()).overridingErrorMessage("The collected data should contain {0} intervals to start", 6).hasSize(6);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.CompositePreStoredLoadProfile preStoredLoadProfile = (PreStoreLoadProfile.CompositePreStoredLoadProfile) loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(preStoredLoadProfile.getPreStoreResult()).isEqualTo(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK);
        assertThat(preStoredLoadProfile.getIntervalBlocks()).hasSize(4);

        // Data should be 'prestored' on the slave channel for the start period
        // Data should be 'prestored' on the data logger channel for the remaining period
        assertThat(preStoredLoadProfile.getPreStoredLoadProfiles()).hasSize(2);
        PreStoreLoadProfile.PreStoredLoadProfile slavePreStoredLoadProfile = preStoredLoadProfile.getPreStoredLoadProfiles().get(0);
        assertThat(slavePreStoredLoadProfile.getOfflineLoadProfile().getDeviceId()).isEqualTo(slave.getId());

        assertThat(slavePreStoredLoadProfile.getIntervalBlocks()).hasSize(2);
        assertThat(slavePreStoredLoadProfile.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(slaveLoadProfile.getChannels().get(0).getReadingType().getMRID());
        assertThat(slavePreStoredLoadProfile.getIntervalBlocks().get(1).getReadingTypeCode()).isEqualTo(slaveLoadProfile.getChannels().get(1).getReadingType().getMRID());
        assertThat(slavePreStoredLoadProfile.getIntervalBlocks().get(0).getIntervals()).hasSize(2);
        assertThat(slavePreStoredLoadProfile.getIntervalBlocks().get(1).getIntervals()).hasSize(2);

        PreStoreLoadProfile.PreStoredLoadProfile dataLoggerPreStoredLoadProfile = preStoredLoadProfile.getPreStoredLoadProfiles().get(1);
        assertThat(dataLoggerPreStoredLoadProfile.getOfflineLoadProfile().getDeviceId()).isEqualTo(dataLogger.getId());

        assertThat(dataLoggerPreStoredLoadProfile.getIntervalBlocks()).hasSize(2);
        assertThat(dataLoggerPreStoredLoadProfile.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(slaveLoadProfile.getChannels().get(0).getReadingType().getMRID());
        assertThat(dataLoggerPreStoredLoadProfile.getIntervalBlocks().get(1).getReadingTypeCode()).isEqualTo(slaveLoadProfile.getChannels().get(1).getReadingType().getMRID());
        assertThat(dataLoggerPreStoredLoadProfile.getIntervalBlocks().get(0).getIntervals()).hasSize(2);
        assertThat(dataLoggerPreStoredLoadProfile.getIntervalBlocks().get(1).getIntervals()).hasSize(2);
    }

    @Test
    @Transactional
    public void testLinkedDataLoggerAtEndOfPeriod() {
        Device dataLogger = this.deviceCreator
                .name("DataLogger")
                .mRDI("DataLoggerLinked")
                .loadProfileTypes(this.loadProfileType)
                .deviceTypeName(DeviceCreator.DATA_LOGGER_DEVICE_TYPE_NAME)
                .deviceConfigName(DeviceCreator.DATA_LOGGER_DEVICE_CONFIGURATION_NAME)
                .dataLoggerEnabled(true)
                .create(Instant.ofEpochMilli(fromClock.getTime()));

        Device slave = this.slaveDeviceCreator
                .name("slave")
                .mRDI("simplePreStoreWithDataInFutureTest")
                .loadProfileTypes(this.loadProfileType)
                .create(Instant.ofEpochMilli(fromClock.getTime()));

        HashMap<Channel, Channel> channelMap = new HashMap<>();
        // Linking the slave
        LoadProfile dataLoggerLoadProfile = dataLogger.getLoadProfiles().get(0);
        LoadProfile slaveLoadProfile = slave.getLoadProfiles().get(0);
        slaveLoadProfile.getChannels().stream().forEach(slaveChannel -> {
            channelMap.put(slaveChannel, dataLoggerLoadProfile.getChannels().get(channelMap.size()));
        });

        getTopologyService().setDataLogger(slave, dataLogger, Instant.ofEpochMilli(intervalEndTime3.getTime()), channelMap, new HashMap<>());

        // Assert there is no linking of the data logger channels with the slave channels before intervalEndTime3
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(0), fromClock.toInstant()).isPresent()).isFalse();
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(1), fromClock.toInstant()).isPresent()).isFalse();
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(0), intervalEndTime3.toInstant()).get().getId()).isEqualTo(slaveLoadProfile.getChannels()
                .get(0)
                .getId());
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(1), intervalEndTime3.toInstant()).get().getId()).isEqualTo(slaveLoadProfile.getChannels()
                .get(1)
                .getId());

        // Collect Data
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(dataLoggerLoadProfile, createMockLoadProfileWithTwoChannelsAndDataInFuture(dataLoggerLoadProfile.getInterval()));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(dataLogger);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(currentTimeStamp);

        assertThat(collectedLoadProfile.getCollectedIntervalData()).overridingErrorMessage("The collected data should contain {0} intervals to start", 6).hasSize(6);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.CompositePreStoredLoadProfile preStoredLoadProfile = (PreStoreLoadProfile.CompositePreStoredLoadProfile) loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(preStoredLoadProfile.getPreStoreResult()).isEqualTo(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK);
        assertThat(preStoredLoadProfile.getIntervalBlocks()).hasSize(4);

        // Data should be 'prestored' on the data logger channel for the start period
        // Data should be 'prestored' on the slave channel for the remaining period
        assertThat(preStoredLoadProfile.getPreStoredLoadProfiles()).hasSize(2);
        PreStoreLoadProfile.PreStoredLoadProfile slavePreStoredLoadProfile = preStoredLoadProfile.getPreStoredLoadProfiles().get(0);
        assertThat(slavePreStoredLoadProfile.getOfflineLoadProfile().getDeviceId()).isEqualTo(slave.getId());

        assertThat(slavePreStoredLoadProfile.getIntervalBlocks()).hasSize(2);
        assertThat(slavePreStoredLoadProfile.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(slaveLoadProfile.getChannels().get(0).getReadingType().getMRID());
        assertThat(slavePreStoredLoadProfile.getIntervalBlocks().get(1).getReadingTypeCode()).isEqualTo(slaveLoadProfile.getChannels().get(1).getReadingType().getMRID());
        assertThat(slavePreStoredLoadProfile.getIntervalBlocks().get(0).getIntervals()).hasSize(2);
        assertThat(slavePreStoredLoadProfile.getIntervalBlocks().get(1).getIntervals()).hasSize(2);

        PreStoreLoadProfile.PreStoredLoadProfile dataLoggerPreStoredLoadProfile = preStoredLoadProfile.getPreStoredLoadProfiles().get(1);
        assertThat(dataLoggerPreStoredLoadProfile.getOfflineLoadProfile().getDeviceId()).isEqualTo(dataLogger.getId());

        assertThat(dataLoggerPreStoredLoadProfile.getIntervalBlocks()).hasSize(2);
        assertThat(dataLoggerPreStoredLoadProfile.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(slaveLoadProfile.getChannels().get(0).getReadingType().getMRID());
        assertThat(dataLoggerPreStoredLoadProfile.getIntervalBlocks().get(1).getReadingTypeCode()).isEqualTo(slaveLoadProfile.getChannels().get(1).getReadingType().getMRID());
        assertThat(dataLoggerPreStoredLoadProfile.getIntervalBlocks().get(0).getIntervals()).hasSize(2);
        assertThat(dataLoggerPreStoredLoadProfile.getIntervalBlocks().get(1).getIntervals()).hasSize(2);
    }

    @Test
    @Transactional
    public void testLinkedDataLoggerWithTwoSlavesOverWholePeriod() {
        Device dataLogger = this.deviceCreator
                .name("DataLogger")
                .mRDI("DataLoggerLinked")
                .loadProfileTypes(this.loadProfileType)
                .deviceTypeName(DeviceCreator.DATA_LOGGER_DEVICE_TYPE_NAME)
                .deviceConfigName(DeviceCreator.DATA_LOGGER_DEVICE_CONFIGURATION_NAME)
                .dataLoggerEnabled(true)
                .create(Instant.ofEpochMilli(fromClock.getTime()));

        RegisterType slave1RegisterType = getMasterDataService().findRegisterTypeByReadingType(getMeteringService().getReadingType(getMdcReadingTypeUtilService().getReadingTypeMridFrom(obisCodeActiveImport, kiloWattHours))
                .get()).get();
        RegisterType slave2RegisterType = getMasterDataService().findRegisterTypeByReadingType(getMeteringService().getReadingType(getMdcReadingTypeUtilService().getReadingTypeMridFrom(obisCodeActiveExport, kiloWattHours))
                .get()).get();
        LoadProfileType loadProfileTypeSlave1 = getInjector().getInstance(MasterDataService.class)
                .newLoadProfileType("loadProfileTypeSlave1", ObisCode.fromString("1.0.99.1.0.255"), loadProfileInterval, Collections.singletonList(slave1RegisterType));
        loadProfileTypeSlave1.save();
        LoadProfileType loadProfileTypeSlave2 = getInjector().getInstance(MasterDataService.class)
                .newLoadProfileType("loadProfileTypeSlave2", ObisCode.fromString("1.0.99.1.0.255"), loadProfileInterval, Collections.singletonList(slave2RegisterType));
        loadProfileTypeSlave2.save();

        Device slave1 = this.slaveDeviceCreator
                .name("slave1")
                .mRDI("simplePreStoreWithDataInFutureTest")
                .deviceTypeName("slave1Type")
                .loadProfileTypes(loadProfileTypeSlave1)
                .create(Instant.ofEpochMilli(fromClock.getTime()));

        DeviceCreator slaveDeviceCreator2 = (DeviceCreator) new DeviceCreator(
                getInjector().getInstance(DeviceConfigurationService.class),
                getInjector().getInstance(DeviceService.class)).dataLoggerSlaveDevice();
        Device slave2 = slaveDeviceCreator2
                .name("slave2")
                .mRDI("simplePreStoreWithDataInFutureTest2")
                .deviceTypeName("slave2Type")
                .loadProfileTypes(loadProfileTypeSlave2)
                .create(Instant.ofEpochMilli(fromClock.getTime()));

        // Linking the slave
        LoadProfile dataLoggerLoadProfile = dataLogger.getLoadProfiles().get(0);
        LoadProfile slaveLoadProfile1 = slave1.getLoadProfiles().get(0);
        LoadProfile slaveLoadProfile2 = slave2.getLoadProfiles().get(0);
        HashMap<Channel, Channel> channelMap1 = new HashMap<>();
        channelMap1.put(slaveLoadProfile1.getChannels().get(0), dataLoggerLoadProfile.getChannels().get(0));
        HashMap<Channel, Channel> channelMap2 = new HashMap<>();
        channelMap2.put(slaveLoadProfile2.getChannels().get(0), dataLoggerLoadProfile.getChannels().get(1));

        getTopologyService().setDataLogger(slave1, dataLogger, fromClock.toInstant(), channelMap1, new HashMap<>());
        getTopologyService().setDataLogger(slave2, dataLogger, fromClock.toInstant(), channelMap2, new HashMap<>());

        // Assert That the channels are linked
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(0), fromClock.toInstant()).get().getId()).isEqualTo(slaveLoadProfile1.getChannels().get(0).getId());
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(1), fromClock.toInstant()).get().getId()).isEqualTo(slaveLoadProfile2.getChannels().get(0).getId());

        // Collect Data
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(dataLoggerLoadProfile, createMockLoadProfileWithTwoChannelsAndDataInFuture(dataLoggerLoadProfile.getInterval()));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(dataLogger);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(currentTimeStamp);

        assertThat(collectedLoadProfile.getCollectedIntervalData()).overridingErrorMessage("The collected data should contain {0} intervals to start", 6).hasSize(6);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.CompositePreStoredLoadProfile preStoredLoadProfile = (PreStoreLoadProfile.CompositePreStoredLoadProfile) loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(preStoredLoadProfile.getPreStoreResult()).isEqualTo(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK);
        assertThat(preStoredLoadProfile.getIntervalBlocks()).hasSize(2);

        assertThat(preStoredLoadProfile.getPreStoredLoadProfiles()).hasSize(2);
        PreStoreLoadProfile.PreStoredLoadProfile slavePreStoredLoadProfile1 = preStoredLoadProfile.getPreStoredLoadProfiles().get(0);
        assertThat(slavePreStoredLoadProfile1.getOfflineLoadProfile().getDeviceId()).isEqualTo(slave1.getId());

        assertThat(slavePreStoredLoadProfile1.getIntervalBlocks()).hasSize(1);
        assertThat(slavePreStoredLoadProfile1.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(slaveLoadProfile1.getChannels().get(0).getReadingType().getMRID());
        assertThat(slavePreStoredLoadProfile1.getIntervalBlocks().get(0).getIntervals()).hasSize(4);

        PreStoreLoadProfile.PreStoredLoadProfile slavePreStoredLoadProfile2 = preStoredLoadProfile.getPreStoredLoadProfiles().get(1);
        assertThat(slavePreStoredLoadProfile2.getOfflineLoadProfile().getDeviceId()).isEqualTo(slave2.getId());

        assertThat(slavePreStoredLoadProfile2.getIntervalBlocks()).hasSize(1);
        assertThat(slavePreStoredLoadProfile2.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(slaveLoadProfile2.getChannels().get(0).getReadingType().getMRID());
        assertThat(slavePreStoredLoadProfile2.getIntervalBlocks().get(0).getIntervals()).hasSize(4);
    }

    @Test
    @Transactional
    public void testLinkedDataLoggerWithTwoSlavesButFirstOnlyAtStart() {
        Device dataLogger = this.deviceCreator
                .name("DataLogger")
                .mRDI("DataLoggerLinked")
                .loadProfileTypes(this.loadProfileType)
                .deviceTypeName(DeviceCreator.DATA_LOGGER_DEVICE_TYPE_NAME)
                .deviceConfigName(DeviceCreator.DATA_LOGGER_DEVICE_CONFIGURATION_NAME)
                .dataLoggerEnabled(true)
                .create(Instant.ofEpochMilli(fromClock.getTime()));

        RegisterType slave1RegisterType = getMasterDataService().findRegisterTypeByReadingType(getMeteringService().getReadingType(getMdcReadingTypeUtilService().getReadingTypeMridFrom(obisCodeActiveImport, kiloWattHours))
                .get()).get();
        RegisterType slave2RegisterType = getMasterDataService().findRegisterTypeByReadingType(getMeteringService().getReadingType(getMdcReadingTypeUtilService().getReadingTypeMridFrom(obisCodeActiveExport, kiloWattHours))
                .get()).get();
        LoadProfileType loadProfileTypeSlave1 = getInjector().getInstance(MasterDataService.class)
                .newLoadProfileType("loadProfileTypeSlave1", ObisCode.fromString("1.0.99.1.0.255"), loadProfileInterval, Collections.singletonList(slave1RegisterType));
        loadProfileTypeSlave1.save();
        LoadProfileType loadProfileTypeSlave2 = getInjector().getInstance(MasterDataService.class)
                .newLoadProfileType("loadProfileTypeSlave2", ObisCode.fromString("1.0.99.1.0.255"), loadProfileInterval, Collections.singletonList(slave2RegisterType));
        loadProfileTypeSlave2.save();

        Device slave1 = this.slaveDeviceCreator
                .name("slave1")
                .mRDI("simplePreStoreWithDataInFutureTest")
                .deviceTypeName("slave1Type")
                .loadProfileTypes(loadProfileTypeSlave1)
                .create(Instant.ofEpochMilli(fromClock.getTime()));

        DeviceCreator slaveDeviceCreator2 = (DeviceCreator) new DeviceCreator(
                getInjector().getInstance(DeviceConfigurationService.class),
                getInjector().getInstance(DeviceService.class)).dataLoggerSlaveDevice();
        Device slave2 = slaveDeviceCreator2
                .name("slave2")
                .mRDI("simplePreStoreWithDataInFutureTest2")
                .deviceTypeName("slave2Type")
                .loadProfileTypes(loadProfileTypeSlave2)
                .create(Instant.ofEpochMilli(fromClock.getTime()));

        // Linking the slave
        LoadProfile dataLoggerLoadProfile = dataLogger.getLoadProfiles().get(0);
        LoadProfile slaveLoadProfile1 = slave1.getLoadProfiles().get(0);
        LoadProfile slaveLoadProfile2 = slave2.getLoadProfiles().get(0);
        HashMap<Channel, Channel> channelMap1 = new HashMap<>();
        channelMap1.put(slaveLoadProfile1.getChannels().get(0), dataLoggerLoadProfile.getChannels().get(0));
        HashMap<Channel, Channel> channelMap2 = new HashMap<>();
        channelMap2.put(slaveLoadProfile2.getChannels().get(0), dataLoggerLoadProfile.getChannels().get(1));

        getTopologyService().setDataLogger(slave1, dataLogger, fromClock.toInstant(), channelMap1, new HashMap<>());
        getTopologyService().setDataLogger(slave2, dataLogger, fromClock.toInstant(), channelMap2, new HashMap<>());
        getTopologyService().clearDataLogger(slave1, Instant.ofEpochMilli(intervalEndTime3.getTime()));

        // Assert the linking/unlinking of the data logger channels
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(0), fromClock.toInstant()).get().getId()).isEqualTo(slaveLoadProfile1.getChannels().get(0).getId());
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(1), fromClock.toInstant()).get().getId()).isEqualTo(slaveLoadProfile2.getChannels().get(0).getId());
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(0), intervalEndTime3.toInstant()).isPresent()).isFalse();

        // Collect Data
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(dataLoggerLoadProfile, createMockLoadProfileWithTwoChannelsAndDataInFuture(dataLoggerLoadProfile.getInterval()));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(dataLogger);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(currentTimeStamp);

        assertThat(collectedLoadProfile.getCollectedIntervalData()).overridingErrorMessage("The collected data should contain {0} intervals to start", 6).hasSize(6);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.CompositePreStoredLoadProfile preStoredLoadProfile = (PreStoreLoadProfile.CompositePreStoredLoadProfile) loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(preStoredLoadProfile.getPreStoreResult()).isEqualTo(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK);
        assertThat(preStoredLoadProfile.getIntervalBlocks()).hasSize(3);

        assertThat(preStoredLoadProfile.getPreStoredLoadProfiles()).hasSize(3);
        PreStoreLoadProfile.PreStoredLoadProfile slavePreStoredLoadProfile1 = preStoredLoadProfile.getPreStoredLoadProfiles().get(0);
        assertThat(slavePreStoredLoadProfile1.getOfflineLoadProfile().getDeviceId()).isEqualTo(slave1.getId());

        assertThat(slavePreStoredLoadProfile1.getIntervalBlocks()).hasSize(1);
        assertThat(slavePreStoredLoadProfile1.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(slaveLoadProfile1.getChannels().get(0).getReadingType().getMRID());
        assertThat(slavePreStoredLoadProfile1.getIntervalBlocks().get(0).getIntervals()).hasSize(2);

        PreStoreLoadProfile.PreStoredLoadProfile dataLoggerUnlinkedPeriod = preStoredLoadProfile.getPreStoredLoadProfiles().get(1);
        assertThat(dataLoggerUnlinkedPeriod.getOfflineLoadProfile().getDeviceId()).isEqualTo(dataLogger.getId());

        assertThat(dataLoggerUnlinkedPeriod.getIntervalBlocks()).hasSize(1);
        assertThat(dataLoggerUnlinkedPeriod.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(dataLogger.getChannels().get(0).getReadingType().getMRID());
        assertThat(dataLoggerUnlinkedPeriod.getIntervalBlocks().get(0).getIntervals()).hasSize(2);

        PreStoreLoadProfile.PreStoredLoadProfile slavePreStoredLoadProfile2 = preStoredLoadProfile.getPreStoredLoadProfiles().get(2);
        assertThat(slavePreStoredLoadProfile2.getOfflineLoadProfile().getDeviceId()).isEqualTo(slave2.getId());

        assertThat(slavePreStoredLoadProfile2.getIntervalBlocks()).hasSize(1);
        assertThat(slavePreStoredLoadProfile2.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(slaveLoadProfile2.getChannels().get(0).getReadingType().getMRID());
        assertThat(slavePreStoredLoadProfile2.getIntervalBlocks().get(0).getIntervals()).hasSize(4);
    }

    @Test
    @Transactional
    public void testLinkedDataLoggerWithTwoSlavesButFirstOnlyAtEnd() {
        Device dataLogger = this.deviceCreator
                .name("DataLogger")
                .mRDI("DataLoggerLinked")
                .loadProfileTypes(this.loadProfileType)
                .deviceTypeName(DeviceCreator.DATA_LOGGER_DEVICE_TYPE_NAME)
                .deviceConfigName(DeviceCreator.DATA_LOGGER_DEVICE_CONFIGURATION_NAME)
                .dataLoggerEnabled(true)
                .create(Instant.ofEpochMilli(fromClock.getTime()));

        RegisterType slave1RegisterType = getMasterDataService().findRegisterTypeByReadingType(getMeteringService().getReadingType(getMdcReadingTypeUtilService().getReadingTypeMridFrom(obisCodeActiveImport, kiloWattHours))
                .get()).get();
        RegisterType slave2RegisterType = getMasterDataService().findRegisterTypeByReadingType(getMeteringService().getReadingType(getMdcReadingTypeUtilService().getReadingTypeMridFrom(obisCodeActiveExport, kiloWattHours))
                .get()).get();
        LoadProfileType loadProfileTypeSlave1 = getInjector().getInstance(MasterDataService.class)
                .newLoadProfileType("loadProfileTypeSlave1", ObisCode.fromString("1.0.99.1.0.255"), loadProfileInterval, Collections.singletonList(slave1RegisterType));
        loadProfileTypeSlave1.save();
        LoadProfileType loadProfileTypeSlave2 = getInjector().getInstance(MasterDataService.class)
                .newLoadProfileType("loadProfileTypeSlave2", ObisCode.fromString("1.0.99.1.0.255"), loadProfileInterval, Collections.singletonList(slave2RegisterType));
        loadProfileTypeSlave2.save();

        Device slave1 = this.slaveDeviceCreator
                .name("slave1")
                .mRDI("simplePreStoreWithDataInFutureTest")
                .deviceTypeName("slave1Type")
                .loadProfileTypes(loadProfileTypeSlave1)
                .create(Instant.ofEpochMilli(fromClock.getTime()));

        DeviceCreator slaveDeviceCreator2 = (DeviceCreator) new DeviceCreator(
                getInjector().getInstance(DeviceConfigurationService.class),
                getInjector().getInstance(DeviceService.class)).dataLoggerSlaveDevice();
        Device slave2 = slaveDeviceCreator2
                .name("slave2")
                .mRDI("simplePreStoreWithDataInFutureTest2")
                .deviceTypeName("slave2Type")
                .loadProfileTypes(loadProfileTypeSlave2)
                .create(Instant.ofEpochMilli(fromClock.getTime()));

        // Linking the slave
        LoadProfile dataLoggerLoadProfile = dataLogger.getLoadProfiles().get(0);
        LoadProfile slaveLoadProfile1 = slave1.getLoadProfiles().get(0);
        LoadProfile slaveLoadProfile2 = slave2.getLoadProfiles().get(0);
        HashMap<Channel, Channel> channelMap1 = new HashMap<>();
        channelMap1.put(slaveLoadProfile1.getChannels().get(0), dataLoggerLoadProfile.getChannels().get(0));
        HashMap<Channel, Channel> channelMap2 = new HashMap<>();
        channelMap2.put(slaveLoadProfile2.getChannels().get(0), dataLoggerLoadProfile.getChannels().get(1));

        getTopologyService().setDataLogger(slave1, dataLogger, intervalEndTime3.toInstant(), channelMap1, new HashMap<>());
        getTopologyService().setDataLogger(slave2, dataLogger, fromClock.toInstant(), channelMap2, new HashMap<>());

        // Assert the linking/unlinking of the data logger channels
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(0), fromClock.toInstant()).isPresent()).isFalse();
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(0), intervalEndTime3.toInstant()).get().getId()).isEqualTo(slaveLoadProfile1.getChannels()
                .get(0)
                .getId());
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(1), fromClock.toInstant()).get().getId()).isEqualTo(slaveLoadProfile2.getChannels().get(0).getId());

        // Collect Data
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(dataLoggerLoadProfile, createMockLoadProfileWithTwoChannelsAndDataInFuture(dataLoggerLoadProfile.getInterval()));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(dataLogger);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(currentTimeStamp);

        assertThat(collectedLoadProfile.getCollectedIntervalData()).overridingErrorMessage("The collected data should contain {0} intervals to start", 6).hasSize(6);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.CompositePreStoredLoadProfile preStoredLoadProfile = (PreStoreLoadProfile.CompositePreStoredLoadProfile) loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(preStoredLoadProfile.getPreStoreResult()).isEqualTo(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK);
        assertThat(preStoredLoadProfile.getIntervalBlocks()).hasSize(3);

        assertThat(preStoredLoadProfile.getPreStoredLoadProfiles()).hasSize(3);
        PreStoreLoadProfile.PreStoredLoadProfile slavePreStoredLoadProfile1 = preStoredLoadProfile.getPreStoredLoadProfiles().get(0);
        assertThat(slavePreStoredLoadProfile1.getOfflineLoadProfile().getDeviceId()).isEqualTo(slave1.getId());

        assertThat(slavePreStoredLoadProfile1.getIntervalBlocks()).hasSize(1);
        assertThat(slavePreStoredLoadProfile1.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(slaveLoadProfile1.getChannels().get(0).getReadingType().getMRID());
        assertThat(slavePreStoredLoadProfile1.getIntervalBlocks().get(0).getIntervals()).hasSize(2);

        PreStoreLoadProfile.PreStoredLoadProfile dataLoggerUnlinkedPeriod = preStoredLoadProfile.getPreStoredLoadProfiles().get(1);
        assertThat(dataLoggerUnlinkedPeriod.getOfflineLoadProfile().getDeviceId()).isEqualTo(dataLogger.getId());

        assertThat(dataLoggerUnlinkedPeriod.getIntervalBlocks()).hasSize(1);
        assertThat(dataLoggerUnlinkedPeriod.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(dataLogger.getChannels().get(0).getReadingType().getMRID());
        assertThat(dataLoggerUnlinkedPeriod.getIntervalBlocks().get(0).getIntervals()).hasSize(2);

        PreStoreLoadProfile.PreStoredLoadProfile slavePreStoredLoadProfile2 = preStoredLoadProfile.getPreStoredLoadProfiles().get(2);
        assertThat(slavePreStoredLoadProfile2.getOfflineLoadProfile().getDeviceId()).isEqualTo(slave2.getId());

        assertThat(slavePreStoredLoadProfile2.getIntervalBlocks()).hasSize(1);
        assertThat(slavePreStoredLoadProfile2.getIntervalBlocks().get(0).getReadingTypeCode()).isEqualTo(slaveLoadProfile2.getChannels().get(0).getReadingType().getMRID());
        assertThat(slavePreStoredLoadProfile2.getIntervalBlocks().get(0).getIntervals()).hasSize(4);
    }

    protected ComServerDAO mockComServerDAOWithOfflineLoadProfile(OfflineLoadProfile offlineLoadProfile) {
        ComServerDAO comServerDAO = spy(new ComServerDAOImpl(this.serviceProvider, comServerUser));
        doCallRealMethod().when(comServerDAO).storeMeterReadings(any(DeviceIdentifier.class), any(MeterReading.class));
        doAnswer(invocation -> ((Transaction<?>) invocation.getArguments()[0]).perform()).when(comServerDAO).executeTransaction(any());
        doReturn(Optional.of(offlineLoadProfile)).when(comServerDAO).findOfflineLoadProfile(any(LoadProfileIdentifier.class));
        DeviceIdentifier deviceIdentifier = offlineLoadProfile.getDeviceIdentifier();
        doReturn(deviceIdentifier).when(comServerDAO).getDeviceIdentifierFor(any(LoadProfileIdentifier.class));
        doCallRealMethod().when(comServerDAO).updateLastReadingFor(any(LoadProfileIdentifier.class), any(Instant.class));
        return comServerDAO;
    }

    CollectedLoadProfile createCollectedLoadProfile(LoadProfile loadProfile) {
        return enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithTwoChannels(loadProfile.getInterval()));
    }

    CollectedLoadProfile createCollectedLoadProfileWithDeltaData(LoadProfile loadProfile) {
        return enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithTwoDeltaChannels(loadProfile.getInterval()));
    }

    CollectedLoadProfile createMockLoadProfileWithTwoChannels(TimeDuration interval) {
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class, RETURNS_DEEP_STUBS);
        List<ChannelInfo> mockedChannelInfos = createMockedChannelInfos(interval);
        when(collectedLoadProfile.getChannelInfo()).thenReturn(mockedChannelInfos);
        List<IntervalData> mockedCollectedIntervalData = createMockedIntervalData();
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(mockedCollectedIntervalData);
        return collectedLoadProfile;
    }

    CollectedLoadProfile createMockLoadProfileWithOverflowData(TimeDuration interval) {
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class, RETURNS_DEEP_STUBS);
        List<ChannelInfo> mockedChannelInfos = createMockedChannelInfos(interval);
        when(collectedLoadProfile.getChannelInfo()).thenReturn(mockedChannelInfos);
        List<IntervalData> mockedCollectedIntervalData = createMockedIntervalDataThatOverflowsOnThirdValue();
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(mockedCollectedIntervalData);
        return collectedLoadProfile;
    }

    CollectedLoadProfile createMockLoadProfileWithOverflowDataAfterPositiveScaling(TimeDuration interval) {
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class, RETURNS_DEEP_STUBS);
        List<ChannelInfo> mockedChannelInfos = createMockedChannelInfosWithPositiveThousandScaler(interval);
        when(collectedLoadProfile.getChannelInfo()).thenReturn(mockedChannelInfos);
        List<IntervalData> mockedCollectedIntervalData = createMockedIntervalDataThatOverflowsOnThirdValueAfterUpScaling();
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(mockedCollectedIntervalData);
        return collectedLoadProfile;
    }

    CollectedLoadProfile createMockLoadProfileWithOverflowDataAfterNegativeScaling(TimeDuration interval) {
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class, RETURNS_DEEP_STUBS);
        List<ChannelInfo> mockedChannelInfos = createMockedChannelInfosWithNegativeThousandScaler(interval);
        when(collectedLoadProfile.getChannelInfo()).thenReturn(mockedChannelInfos);
        List<IntervalData> mockedCollectedIntervalData = createMockedIntervalDataThatOverflowsOnThirdValueAfterDownScaling();
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(mockedCollectedIntervalData);
        return collectedLoadProfile;
    }

    CollectedLoadProfile createMockLoadProfileWithMultiplier(TimeDuration interval, BigDecimal multiplier) {
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class, RETURNS_DEEP_STUBS);
        List<ChannelInfo> mockedChannelInfos = createMockedChannelInfosWithMultiplier(interval, multiplier);
        when(collectedLoadProfile.getChannelInfo()).thenReturn(mockedChannelInfos);
        List<IntervalData> mockedCollectedIntervalData = createMockedIntervalData();
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(mockedCollectedIntervalData);
        return collectedLoadProfile;
    }

    CollectedLoadProfile createMockLoadProfileWithPositiveScaler(TimeDuration interval) {
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class, RETURNS_DEEP_STUBS);
        List<ChannelInfo> mockedChannelInfos = createMockedChannelInfosWithPositiveThousandScaler(interval);
        when(collectedLoadProfile.getChannelInfo()).thenReturn(mockedChannelInfos);
        List<IntervalData> mockedCollectedIntervalData = createMockedIntervalData();
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(mockedCollectedIntervalData);
        return collectedLoadProfile;
    }

    CollectedLoadProfile createMockLoadProfileWithNegativeScaler(TimeDuration interval) {
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class, RETURNS_DEEP_STUBS);
        List<ChannelInfo> mockedChannelInfos = createMockedChannelInfosWithNegativeThousandScaler(interval);
        when(collectedLoadProfile.getChannelInfo()).thenReturn(mockedChannelInfos);
        List<IntervalData> mockedCollectedIntervalData = createMockedIntervalData();
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(mockedCollectedIntervalData);
        return collectedLoadProfile;
    }

    CollectedLoadProfile createMockLoadProfileWithTwoChannelsAndDataInFuture(TimeDuration interval) {
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class, RETURNS_DEEP_STUBS);
        List<ChannelInfo> mockedChannelInfos = createMockedChannelInfos(interval);
        when(collectedLoadProfile.getChannelInfo()).thenReturn(mockedChannelInfos);
        List<IntervalData> mockedCollectedIntervalData = createMockedIntervalDataWithTwoEntriesInFuture();
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(mockedCollectedIntervalData);
        when(collectedLoadProfile.getCollectedIntervalDataRange()).thenReturn(Range.closedOpen(Instant.ofEpochMilli(intervalEndTime1.getTime()), Instant.ofEpochMilli(futureIntervalEndTime2.getTime())));
        return collectedLoadProfile;
    }

    CollectedLoadProfile createMockLoadProfileWithTwoDeltaChannels(TimeDuration interval) {
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class);
        List<ChannelInfo> mockedChannelInfos = createMockedDeltaChannelInfos(interval);
        when(collectedLoadProfile.getChannelInfo()).thenReturn(mockedChannelInfos);
        List<IntervalData> mockedCollectedIntervalData = createMockedIntervalData();
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(mockedCollectedIntervalData);
        return collectedLoadProfile;
    }

    List<ChannelInfo> createMockedChannelInfos(TimeDuration interval) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        ObisCode channelObisCodeOne = obisCodeActiveImport;
        channelInfos.add(ChannelInfo.ChannelInfoBuilder.fromObisCode(channelObisCodeOne).meterIdentifier(DEVICE_NAME).unit(kiloWattHours)
                .readingTypeMRID(getMdcReadingTypeUtilService().getReadingTypeFrom(channelObisCodeOne, kiloWattHours, interval)).build());
        ObisCode channelObisCodeTwo = obisCodeActiveExport;
        channelInfos.add(ChannelInfo.ChannelInfoBuilder.fromObisCode(channelObisCodeTwo).meterIdentifier(DEVICE_NAME).unit(kiloWattHours)
                .readingTypeMRID(getMdcReadingTypeUtilService().getReadingTypeFrom(channelObisCodeTwo, kiloWattHours, interval)).build());
        return channelInfos;
    }

    private List<ChannelInfo> createMockedChannelInfosWithMultiplier(TimeDuration interval, BigDecimal multiplier) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        ObisCode channelObisCodeOne = obisCodeActiveImport;
        channelInfos.add(
                ChannelInfo.ChannelInfoBuilder
                        .fromObisCode(channelObisCodeOne)
                        .multiplier(multiplier)
                        .meterIdentifier(DEVICE_NAME)
                        .unit(kiloWattHours)
                        .readingTypeMRID(getMdcReadingTypeUtilService().getReadingTypeFrom(channelObisCodeOne, wattHours, interval))
                        .build());
        ObisCode channelObisCodeTwo = obisCodeActiveExport;
        channelInfos.add(
                ChannelInfo.ChannelInfoBuilder
                        .fromObisCode(channelObisCodeTwo)
                        .multiplier(multiplier)
                        .meterIdentifier(DEVICE_NAME)
                        .unit(kiloWattHours)
                        .readingTypeMRID(getMdcReadingTypeUtilService().getReadingTypeFrom(channelObisCodeTwo, wattHours, interval))
                        .build());
        return channelInfos;
    }

    private List<ChannelInfo> createMockedChannelInfosWithPositiveThousandScaler(TimeDuration interval) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        ObisCode channelObisCodeOne = obisCodeActiveImport;
        channelInfos.add(ChannelInfo.ChannelInfoBuilder.fromObisCode(channelObisCodeOne).meterIdentifier(DEVICE_NAME).unit(kiloWattHours)
                .readingTypeMRID(getMdcReadingTypeUtilService().getReadingTypeFrom(channelObisCodeOne, wattHours, interval)).build());
        ObisCode channelObisCodeTwo = obisCodeActiveExport;
        channelInfos.add(ChannelInfo.ChannelInfoBuilder.fromObisCode(channelObisCodeTwo).meterIdentifier(DEVICE_NAME).unit(kiloWattHours)
                .readingTypeMRID(getMdcReadingTypeUtilService().getReadingTypeFrom(channelObisCodeTwo, wattHours, interval)).build());
        return channelInfos;
    }

    private List<ChannelInfo> createMockedChannelInfosWithNegativeThousandScaler(TimeDuration interval) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        ObisCode channelObisCodeOne = obisCodeActiveImport;
        channelInfos.add(ChannelInfo.ChannelInfoBuilder.fromObisCode(channelObisCodeOne).meterIdentifier(DEVICE_NAME).unit(wattHours)
                .readingTypeMRID(getMdcReadingTypeUtilService().getReadingTypeFrom(channelObisCodeOne, kiloWattHours, interval)).build());
        ObisCode channelObisCodeTwo = obisCodeActiveExport;
        channelInfos.add(ChannelInfo.ChannelInfoBuilder.fromObisCode(channelObisCodeTwo).meterIdentifier(DEVICE_NAME).unit(wattHours)
                .readingTypeMRID(getMdcReadingTypeUtilService().getReadingTypeFrom(channelObisCodeTwo, kiloWattHours, interval)).build());
        return channelInfos;
    }

    private List<ChannelInfo> createMockedDeltaChannelInfos(TimeDuration interval) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        String deltaChannelObisCodeOne = "1.0.1.6.0.255";
        ObisCode obisCodeOne = ObisCode.fromString(deltaChannelObisCodeOne);
        channelInfos.add(ChannelInfo.ChannelInfoBuilder.fromObisCode(obisCodeOne).meterIdentifier(DEVICE_NAME).unit(kiloWattHours)
                .readingTypeMRID(getMdcReadingTypeUtilService().getReadingTypeFrom(obisCodeOne, kiloWattHours, interval)).build());
        String deltaChannelObisCodeTwo = "1.0.2.6.0.255";
        ObisCode obisCodeTwo = ObisCode.fromString(deltaChannelObisCodeTwo);
        channelInfos.add(ChannelInfo.ChannelInfoBuilder.fromObisCode(obisCodeTwo).meterIdentifier(DEVICE_NAME).unit(kiloWattHours)
                .readingTypeMRID(getMdcReadingTypeUtilService().getReadingTypeFrom(obisCodeTwo, kiloWattHours, interval)).build());
        return channelInfos;
    }

    private List<IntervalData> createMockedIntervalData() {
        List<IntervalData> intervalDatas = new ArrayList<>();
        intervalDatas.add(new IntervalData(intervalEndTime1, new HashSet<>(), 0, 0, getIntervalValues(0)));
        intervalDatas.add(new IntervalData(intervalEndTime2, new HashSet<>(), 0, 0, getIntervalValues(1)));
        intervalDatas.add(new IntervalData(intervalEndTime3, new HashSet<>(), 0, 0, getIntervalValues(2)));
        intervalDatas.add(new IntervalData(intervalEndTime4, new HashSet<>(), 0, 0, getIntervalValues(3)));
        return intervalDatas;
    }

    private List<IntervalData> createMockedIntervalDataThatOverflowsOnThirdValue() {
        List<IntervalData> intervalDatas = new ArrayList<>();
        intervalDatas.add(new IntervalData(intervalEndTime1, new HashSet<>(), 0, 0, getIntervalValuesFor(DeviceCreator.CHANNEL_OVERFLOW_VALUE - 1, 10)));
        intervalDatas.add(new IntervalData(intervalEndTime2, new HashSet<>(), 0, 0, getIntervalValuesFor(DeviceCreator.CHANNEL_OVERFLOW_VALUE, 132)));
        intervalDatas.add(new IntervalData(intervalEndTime3, new HashSet<>(), 0, 0, getIntervalValuesFor(DeviceCreator.CHANNEL_OVERFLOW_VALUE + 1, 1651)));
        intervalDatas.add(new IntervalData(intervalEndTime4, new HashSet<>(), 0, 0, getIntervalValuesFor(DeviceCreator.CHANNEL_OVERFLOW_VALUE + 2, 865461)));
        return intervalDatas;
    }

    private List<IntervalData> createMockedIntervalDataThatOverflowsOnThirdValueAfterUpScaling() {
        List<IntervalData> intervalDatas = new ArrayList<>();
        intervalDatas.add(new IntervalData(intervalEndTime1, new HashSet<>(), 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE - 1)
                .divide(BigDecimal.valueOf(1000)), 10)));
        intervalDatas.add(new IntervalData(intervalEndTime2, new HashSet<>(), 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE)
                .divide(BigDecimal.valueOf(1000)), 132)));
        intervalDatas.add(new IntervalData(intervalEndTime3, new HashSet<>(), 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE + 1)
                .divide(BigDecimal.valueOf(1000)), 165)));
        intervalDatas.add(new IntervalData(intervalEndTime4, new HashSet<>(), 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE + 2)
                .divide(BigDecimal.valueOf(1000)), 865)));
        return intervalDatas;
    }

    private List<IntervalData> createMockedIntervalDataThatOverflowsOnThirdValueAfterDownScaling() {
        List<IntervalData> intervalDatas = new ArrayList<>();
        intervalDatas.add(new IntervalData(intervalEndTime1, new HashSet<>(), 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE - 1)
                .multiply(BigDecimal.valueOf(1000)), 10)));
        intervalDatas.add(new IntervalData(intervalEndTime2, new HashSet<>(), 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE)
                .multiply(BigDecimal.valueOf(1000)), 132)));
        intervalDatas.add(new IntervalData(intervalEndTime3, new HashSet<>(), 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE + 1)
                .multiply(BigDecimal.valueOf(1000)), 165)));
        intervalDatas.add(new IntervalData(intervalEndTime4, new HashSet<>(), 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE + 2)
                .multiply(BigDecimal.valueOf(1000)), 865)));
        return intervalDatas;
    }

    public List<IntervalValue> getIntervalValuesFor(Number intervalValueOne, Number intervalValueTwo) {
        List<IntervalValue> intervalValues = new ArrayList<>();
        intervalValues.add(new IntervalValue(intervalValueOne, 0, new HashSet<>()));
        intervalValues.add(new IntervalValue(intervalValueTwo, 0, new HashSet<>()));
        return intervalValues;
    }

    private List<IntervalData> createMockedIntervalDataWithTwoEntriesInFuture() {
        List<IntervalData> intervalDatas = new ArrayList<>();
        intervalDatas.add(new IntervalData(intervalEndTime1, new HashSet<>(), 0, 0, getIntervalValues(0)));
        intervalDatas.add(new IntervalData(intervalEndTime2, new HashSet<>(), 0, 0, getIntervalValues(1)));
        intervalDatas.add(new IntervalData(intervalEndTime3, new HashSet<>(), 0, 0, getIntervalValues(2)));
        intervalDatas.add(new IntervalData(intervalEndTime4, new HashSet<>(), 0, 0, getIntervalValues(3)));
        intervalDatas.add(new IntervalData(futureIntervalEndTime1, new HashSet<>(), 0, 0, getIntervalValues(4)));
        intervalDatas.add(new IntervalData(futureIntervalEndTime2, new HashSet<>(), 0, 0, getIntervalValues(5)));
        return intervalDatas;
    }

    CollectedLoadProfile enhanceCollectedLoadProfile(LoadProfile loadProfile, CollectedLoadProfile collectedLoadProfile) {
        LoadProfileIdentifier loadProfileIdentifier = mock(LoadProfileIdentifier.class);
        when(loadProfileIdentifier.getLoadProfile()).thenReturn(loadProfile);
        when(collectedLoadProfile.getLoadProfileIdentifier()).thenReturn(loadProfileIdentifier);
        when(loadProfileIdentifier.getDeviceIdentifier()).thenReturn(new DeviceIdentifierForAlreadyKnownDeviceByMrID(loadProfile.getDevice()));
        return collectedLoadProfile;
    }

}