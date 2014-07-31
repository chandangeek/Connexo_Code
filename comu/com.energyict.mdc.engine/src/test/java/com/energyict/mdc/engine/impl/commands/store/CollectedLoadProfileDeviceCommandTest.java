package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.ProgrammableClock;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfile;
import com.energyict.mdc.engine.impl.meterdata.identifiers.LoadProfileDataIdentifier;
import com.energyict.mdc.engine.impl.protocol.inbound.DeviceIdentifierById;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


/**
 * Tests the {@link CollectedLoadProfileDeviceCommand} component.
 * Primary focus is the toString method for now.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-12 (11:51)
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedLoadProfileDeviceCommandTest extends LoadProfilePreStorerTest {

    @Rule
    public TimeZoneNeutral weAreInAntarctica = Using.timeZoneOfMcMurdo();

    private static final long DEVICE_ID = 1;
    private static final int CHANNEL1_ID = 2;
    private static final int CHANNEL2_ID = CHANNEL1_ID + 1;
    private static final int CHANNEL_INFO_ID = CHANNEL1_ID + 1;
    private static final String OBIS_CODE = "1.33.1.8.0.255";
    private TimeZone toReset;

    @Before
    public void setUp() {
        super.setUp();
        toReset = TimeZone.getDefault();
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(toReset);
    }

    @Mock
    private DeviceDataService deviceDataService;

    @Test
    public void testToJournalMessageDescriptionWithoutCollectedData() {
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileDataIdentifier(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID, deviceDataService)));
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).isEqualTo(CollectedLoadProfileDeviceCommand.class.getSimpleName()
                + " {load profile: 1.33.1.8.0.255; interval data period: Interval{start=null, end=null}; channels: }");
    }

    @Test
    public void testToJournalMessageDescriptionWithOneInterval() {
        Date now = new DateTime(2012, 12, 12, 12, 53, 5, 0, DateTimeZone.UTC).toDate();
        Clock frozenClock = new ProgrammableClock().frozenAt(now);
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileDataIdentifier(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID, deviceDataService)));
        List<IntervalData> intervalData = Arrays.asList(new IntervalData(now));
        List<ChannelInfo> channelInfo = Arrays.asList(new ChannelInfo(CHANNEL_INFO_ID, CHANNEL1_ID, "testToStringWithOneInterval", Unit.get("kWh")));
        deviceLoadProfile.setCollectedData(intervalData, channelInfo);
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).isEqualTo(CollectedLoadProfileDeviceCommand.class.getSimpleName()
                + " {load profile: 1.33.1.8.0.255; interval data period: Interval{start=Thu Dec 13 01:53:05 NZDT 2012, end=Thu Dec 13 01:53:05 NZDT 2012}; channels: 2}");
    }

    @Test
    public void testToJournalMessageDescriptionWithMultipleIntervalsFromOneChannel() {
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileDataIdentifier(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID, deviceDataService)));
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
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).isEqualTo(CollectedLoadProfileDeviceCommand.class.getSimpleName()
                + " {load profile: 1.33.1.8.0.255; interval data period: Interval{start=Thu Dec 13 01:45:00 NZDT 2012, end=Thu Dec 13 02:30:00 NZDT 2012}; channels: 2}");
    }

    @Test
    public void testToJournalMessageDescriptionWithIntervalsFromMultipleChannels() {
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileDataIdentifier(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID, deviceDataService)));
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
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).isEqualTo(CollectedLoadProfileDeviceCommand.class.getSimpleName() +
                " {load profile: 1.33.1.8.0.255; interval data period: Interval{start=Thu Dec 13 01:45:00 NZDT 2012, end=Thu Dec 13 02:00:00 NZDT 2012}; channels: 2, 3}");
    }


    @Test
    public void successfulDoubleStoreTestWithSameData() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("successfulDoubleStoreTestWithSameData").loadProfileTypes(this.loadProfileType).create();
        long deviceId = device.getId();

        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(device.getLoadProfiles().get(0));

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();

        freezeClock(verificationTimeStamp);
        this.execute(collectedLoadProfileDeviceCommand, comServerDAO);

        // Business method
        this.execute(collectedLoadProfileDeviceCommand, comServerDAO);

        // Asserts
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

        // Business method
        this.execute(collectedLoadProfileDeviceCommand, comServerDAO);

        // Asserts
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

        // Business method
        this.execute(collectedLoadProfileDeviceCommand, comServerDAO);

        // Asserts
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

        this.execute(collectedLoadProfileDeviceCommand, comServerDAO);

        List<IntervalData> updatedCollectedIntervalData = new ArrayList<>();
        List<IntervalValue> updatedIntervalList = new ArrayList<>();
        int updatedIntervalChannelOne = 7777;
        int updatedIntervalChannelTwo = 3333;
        updatedIntervalList.add(new IntervalValue(updatedIntervalChannelOne, 0, 0));
        updatedIntervalList.add(new IntervalValue(updatedIntervalChannelTwo, 0, 0));
        updatedCollectedIntervalData.add(new IntervalData(intervalEndTime3, 0, 0, 0, updatedIntervalList));
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(updatedCollectedIntervalData);

        // Business method
        this.execute(collectedLoadProfileDeviceCommand, comServerDAO);

        // Asserts
        List<Channel> channels = getChannels(deviceId);
        assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel1).hasSize(4);
        assertThat(intervalReadingsChannel1.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel1.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(updatedIntervalChannelOne-(intervalValueOne + 1)));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(updatedIntervalChannelOne));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal((intervalValueOne + 3)-updatedIntervalChannelOne));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel2).hasSize(4);
        assertThat(intervalReadingsChannel2.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel2.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(updatedIntervalChannelTwo-(intervalValueTwo + 1)));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(updatedIntervalChannelTwo));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal((intervalValueTwo + 3)-updatedIntervalChannelTwo));
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
        this.execute(collectedLoadProfileDeviceCommand, comServerDAO);

        // Asserts
        assertThat(device.getLoadProfiles().get(0).getLastReading()).isEqualTo(intervalEndTime4);
    }
}