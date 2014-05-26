package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.engine.DeviceCreator;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the store functionality of the {@link CollectedLoadProfileDeviceCommand}
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/01/14
 * Time: 15:30
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedLoadProfileStoreDeviceCommandTest extends AbstractCollectedDataIntegrationTest {

    private static final String DEVICE_NAME = "DeviceName";

    private final int intervalValueOne = 123;
    private final int intervalValueTwo = 6516516;
    private final Unit kiloWattHours = Unit.get("kWh");

    private Date verificationTimeStamp = new DateTime(2015, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
    private Date currentTimeStamp = new DateTime(2014, 1, 13, 10, 0, 0, 0, DateTimeZone.UTC).toDate();

    private Date fromClock = new DateTime(2013, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
    private Date intervalEndTime1 = new DateTime(2014, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
    private Date intervalEndTime2 = new DateTime(2014, 1, 1, 0, 15, 0, 0, DateTimeZone.UTC).toDate();
    private Date intervalEndTime3 = new DateTime(2014, 1, 1, 0, 30, 0, 0, DateTimeZone.UTC).toDate();
    private Date intervalEndTime4 = new DateTime(2014, 1, 1, 0, 45, 0, 0, DateTimeZone.UTC).toDate();

    private DeviceCreator deviceCreator;

    private LoadProfileType loadProfileType;

    @Mock
    private MdcReadingTypeUtilService readingTypeService;

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
                loadProfileType.save();
                return loadProfileType;
            }
        });
    }

    @After
    public void cleanup(){
        this.deviceCreator.destroy();
        this.executeInTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                loadProfileType.delete();
            }
        });
    }

    @Test
    public void successfulDoubleStoreTestWithSameData() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("successfulDoubleStoreTestWithSameData").loadProfileTypes(this.loadProfileType).create();
        long deviceId = device.getId();

        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(device.getLoadProfiles().get(0));

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();

        freezeClock(verificationTimeStamp);
        collectedLoadProfileDeviceCommand.execute(comServerDAO);
        collectedLoadProfileDeviceCommand.execute(comServerDAO);

        List<Channel> channels = getChannels(deviceId);

        assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel1).hasSize(4);
        assertThat(intervalReadingsChannel1.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel1.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 2));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel2).hasSize(4);
        assertThat(intervalReadingsChannel2.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel2.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 2));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    public void successfulStoreTest() throws SQLException, BusinessException {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("successfulStoreTest").loadProfileTypes(this.loadProfileType).create();
        long deviceId = device.getId();
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(device.getLoadProfiles().get(0));

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();

        freezeClock(verificationTimeStamp);
        collectedLoadProfileDeviceCommand.execute(comServerDAO);

        List<Channel> channels = getChannels(deviceId);

        assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel1).hasSize(4);
        assertThat(intervalReadingsChannel1.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel1.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 2));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel2).hasSize(4);
        assertThat(intervalReadingsChannel2.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel2.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 2));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    public void successfulStoreWithDeltaDataTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("successfulStoreWithDeltaDataTest").loadProfileTypes(this.loadProfileType).create();
        long deviceId = device.getId();
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfileWithDeltaData(device.getLoadProfiles().get(0));

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();

        freezeClock(verificationTimeStamp);

        collectedLoadProfileDeviceCommand.execute(comServerDAO);

        List<Channel> channels = getChannels(deviceId);

        assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel1).hasSize(4);

        assertThat(intervalReadingsChannel1.get(0).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 2));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel2).hasSize(4);
        assertThat(intervalReadingsChannel2.get(0).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 2));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    public void successfulStoreWithUpdatedDataTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("successfulStoreWithUpdatedDataTest").loadProfileTypes(this.loadProfileType).create();
        long deviceId = device.getId();
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(device.getLoadProfiles().get(0));

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();

        freezeClock(verificationTimeStamp);

        collectedLoadProfileDeviceCommand.execute(comServerDAO);

        List<IntervalData> updatedCollectedIntervalData = new ArrayList<>();

        List<IntervalValue> updatedIntervalList = new ArrayList<>();
        int updatedIntervalChannelOne = 7777;
        int updatedIntervalChannelTwo = 3333;
        updatedIntervalList.add(new IntervalValue(updatedIntervalChannelOne, 0, 0));
        updatedIntervalList.add(new IntervalValue(updatedIntervalChannelTwo, 0, 0));
        updatedCollectedIntervalData.add(new IntervalData(intervalEndTime3, 0, 0, 0, updatedIntervalList));
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(updatedCollectedIntervalData);

        collectedLoadProfileDeviceCommand.execute(comServerDAO);

        List<Channel> channels = getChannels(deviceId);

        assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel1).hasSize(4);
        assertThat(intervalReadingsChannel1.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel1.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(7653));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(updatedIntervalChannelOne));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(-7651));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel2).hasSize(4);
        assertThat(intervalReadingsChannel2.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel2.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(-6513184));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(updatedIntervalChannelTwo));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(6513186));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    public void updateLastReadingTest() throws SQLException, BusinessException {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("updateLastReadingTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(loadProfile);

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();

        // Business method
        collectedLoadProfileDeviceCommand.execute(comServerDAO);

        // Asserts
        assertThat(device.getLoadProfiles().get(0).getLastReading()).isEqualTo(intervalEndTime4);
    }

    private CollectedLoadProfile createCollectedLoadProfile(LoadProfile loadProfile) {
        return enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithTwoChannels());
    }

    private CollectedLoadProfile createCollectedLoadProfileWithDeltaData(LoadProfile loadProfile) {
        return enhanceCollectedLoadProfile(loadProfile, createMockLoadProfileWithTwoDeltaChannels());
    }

    private CollectedLoadProfile createMockLoadProfileWithTwoChannels() {
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class, RETURNS_DEEP_STUBS);
        List<ChannelInfo> mockedChannelInfos = createMockedChannelInfos();
        when(collectedLoadProfile.getChannelInfo()).thenReturn(mockedChannelInfos);
        List<IntervalData> mockedCollectedIntervalData = createMockedIntervalData();
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(mockedCollectedIntervalData);
        return collectedLoadProfile;
    }

    private CollectedLoadProfile createMockLoadProfileWithTwoDeltaChannels() {
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class);
        List<ChannelInfo> mockedChannelInfos = createMockedDeltaChannelInfos();
        when(collectedLoadProfile.getChannelInfo()).thenReturn(mockedChannelInfos);
        List<IntervalData> mockedCollectedIntervalData = createMockedIntervalData();
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(mockedCollectedIntervalData);
        return collectedLoadProfile;
    }

    private List<ChannelInfo> createMockedChannelInfos() {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        String channelObisCodeOne = "1.0.1.8.0.255";
        channelInfos.add(new ChannelInfo(1, channelObisCodeOne, kiloWattHours));
        String channelObisCodeTwo = "1.0.2.8.0.255";
        channelInfos.add(new ChannelInfo(2, channelObisCodeTwo, kiloWattHours));
        return channelInfos;
    }

    private List<ChannelInfo> createMockedDeltaChannelInfos() {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        String deltaChannelObisCodeOne = "1.0.1.6.0.255";
        channelInfos.add(new ChannelInfo(1, deltaChannelObisCodeOne, kiloWattHours));
        String deltaChannelObisCodeTwo = "1.0.2.6.0.255";
        channelInfos.add(new ChannelInfo(2, deltaChannelObisCodeTwo, kiloWattHours));
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

    private CollectedLoadProfile enhanceCollectedLoadProfile(LoadProfile loadProfile, CollectedLoadProfile collectedLoadProfile) {
        LoadProfileIdentifier loadProfileIdentifier = mock(LoadProfileIdentifier.class);
        when(loadProfileIdentifier.findLoadProfile()).thenReturn(loadProfile);
        when(collectedLoadProfile.getLoadProfileIdentifier()).thenReturn(loadProfileIdentifier);
        return collectedLoadProfile;
    }

    private List<Channel> getChannels(long deviceId) {
        Optional<AmrSystem> amrSystem = getMeteringService().findAmrSystem(1);
        for (MeterActivation meterActivation : amrSystem.get().findMeter(String.valueOf(deviceId)).get().getMeterActivations()) {
            if (meterActivation.isCurrent()) {
                return meterActivation.getChannels();
            }
        }
        return Collections.emptyList();
    }
}
