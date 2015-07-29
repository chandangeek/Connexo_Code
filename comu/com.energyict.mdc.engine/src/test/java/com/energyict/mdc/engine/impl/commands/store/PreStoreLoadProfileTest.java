package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.ObisCode;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierForAlreadyKnownDeviceByMrID;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.DeviceCreator;
import com.energyict.mdc.engine.impl.commands.offline.OfflineLoadProfileImpl;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import com.energyict.mdc.protocol.api.services.IdentificationService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the store functionality of the {@link CollectedLoadProfileDeviceCommand}
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

    Date verificationTimeStamp = new DateTime(2015, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
    Date currentTimeStamp = new DateTime(2014, 1, 13, 10, 0, 0, 0, DateTimeZone.UTC).toDate();

    Date fromClock = new DateTime(2013, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
    Date intervalEndTime1 = new DateTime(2014, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
    Date intervalEndTime2 = new DateTime(2014, 1, 1, 0, 15, 0, 0, DateTimeZone.UTC).toDate();
    Date intervalEndTime3 = new DateTime(2014, 1, 1, 0, 30, 0, 0, DateTimeZone.UTC).toDate();
    Date intervalEndTime4 = new DateTime(2014, 1, 1, 0, 45, 0, 0, DateTimeZone.UTC).toDate();

    Date futureIntervalEndTime1 = new DateTime(2014, 2, 2, 10, 45, 0, 0, DateTimeZone.UTC).toDate();
    Date futureIntervalEndTime2 = new DateTime(2014, 2, 2, 11, 0, 0, 0, DateTimeZone.UTC).toDate();

    DeviceCreator deviceCreator;
    LoadProfileType loadProfileType;
    @Mock
    private IdentificationService identificationService;

    public List<IntervalValue> getIntervalValues(int addition) {
        List<IntervalValue> intervalValues = new ArrayList<>();
        intervalValues.add(new IntervalValue(intervalValueOne + addition, 0, 0));
        intervalValues.add(new IntervalValue(intervalValueTwo + addition, 0, 0));
        return intervalValues;
    }

    @Before
    public void setUp() {
        this.deviceCreator = new DeviceCreator(
                getInjector().getInstance(DeviceConfigurationService.class),
                getInjector().getInstance(DeviceService.class)
        );
        this.loadProfileType = createLoadProfileType();
        when(this.identificationService.createDeviceIdentifierForAlreadyKnownDevice(any())).thenAnswer(invocationOnMock -> new DeviceIdentifierForAlreadyKnownDeviceByMrID((Device) invocationOnMock.getArguments()[0]));
    }

    private LoadProfileType createLoadProfileType() {
        RegisterType registerType = getMasterDataService().findRegisterTypeByReadingType(getMeteringService().getReadingType(getMdcReadingTypeUtilService().getReadingTypeMridFrom(obisCodeActiveImport, kiloWattHours)).get()).get();
        RegisterType registerType1 = getMasterDataService().findRegisterTypeByReadingType(getMeteringService().getReadingType(getMdcReadingTypeUtilService().getReadingTypeMridFrom(obisCodeActiveExport, kiloWattHours)).get()).get();
        LoadProfileType loadProfileType = getInjector().getInstance(MasterDataService.class).newLoadProfileType("MyLoadProfileType", ObisCode.fromString("1.0.99.1.0.255"), TimeDuration.minutes(15), Arrays.asList(registerType, registerType1));
        loadProfileType.save();
        return loadProfileType;
    }

    @Test
    @Transactional
    public void simplePreStoreWithDataInFutureTest() {

        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("simplePreStoreWithDataInFutureTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithTwoChannelsAndDataInFuture(loadProfile.getInterval()));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(currentTimeStamp);

        assertThat(collectedLoadProfile.getCollectedIntervalData()).overridingErrorMessage("The collected data should contain {0} intervals to start", 6).hasSize(6);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        Pair<DeviceIdentifier<Device>, PreStoreLoadProfile.LocalLoadProfile> localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(localLoadProfile.getLast().getIntervalBlocks().get(0).getIntervals()).hasSize(4);
    }

    @Test
    @Transactional
    public void preStoreWithPositiveUnitConversionTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithPositiveUnitConversionTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithPositiveScaler(loadProfile.getInterval()));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(currentTimeStamp);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        Pair<DeviceIdentifier<Device>, PreStoreLoadProfile.LocalLoadProfile> localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        for (int i = 0; i < collectedLoadProfile.getCollectedIntervalData().size(); i++) {
            IntervalData intervalData = collectedLoadProfile.getCollectedIntervalData().get(i);
            for (int j = 0; j < intervalData.getIntervalValues().size(); j++) {
                IntervalValue intervalValue = intervalData.getIntervalValues().get(j);
                IntervalReading intervalReading = localLoadProfile.getLast().getIntervalBlocks().get(j).getIntervals().get(i);
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
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithNegativeUnitConversionTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithNegativeScaler(loadProfile.getInterval()));

        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);
        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(currentTimeStamp);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        Pair<DeviceIdentifier<Device>, PreStoreLoadProfile.LocalLoadProfile> localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        for (int i = 0; i < collectedLoadProfile.getCollectedIntervalData().size(); i++) {
            IntervalData intervalData = collectedLoadProfile.getCollectedIntervalData().get(i);
            for (int j = 0; j < intervalData.getIntervalValues().size(); j++) {
                IntervalValue intervalValue = intervalData.getIntervalValues().get(j);
                IntervalReading intervalReading = localLoadProfile.getLast().getIntervalBlocks().get(j).getIntervals().get(i);
                assertThat(new BigDecimal(intervalValue.getNumber().toString()).divide(BigDecimal.valueOf(1000)).compareTo(intervalReading.getValue()))
                        .overridingErrorMessage("Values are not the same -> %s and %s",
                                new BigDecimal(intervalValue.getNumber().toString()).divide(BigDecimal.valueOf(1000)),
                                intervalReading.getValue())
                        .isEqualTo(0);
            }
        }
    }

    @Test
    @Transactional
    public void preStoreWithOverflowExceededTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithOverflowExceededTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithOverflowData(loadProfile.getInterval()));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);
        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(currentTimeStamp);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        Pair<DeviceIdentifier<Device>, PreStoreLoadProfile.LocalLoadProfile> localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        for (int i = 0; i < collectedLoadProfile.getCollectedIntervalData().size(); i++) {
            IntervalData intervalData = collectedLoadProfile.getCollectedIntervalData().get(i);
            for (int j = 0; j < intervalData.getIntervalValues().size(); j++) {
                IntervalValue intervalValue = intervalData.getIntervalValues().get(j);
                IntervalReading intervalReading = localLoadProfile.getLast().getIntervalBlocks().get(j).getIntervals().get(i);
                if (i >= 2 && j == 0) { // only the third and fourth interval of channel 1 have overflowed ...
                    assertThat(new BigDecimal(intervalValue.getNumber().toString()).subtract(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE)).compareTo(intervalReading.getValue()))
                            .overridingErrorMessage("Values are not the same -> %s and %s",
                                    new BigDecimal(intervalValue.getNumber().toString()).subtract(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE)),
                                    intervalReading.getValue())
                            .isEqualTo(0);
                } else {
                    assertThat(new BigDecimal(intervalValue.getNumber().toString()).compareTo(intervalReading.getValue()))
                            .overridingErrorMessage("Values are not the same -> %s and %s",
                                    new BigDecimal(intervalValue.getNumber().toString()),
                                    intervalReading.getValue())
                            .isEqualTo(0);
                }
            }
        }
    }

    public OfflineLoadProfile createMockedOfflineLoadProfile(Device device) {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        when(deviceIdentifier.getDeviceIdentifierType()).thenReturn(DeviceIdentifierType.ActualDevice);
        when(deviceIdentifier.getIdentifier()).thenReturn(String.valueOf(device.getId()));
        when(this.identificationService.createDeviceIdentifierForAlreadyKnownDevice(device)).thenReturn(deviceIdentifier);
        return new OfflineLoadProfileImpl(device.getLoadProfiles().get(0), mock(TopologyService.class), this.identificationService);
    }

    @Test
    @Transactional
    public void preStoreWithPositiveScalingAndOverflowExceededTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithPositiveScalingAndOverflowExceededTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithOverflowDataAfterPositiveScaling(loadProfile.getInterval()));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);
        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);
        freezeClock(currentTimeStamp);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        Pair<DeviceIdentifier<Device>, PreStoreLoadProfile.LocalLoadProfile> localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        for (int i = 0; i < collectedLoadProfile.getCollectedIntervalData().size(); i++) {
            IntervalData intervalData = collectedLoadProfile.getCollectedIntervalData().get(i);
            for (int j = 0; j < intervalData.getIntervalValues().size(); j++) {
                IntervalValue intervalValue = intervalData.getIntervalValues().get(j);
                IntervalReading intervalReading = localLoadProfile.getLast().getIntervalBlocks().get(j).getIntervals().get(i);
                BigDecimal intervalValueBigDecimal = new BigDecimal(intervalValue.getNumber().toString()).multiply(BigDecimal.valueOf(1000));
                if (i >= 2 && j == 0) { // only the third and fourth interval of channel 1 have overflowed ...
                    assertThat(intervalValueBigDecimal.subtract(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE)).compareTo(intervalReading.getValue()))
                            .overridingErrorMessage("Values are not the same -> %s and %s",
                                    intervalValueBigDecimal.subtract(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE)),
                                    intervalReading.getValue())
                            .isEqualTo(0);
                } else {
                    assertThat(intervalValueBigDecimal.compareTo(intervalReading.getValue()))
                            .overridingErrorMessage("Values are not the same -> %s and %s",
                                    intervalValueBigDecimal,
                                    intervalReading.getValue())
                            .isEqualTo(0);
                }
            }
        }
    }

    @Test
    @Transactional
    public void preStoreWithNegativeScalingAndOverflowExceededTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithNegativeScalingAndOverflowExceededTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithOverflowDataAfterNegativeScaling(loadProfile.getInterval()));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);
        freezeClock(currentTimeStamp);

        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        Pair<DeviceIdentifier<Device>, PreStoreLoadProfile.LocalLoadProfile> localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        for (int i = 0; i < collectedLoadProfile.getCollectedIntervalData().size(); i++) {
            IntervalData intervalData = collectedLoadProfile.getCollectedIntervalData().get(i);
            for (int j = 0; j < intervalData.getIntervalValues().size(); j++) {
                IntervalValue intervalValue = intervalData.getIntervalValues().get(j);
                IntervalReading intervalReading = localLoadProfile.getLast().getIntervalBlocks().get(j).getIntervals().get(i);
                BigDecimal intervalValueBigDecimal = new BigDecimal(intervalValue.getNumber().toString()).divide(BigDecimal.valueOf(1000));
                if (i >= 2 && j == 0) { // only the third and fourth interval of channel 1 have overflowed ...
                    assertThat(intervalValueBigDecimal.subtract(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE)).compareTo(intervalReading.getValue()))
                            .overridingErrorMessage("Values are not the same -> %s and %s",
                                    intervalValueBigDecimal.subtract(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE)),
                                    intervalReading.getValue())
                            .isEqualTo(0);
                } else {
                    assertThat(intervalValueBigDecimal.compareTo(intervalReading.getValue()))
                            .overridingErrorMessage("Values are not the same -> %s and %s",
                                    intervalValueBigDecimal,
                                    intervalReading.getValue())
                            .isEqualTo(0);
                }
            }
        }
    }

    @Test
    @Transactional
    public void removeLastReadingIntervalTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithNegativeScalingAndOverflowExceededTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        device.getLoadProfileUpdaterFor(loadProfile).setLastReading(intervalEndTime1.toInstant()).update();
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createCollectedLoadProfile(loadProfile));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);
        freezeClock(currentTimeStamp);
        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        Pair<DeviceIdentifier<Device>, PreStoreLoadProfile.LocalLoadProfile> localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(localLoadProfile.getLast().getIntervalBlocks().get(0).getIntervals()).hasSize(3);
        assertThat(localLoadProfile.getLast().getIntervalBlocks().get(0).getIntervals().get(0).getTimeStamp()).isEqualTo(intervalEndTime2.toInstant());
        assertThat(localLoadProfile.getLast().getIntervalBlocks().get(0).getIntervals().get(1).getTimeStamp()).isEqualTo(intervalEndTime3.toInstant());
        assertThat(localLoadProfile.getLast().getIntervalBlocks().get(0).getIntervals().get(2).getTimeStamp()).isEqualTo(intervalEndTime4.toInstant());
    }

    @Test
    @Transactional
    public void testUpperRangeValueWithCurrentTimeStamp() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithNegativeScalingAndOverflowExceededTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        device.getLoadProfileUpdaterFor(loadProfile).setLastReading(intervalEndTime1.toInstant()).update();
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createCollectedLoadProfile(loadProfile));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);
        freezeClock(intervalEndTime4);
        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        Pair<DeviceIdentifier<Device>, PreStoreLoadProfile.LocalLoadProfile> localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(localLoadProfile.getLast().getIntervalBlocks().get(0).getIntervals()).hasSize(3);
        assertThat(localLoadProfile.getLast().getIntervalBlocks().get(0).getIntervals().get(0).getTimeStamp()).isEqualTo(intervalEndTime2.toInstant());
        assertThat(localLoadProfile.getLast().getIntervalBlocks().get(0).getIntervals().get(1).getTimeStamp()).isEqualTo(intervalEndTime3.toInstant());
        assertThat(localLoadProfile.getLast().getIntervalBlocks().get(0).getIntervals().get(2).getTimeStamp()).isEqualTo(intervalEndTime4.toInstant());
    }

    @Test
    @Transactional
    public void testUpperRangeValueAfterCurrentTimeStamp() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithNegativeScalingAndOverflowExceededTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        device.getLoadProfileUpdaterFor(loadProfile).setLastReading(intervalEndTime1.toInstant()).update();
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createCollectedLoadProfile(loadProfile));
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);
        freezeClock(intervalEndTime3);
        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        Pair<DeviceIdentifier<Device>, PreStoreLoadProfile.LocalLoadProfile> localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(localLoadProfile.getLast().getIntervalBlocks().get(0).getIntervals()).hasSize(2);
        assertThat(localLoadProfile.getLast().getIntervalBlocks().get(0).getIntervals().get(0).getTimeStamp()).isEqualTo(intervalEndTime2.toInstant());
        assertThat(localLoadProfile.getLast().getIntervalBlocks().get(0).getIntervals().get(1).getTimeStamp()).isEqualTo(intervalEndTime3.toInstant());
    }

    protected ComServerDAOImpl mockComServerDAOWithOfflineLoadProfile(OfflineLoadProfile offlineLoadProfile) {
        final ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        doCallRealMethod().when(comServerDAO).storeMeterReadings(any(DeviceIdentifier.class), any(MeterReading.class));
        when(comServerDAO.executeTransaction(any())).thenAnswer(invocation -> ((Transaction<?>) invocation.getArguments()[0]).perform());
        when(comServerDAO.findOfflineLoadProfile(any(LoadProfileIdentifier.class))).thenReturn(offlineLoadProfile);
        DeviceIdentifier<Device> deviceIdentifier = (DeviceIdentifier<Device>) offlineLoadProfile.getDeviceIdentifier();
        when(comServerDAO.getDeviceIdentifierFor(any(LoadProfileIdentifier.class))).thenReturn(deviceIdentifier);
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
        intervalDatas.add(new IntervalData(intervalEndTime1, 0, 0, 0, getIntervalValues(0)));
        intervalDatas.add(new IntervalData(intervalEndTime2, 0, 0, 0, getIntervalValues(1)));
        intervalDatas.add(new IntervalData(intervalEndTime3, 0, 0, 0, getIntervalValues(2)));
        intervalDatas.add(new IntervalData(intervalEndTime4, 0, 0, 0, getIntervalValues(3)));
        return intervalDatas;
    }

    private List<IntervalData> createMockedIntervalDataThatOverflowsOnThirdValue() {
        List<IntervalData> intervalDatas = new ArrayList<>();
        intervalDatas.add(new IntervalData(intervalEndTime1, 0, 0, 0, getIntervalValuesFor(DeviceCreator.CHANNEL_OVERFLOW_VALUE - 1, 10)));
        intervalDatas.add(new IntervalData(intervalEndTime2, 0, 0, 0, getIntervalValuesFor(DeviceCreator.CHANNEL_OVERFLOW_VALUE, 132)));
        intervalDatas.add(new IntervalData(intervalEndTime3, 0, 0, 0, getIntervalValuesFor(DeviceCreator.CHANNEL_OVERFLOW_VALUE + 1, 1651)));
        intervalDatas.add(new IntervalData(intervalEndTime4, 0, 0, 0, getIntervalValuesFor(DeviceCreator.CHANNEL_OVERFLOW_VALUE + 2, 865461)));
        return intervalDatas;
    }

    private List<IntervalData> createMockedIntervalDataThatOverflowsOnThirdValueAfterUpScaling() {
        List<IntervalData> intervalDatas = new ArrayList<>();
        intervalDatas.add(new IntervalData(intervalEndTime1, 0, 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE - 1).divide(BigDecimal.valueOf(1000)), 10)));
        intervalDatas.add(new IntervalData(intervalEndTime2, 0, 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE).divide(BigDecimal.valueOf(1000)), 132)));
        intervalDatas.add(new IntervalData(intervalEndTime3, 0, 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE + 1).divide(BigDecimal.valueOf(1000)), 165)));
        intervalDatas.add(new IntervalData(intervalEndTime4, 0, 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE + 2).divide(BigDecimal.valueOf(1000)), 865)));
        return intervalDatas;
    }

    private List<IntervalData> createMockedIntervalDataThatOverflowsOnThirdValueAfterDownScaling() {
        List<IntervalData> intervalDatas = new ArrayList<>();
        intervalDatas.add(new IntervalData(intervalEndTime1, 0, 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE - 1).multiply(BigDecimal.valueOf(1000)), 10)));
        intervalDatas.add(new IntervalData(intervalEndTime2, 0, 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE).multiply(BigDecimal.valueOf(1000)), 132)));
        intervalDatas.add(new IntervalData(intervalEndTime3, 0, 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE + 1).multiply(BigDecimal.valueOf(1000)), 165)));
        intervalDatas.add(new IntervalData(intervalEndTime4, 0, 0, 0, getIntervalValuesFor(BigDecimal.valueOf(DeviceCreator.CHANNEL_OVERFLOW_VALUE + 2).multiply(BigDecimal.valueOf(1000)), 865)));
        return intervalDatas;
    }

    public List<IntervalValue> getIntervalValuesFor(Number intervalValueOne, Number intervalValueTwo) {
        List<IntervalValue> intervalValues = new ArrayList<>();
        intervalValues.add(new IntervalValue(intervalValueOne, 0, 0));
        intervalValues.add(new IntervalValue(intervalValueTwo, 0, 0));
        return intervalValues;
    }

    private List<IntervalData> createMockedIntervalDataWithTwoEntriesInFuture() {
        List<IntervalData> intervalDatas = new ArrayList<>();
        intervalDatas.add(new IntervalData(intervalEndTime1, 0, 0, 0, getIntervalValues(0)));
        intervalDatas.add(new IntervalData(intervalEndTime2, 0, 0, 0, getIntervalValues(1)));
        intervalDatas.add(new IntervalData(intervalEndTime3, 0, 0, 0, getIntervalValues(2)));
        intervalDatas.add(new IntervalData(intervalEndTime4, 0, 0, 0, getIntervalValues(3)));
        intervalDatas.add(new IntervalData(futureIntervalEndTime1, 0, 0, 0, getIntervalValues(4)));
        intervalDatas.add(new IntervalData(futureIntervalEndTime2, 0, 0, 0, getIntervalValues(5)));
        return intervalDatas;
    }

    CollectedLoadProfile enhanceCollectedLoadProfile(LoadProfile loadProfile, CollectedLoadProfile collectedLoadProfile) {
        LoadProfileIdentifier loadProfileIdentifier = mock(LoadProfileIdentifier.class);
        when(loadProfileIdentifier.findLoadProfile()).thenReturn(loadProfile);
        when(collectedLoadProfile.getLoadProfileIdentifier()).thenReturn(loadProfileIdentifier);
        when(loadProfileIdentifier.getDeviceIdentifier()).thenReturn(new DeviceIdentifierForAlreadyKnownDeviceByMrID(loadProfile.getDevice()));
        return collectedLoadProfile;
    }

    List<Channel> getChannels(long deviceId) {
        Optional<AmrSystem> amrSystem = getMeteringService().findAmrSystem(1);
        for (MeterActivation meterActivation : amrSystem.get().findMeter(String.valueOf(deviceId)).get().getMeterActivations()) {
            if (meterActivation.isCurrent()) {
                return meterActivation.getChannels();
            }
        }
        return Collections.emptyList();
    }

}