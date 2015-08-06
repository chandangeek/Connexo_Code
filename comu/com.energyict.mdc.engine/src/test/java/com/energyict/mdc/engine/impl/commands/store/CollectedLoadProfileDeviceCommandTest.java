package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfile;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CollectedLoadProfileDeviceCommand} component.
 * Primary focus is the toString method for now.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-12 (11:51)
 */
public class CollectedLoadProfileDeviceCommandTest extends PreStoreLoadProfileTest {

    private static final long DEVICE_ID = 1;
    private static final int CHANNEL1_ID = 2;
    private static final int CHANNEL2_ID = CHANNEL1_ID + 1;
    private static final int CHANNEL_INFO_ID = CHANNEL1_ID + 1;
    private static final String OBIS_CODE = "1.33.1.8.0.255";

    @Rule
    public TimeZoneNeutral weAreInAntarctica = Using.timeZoneOfMcMurdo();

    @Mock
    private DeviceService deviceService;

    private TimeZone toReset;

    private MeterDataStoreCommand meterDataStoreCommand = new MeterDataStoreCommandImpl(new NoDeviceCommandServices());

    @Before
    public void setUp() {
        super.setUp();
        toReset = TimeZone.getDefault();
    }

    @After
    public void restoreDefaultTimeZone() {
        TimeZone.setDefault(toReset);
    }

