package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.engine.DeviceCreator;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the store functionality of the {@link CollectedLoadProfileDeviceCommand}
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/01/14
 * Time: 15:30
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadProfilePreStorerTest extends AbstractCollectedDataIntegrationTest {

    static final String DEVICE_NAME = "DeviceName";

    final int intervalValueOne = 123;
    final int intervalValueTwo = 651;
    final Unit kiloWattHours = Unit.get("kWh");
    final Unit wattHours = Unit.get("Wh");

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
                getInjector().getInstance(DeviceDataService.class),
                getInjector().getInstance(TransactionService.class));
        this.loadProfileType = createLoadProfileType();
        initializeEnvironment();
    }

    private static void initializeEnvironment() {
        Environment mockedEnvironment = mock(Environment.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(mockedEnvironment.getApplicationContext()).thenReturn(applicationContext);
        Environment.DEFAULT.set(mockedEnvironment);
    }


    private LoadProfileType createLoadProfileType() {
        return this.executeInTransaction(new Transaction<LoadProfileType>() {
            @Override
            public LoadProfileType perform() {
                LoadProfileType loadProfileType = getInjector().getInstance(MasterDataService.class).newLoadProfileType("MyLoadProfileType", ObisCode.fromString("1.0.99.1.0.255"), TimeDuration.minutes(15));
                loadProfileType.createChannelTypeForRegisterType(getMasterDataService().findRegisterTypeByReadingType(getMeteringService().getReadingType(getMdcReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.0.255"), kiloWattHours)).get()).get());
                loadProfileType.createChannelTypeForRegisterType(getMasterDataService().findRegisterTypeByReadingType(getMeteringService().getReadingType(getMdcReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.2.8.0.255"), kiloWattHours)).get()).get());
                loadProfileType.save();
                return loadProfileType;
            }
        });
    }

    @After
    public void cleanup() {
        this.deviceCreator.destroy();
        this.executeInTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                loadProfileType.delete();
            }
        });
    }

    @Test
    public void simplePreStoreWithDataInFutureTest() {

        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("simplePreStoreWithDataInFutureTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithTwoChannelsAndDataInFuture(loadProfile.getInterval()));

        freezeClock(currentTimeStamp);

        assertThat(collectedLoadProfile.getCollectedIntervalData()).overridingErrorMessage("The collected data should contain {0} intervals to start", 6).hasSize(6);

        LoadProfilePreStorer loadProfilePreStorer = new LoadProfilePreStorer(getClock(), getMdcReadingTypeUtilService());
        LoadProfilePreStorer.LocalLoadProfile localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        assertThat(localLoadProfile.getIntervalBlocks().get(0).getIntervals()).hasSize(4);
    }

    @Test
    public void preStoreWithPositiveUnitConversionTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithPositiveUnitConversionTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithPositiveScaler(loadProfile.getInterval()));

        freezeClock(currentTimeStamp);

        LoadProfilePreStorer loadProfilePreStorer = new LoadProfilePreStorer(getClock(), getMdcReadingTypeUtilService());
        LoadProfilePreStorer.LocalLoadProfile localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        for (int i = 0; i < collectedLoadProfile.getCollectedIntervalData().size(); i++) {
            IntervalData intervalData = collectedLoadProfile.getCollectedIntervalData().get(i);
            for (int j = 0; j < intervalData.getIntervalValues().size(); j++) {
                IntervalValue intervalValue = intervalData.getIntervalValues().get(j);
                IntervalReading intervalReading = localLoadProfile.getIntervalBlocks().get(j).getIntervals().get(i);
                assertThat(new BigDecimal(intervalValue.getNumber().toString()).multiply(BigDecimal.valueOf(1000)).compareTo(intervalReading.getValue()))
                        .overridingErrorMessage("Values are not the same -> %s and %s",
                                new BigDecimal(intervalValue.getNumber().toString()).multiply(BigDecimal.valueOf(1000)),
                                intervalReading.getValue())
                        .isEqualTo(0);
            }
        }
    }

    @Test
    public void preStoreWithNegativeUnitConversionTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithNegativeUnitConversionTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithNegativeScaler(loadProfile.getInterval()));

        freezeClock(currentTimeStamp);

        LoadProfilePreStorer loadProfilePreStorer = new LoadProfilePreStorer(getClock(), getMdcReadingTypeUtilService());
        LoadProfilePreStorer.LocalLoadProfile localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        for (int i = 0; i < collectedLoadProfile.getCollectedIntervalData().size(); i++) {
            IntervalData intervalData = collectedLoadProfile.getCollectedIntervalData().get(i);
            for (int j = 0; j < intervalData.getIntervalValues().size(); j++) {
                IntervalValue intervalValue = intervalData.getIntervalValues().get(j);
                IntervalReading intervalReading = localLoadProfile.getIntervalBlocks().get(j).getIntervals().get(i);
                assertThat(new BigDecimal(intervalValue.getNumber().toString()).divide(BigDecimal.valueOf(1000)).compareTo(intervalReading.getValue()))
                        .overridingErrorMessage("Values are not the same -> %s and %s",
                                new BigDecimal(intervalValue.getNumber().toString()).divide(BigDecimal.valueOf(1000)),
                                intervalReading.getValue())
                        .isEqualTo(0);
            }
        }
    }

    @Test
    public void preStoreWithOverflowExceededTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithOverflowExceededTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithOverflowData(loadProfile.getInterval()));

        freezeClock(currentTimeStamp);

        LoadProfilePreStorer loadProfilePreStorer = new LoadProfilePreStorer(getClock(), getMdcReadingTypeUtilService());
        LoadProfilePreStorer.LocalLoadProfile localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        for (int i = 0; i < collectedLoadProfile.getCollectedIntervalData().size(); i++) {
            IntervalData intervalData = collectedLoadProfile.getCollectedIntervalData().get(i);
            for (int j = 0; j < intervalData.getIntervalValues().size(); j++) {
                IntervalValue intervalValue = intervalData.getIntervalValues().get(j);
                IntervalReading intervalReading = localLoadProfile.getIntervalBlocks().get(j).getIntervals().get(i);
                if(i >= 2 && j == 0){ // only the third and fourth interval of channel 1 have overflowed ...
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

    @Test
    public void preStoreWithPositiveScalingAndOverflowExceededTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithPositiveScalingAndOverflowExceededTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithOverflowDataAfterPositiveScaling(loadProfile.getInterval()));

        freezeClock(currentTimeStamp);

        LoadProfilePreStorer loadProfilePreStorer = new LoadProfilePreStorer(getClock(), getMdcReadingTypeUtilService());
        LoadProfilePreStorer.LocalLoadProfile localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        for (int i = 0; i < collectedLoadProfile.getCollectedIntervalData().size(); i++) {
            IntervalData intervalData = collectedLoadProfile.getCollectedIntervalData().get(i);
            for (int j = 0; j < intervalData.getIntervalValues().size(); j++) {
                IntervalValue intervalValue = intervalData.getIntervalValues().get(j);
                IntervalReading intervalReading = localLoadProfile.getIntervalBlocks().get(j).getIntervals().get(i);
                BigDecimal intervalValueBigDecimal = new BigDecimal(intervalValue.getNumber().toString()).multiply(BigDecimal.valueOf(1000));
                if(i >= 2 && j == 0){ // only the third and fourth interval of channel 1 have overflowed ...
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
    public void preStoreWithNegativeScalingAndOverflowExceededTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("preStoreWithNegativeScalingAndOverflowExceededTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile =
                enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithOverflowDataAfterNegativeScaling(loadProfile.getInterval()));

        freezeClock(currentTimeStamp);

        LoadProfilePreStorer loadProfilePreStorer = new LoadProfilePreStorer(getClock(), getMdcReadingTypeUtilService());
        LoadProfilePreStorer.LocalLoadProfile localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);

        for (int i = 0; i < collectedLoadProfile.getCollectedIntervalData().size(); i++) {
            IntervalData intervalData = collectedLoadProfile.getCollectedIntervalData().get(i);
            for (int j = 0; j < intervalData.getIntervalValues().size(); j++) {
                IntervalValue intervalValue = intervalData.getIntervalValues().get(j);
                IntervalReading intervalReading = localLoadProfile.getIntervalBlocks().get(j).getIntervals().get(i);
                BigDecimal intervalValueBigDecimal = new BigDecimal(intervalValue.getNumber().toString()).divide(BigDecimal.valueOf(1000));
                if(i >= 2 && j == 0){ // only the third and fourth interval of channel 1 have overflowed ...
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

    protected ComServerDAOImpl mockComServerDAOButCallRealMethodForMeterReadingStoring() {
        final ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        doCallRealMethod().when(comServerDAO).storeMeterReadings(any(DeviceIdentifier.class), any(MeterReading.class));
        when(comServerDAO.executeTransaction(any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return ((Transaction<?>) invocation.getArguments()[0]).perform();
            }
        });
        return comServerDAO;
    }

    void execute(final DeviceCommand command, final ComServerDAO comServerDAO) {
        this.executeInTransaction(new Transaction<Object>() {
            @Override
            public Object perform() {
                command.execute(comServerDAO);
                return null;
            }
        });
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
        ObisCode channelObisCodeOne = ObisCode.fromString("1.0.1.8.0.255");
        channelInfos.add(ChannelInfo.ChannelInfoBuilder.fromObisCode(channelObisCodeOne).meterIdentifier(DEVICE_NAME).unit(kiloWattHours)
                .readingTypeMRID(getMdcReadingTypeUtilService().getReadingTypeFrom(channelObisCodeOne, kiloWattHours, interval)).build());
        ObisCode channelObisCodeTwo = ObisCode.fromString("1.0.2.8.0.255");
        channelInfos.add(ChannelInfo.ChannelInfoBuilder.fromObisCode(channelObisCodeTwo).meterIdentifier(DEVICE_NAME).unit(kiloWattHours)
                .readingTypeMRID(getMdcReadingTypeUtilService().getReadingTypeFrom(channelObisCodeTwo, kiloWattHours, interval)).build());
        return channelInfos;
    }

    private List<ChannelInfo> createMockedChannelInfosWithPositiveThousandScaler(TimeDuration interval) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        ObisCode channelObisCodeOne = ObisCode.fromString("1.0.1.8.0.255");
        channelInfos.add(ChannelInfo.ChannelInfoBuilder.fromObisCode(channelObisCodeOne).meterIdentifier(DEVICE_NAME).unit(kiloWattHours)
                .readingTypeMRID(getMdcReadingTypeUtilService().getReadingTypeFrom(channelObisCodeOne, wattHours, interval)).build());
        ObisCode channelObisCodeTwo = ObisCode.fromString("1.0.2.8.0.255");
        channelInfos.add(ChannelInfo.ChannelInfoBuilder.fromObisCode(channelObisCodeTwo).meterIdentifier(DEVICE_NAME).unit(kiloWattHours)
                .readingTypeMRID(getMdcReadingTypeUtilService().getReadingTypeFrom(channelObisCodeTwo, wattHours, interval)).build());
        return channelInfos;
    }

    private List<ChannelInfo> createMockedChannelInfosWithNegativeThousandScaler(TimeDuration interval) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        ObisCode channelObisCodeOne = ObisCode.fromString("1.0.1.8.0.255");
        channelInfos.add(ChannelInfo.ChannelInfoBuilder.fromObisCode(channelObisCodeOne).meterIdentifier(DEVICE_NAME).unit(wattHours)
                .readingTypeMRID(getMdcReadingTypeUtilService().getReadingTypeFrom(channelObisCodeOne, kiloWattHours, interval)).build());
        ObisCode channelObisCodeTwo = ObisCode.fromString("1.0.2.8.0.255");
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