    @Test
    @Transactional
    public void testToJournalMessageDescriptionWithoutCollectedData() {
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileIdentifierByObisCodeAndDevice(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID, deviceService)));
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).matches(
                Pattern.compile(".*\\{load profile:.*1\\.33\\.1\\.8\\.0\\.255.*; interval data period: \\(-∞‥\\+∞\\); channels: \\}"));
    }

    @Test
    @Transactional
    public void testToJournalMessageDescriptionWithOneInterval() {
        Date now = new DateTime(2012, 12, 12, 12, 53, 5, 0, DateTimeZone.UTC).toDate();
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileIdentifierByObisCodeAndDevice(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID, deviceService)));
        List<IntervalData> intervalData = Arrays.asList(new IntervalData(now));
        List<ChannelInfo> channelInfo = Arrays.asList(new ChannelInfo(CHANNEL_INFO_ID, CHANNEL1_ID, "testToStringWithOneInterval", Unit.get("kWh")));
        deviceLoadProfile.setCollectedData(intervalData, channelInfo);
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).matches(
                Pattern.compile(".*\\{load profile:.*1\\.33\\.1\\.8\\.0\\.255.*interval data period: \\(2012-12-12T12:53:05Z‥2012-12-12T12:53:05Z\\]; channels: 2\\}"));
    }

    @Test
    @Transactional
    public void testToJournalMessageDescriptionWithMultipleIntervalsFromOneChannel() {
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileIdentifierByObisCodeAndDevice(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID, deviceService)));
        List<IntervalData> intervalData =
                Arrays.asList(
                        new IntervalData(new DateTime(2012, 12, 12, 12, 45, 0, 0, DateTimeZone.UTC).toDate()),
                        new IntervalData(new DateTime(2012, 12, 12, 13, 0, 0, 0, DateTimeZone.UTC).toDate()),
                        new IntervalData(new DateTime(2012, 12, 12, 13, 15, 0, 0, DateTimeZone.UTC).toDate()),
                        new IntervalData(new DateTime(2012, 12, 12, 13, 30, 0, 0, DateTimeZone.UTC).toDate()));
        List<ChannelInfo> channelInfo =
                Arrays.asList(
                        new ChannelInfo(
                                CHANNEL_INFO_ID,
                                CHANNEL1_ID,
                                "testToStringWithMultipleIntervalsFromOneChannel",
                                Unit.get("kWh")));
        deviceLoadProfile.setCollectedData(intervalData, channelInfo);
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).matches(
                Pattern.compile(".*\\{load profile:.*1\\.33\\.1\\.8\\.0\\.255.*; interval data period: \\(2012-12-12T12:45:00Z‥2012-12-12T13:30:00Z\\]; channels: 2\\}"));
    }

    @Test
    @Transactional
    public void testToJournalMessageDescriptionWithIntervalsFromMultipleChannels() {
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileIdentifierByObisCodeAndDevice(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID, deviceService)));
        List<IntervalData> intervalData =
                Arrays.asList(
                        new IntervalData(new DateTime(2012, 12, 12, 12, 45, 0, 0, DateTimeZone.UTC).toDate()),
                        new IntervalData(new DateTime(2012, 12, 12, 13, 0, 0, 0, DateTimeZone.UTC).toDate()),
                        new IntervalData(new DateTime(2012, 12, 12, 12, 45, 0, 0, DateTimeZone.UTC).toDate()),
                        new IntervalData(new DateTime(2012, 12, 12, 13, 0, 0, 0, DateTimeZone.UTC).toDate()));
        List<ChannelInfo> channelInfo =
                Arrays.asList(
                        new ChannelInfo(
                                CHANNEL_INFO_ID,
                                CHANNEL1_ID,
                                "Channel-1",
                                Unit.get("kWh")),
                        new ChannelInfo(
                                CHANNEL_INFO_ID,
                                CHANNEL2_ID,
                                "Channel-2",
                                Unit.get("kWh")));
        deviceLoadProfile.setCollectedData(intervalData, channelInfo);
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).matches(
                Pattern.compile(".*\\{load profile:.*1\\.33\\.1\\.8\\.0\\.255.*; interval data period: \\(2012-12-12T12:45:00Z‥2012-12-12T13:00:00Z\\]; channels: 2, 3\\}"));
    }


    @Test
    @Transactional
    public void successfulDoubleStoreTestWithSameData() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("successfulDoubleStoreTestWithSameData").loadProfileTypes(this.loadProfileType).create();
        long deviceId = device.getId();

        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(device.getLoadProfiles().get(0));

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(verificationTimeStamp);
        collectedLoadProfileDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // Business method
        meterDataStoreCommand.execute(comServerDAO);

        // Asserts
        List<Channel> channels = getChannels(deviceId);
        assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock, verificationTimeStamp).toOpenClosedRange());
        assertThat(intervalReadingsChannel1).hasSize(4);
        assertThat(intervalReadingsChannel1.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel1.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(intervalReadingsChannel1.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(intervalReadingsChannel1.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 2));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(intervalReadingsChannel1.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock, verificationTimeStamp).toOpenClosedRange());
        assertThat(intervalReadingsChannel2).hasSize(4);
        assertThat(intervalReadingsChannel2.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel2.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(intervalReadingsChannel2.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(intervalReadingsChannel2.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 2));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(intervalReadingsChannel2.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    @Transactional
    public void successfulStoreTest() throws SQLException, BusinessException {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("successfulStoreTest").loadProfileTypes(this.loadProfileType).create();
        long deviceId = device.getId();
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(device.getLoadProfiles().get(0));

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);
        freezeClock(verificationTimeStamp);

        // Business method
        collectedLoadProfileDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // Asserts
        List<Channel> channels = getChannels(deviceId);
        assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock, verificationTimeStamp).toOpenClosedRange());
        assertThat(intervalReadingsChannel1).hasSize(4);
        assertThat(intervalReadingsChannel1.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel1.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(intervalReadingsChannel1.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(intervalReadingsChannel1.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 2));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(intervalReadingsChannel1.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock, verificationTimeStamp).toOpenClosedRange());
        assertThat(intervalReadingsChannel2).hasSize(4);
        assertThat(intervalReadingsChannel2.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel2.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(intervalReadingsChannel2.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(intervalReadingsChannel2.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 2));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(intervalReadingsChannel2.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    @Transactional
    public void successfulStoreWithDeltaDataTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("successfulStoreWithDeltaDataTest").loadProfileTypes(this.loadProfileType).create();
        long deviceId = device.getId();
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfileWithDeltaData(device.getLoadProfiles().get(0));

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(verificationTimeStamp);

        // Business method
        collectedLoadProfileDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // Asserts
        List<Channel> channels = getChannels(deviceId);
        assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock, verificationTimeStamp).toOpenClosedRange());
        assertThat(intervalReadingsChannel1).hasSize(4);

        assertThat(intervalReadingsChannel1.get(0).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 2));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock, verificationTimeStamp).toOpenClosedRange());
        assertThat(intervalReadingsChannel2).hasSize(4);
        assertThat(intervalReadingsChannel2.get(0).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 2));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    @Transactional
    public void successfulStoreWithUpdatedDataTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("successfulStoreWithUpdatedDataTest").loadProfileTypes(this.loadProfileType).create();
        long deviceId = device.getId();
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(device.getLoadProfiles().get(0));

        CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(verificationTimeStamp);

        collectedLoadProfileDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        List<IntervalData> updatedCollectedIntervalData = new ArrayList<>();
        List<IntervalValue> updatedIntervalList = new ArrayList<>();
        int updatedIntervalChannelOne = 7777;
        int updatedIntervalChannelTwo = 3333;
        updatedIntervalList.add(new IntervalValue(updatedIntervalChannelOne, 0, 0));
        updatedIntervalList.add(new IntervalValue(updatedIntervalChannelTwo, 0, 0));
        updatedCollectedIntervalData.add(new IntervalData(intervalEndTime3, 0, 0, 0, updatedIntervalList));
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(updatedCollectedIntervalData);

        meterDataStoreCommand = new MeterDataStoreCommandImpl(new NoDeviceCommandServices());
        collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());
        // Business method
        collectedLoadProfileDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // Asserts
        List<Channel> channels = getChannels(deviceId);
        assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock, verificationTimeStamp).toOpenClosedRange());
        assertThat(intervalReadingsChannel1).hasSize(4);
        assertThat(intervalReadingsChannel1.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel1.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(intervalReadingsChannel1.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(updatedIntervalChannelOne - (intervalValueOne + 1)));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(updatedIntervalChannelOne));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(-7651));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock, verificationTimeStamp).toOpenClosedRange());
        assertThat(intervalReadingsChannel2).hasSize(4);
        assertThat(intervalReadingsChannel2.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel2.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(intervalReadingsChannel2.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(updatedIntervalChannelTwo - (intervalValueTwo + 1)));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(updatedIntervalChannelTwo));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(-2679));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    @Transactional
    public void updateLastReadingTest() throws SQLException, BusinessException {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("updateLastReadingTest").loadProfileTypes(this.loadProfileType).create();
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(loadProfile);

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAOImpl comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        // Business method
        collectedLoadProfileDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // Asserts
        assertThat(device.getLoadProfiles().get(0).getLastReading().isPresent()).isTrue();
        assertThat(device.getLoadProfiles().get(0).getLastReading().get()).isEqualTo(intervalEndTime4.toInstant());
    }
}