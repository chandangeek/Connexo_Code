/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.mdc.engine.DeviceCreator;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfile;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

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

    private MeterDataStoreCommand meterDataStoreCommand = new MeterDataStoreCommandImpl(null, new NoDeviceCommandServices());

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
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile, null, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());

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
        List<IntervalData> intervalData = Collections.singletonList(new IntervalData(now));
        List<ChannelInfo> channelInfo = Collections.singletonList(new ChannelInfo(CHANNEL_INFO_ID, CHANNEL1_ID, "testToStringWithOneInterval", Unit.get("kWh")));
        deviceLoadProfile.setCollectedData(intervalData, channelInfo);
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile, null, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());

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
                Collections.singletonList(
                        new ChannelInfo(
                                CHANNEL_INFO_ID,
                                CHANNEL1_ID,
                                "testToStringWithMultipleIntervalsFromOneChannel",
                                Unit.get("kWh")));
        deviceLoadProfile.setCollectedData(intervalData, channelInfo);
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile, null, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());

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
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile, null, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).matches(
                Pattern.compile(".*\\{load profile:.*1\\.33\\.1\\.8\\.0\\.255.*; interval data period: \\(2012-12-12T12:45:00Z‥2012-12-12T13:00:00Z\\]; channels: 2, 3\\}"));
    }


    @Test
    @Transactional
    public void successfulDoubleStoreTestWithSameData() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("successfulDoubleStoreTestWithSameData").loadProfileTypes(this.loadProfileType).create(Instant.ofEpochMilli(fromClock.getTime()));
        long deviceId = device.getId();
        when(this.getComServerDAOServiceProvider().deviceService()).thenReturn(this.deviceService);
        when(this.deviceService.findDeviceById(deviceId)).thenReturn(Optional.of(device));

        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(device.getLoadProfiles().get(0));

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, null, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(verificationTimeStamp);
        collectedLoadProfileDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // Business method
        meterDataStoreCommand.execute(comServerDAO);

        // Asserts
        List<Channel> channels = device.getCurrentMeterActivation().get().getChannelsContainer().getChannels();
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
    public void successfulStoreTest() throws SQLException {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("successfulStoreTest").loadProfileTypes(this.loadProfileType).create(Instant.ofEpochMilli(fromClock.getTime()));

        long deviceId = device.getId();
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(device.getLoadProfiles().get(0));
        when(this.getComServerDAOServiceProvider().deviceService()).thenReturn(this.deviceService);
        when(this.deviceService.findDeviceById(deviceId)).thenReturn(Optional.of(device));

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, null, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);
        freezeClock(verificationTimeStamp);

        // Business method
        collectedLoadProfileDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // Asserts
        List<Channel> channels = device.getCurrentMeterActivation().get().getChannelsContainer().getChannels();
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
    @Ignore //todo this fails !!!!
    public void successfulStoreWithDeltaDataTest() {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("successfulStoreWithDeltaDataTest").loadProfileTypes(this.loadProfileType).create(Instant.ofEpochMilli(fromClock.getTime()));
        long deviceId = device.getId();
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfileWithDeltaData(device.getLoadProfiles().get(0));
        when(this.getComServerDAOServiceProvider().deviceService()).thenReturn(this.deviceService);
        when(this.deviceService.findDeviceById(deviceId)).thenReturn(Optional.of(device));

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, null, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(verificationTimeStamp);

        // Business method
        collectedLoadProfileDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // Asserts
        List<Channel> channels = device.getCurrentMeterActivation().get().getChannelsContainer().getChannels();
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
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("successfulStoreWithUpdatedDataTest").loadProfileTypes(this.loadProfileType).create(Instant.ofEpochMilli(fromClock.getTime()));
        long deviceId = device.getId();
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(device.getLoadProfiles().get(0));
        when(this.getComServerDAOServiceProvider().deviceService()).thenReturn(this.deviceService);
        when(this.deviceService.findDeviceById(deviceId)).thenReturn(Optional.of(device));

        CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, null, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        freezeClock(verificationTimeStamp);

        collectedLoadProfileDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        List<IntervalData> updatedCollectedIntervalData = new ArrayList<>();
        List<IntervalValue> updatedIntervalList = new ArrayList<>();
        int updatedIntervalChannelOne = 7777;
        int updatedIntervalChannelTwo = 3333;
        updatedIntervalList.add(new IntervalValue(updatedIntervalChannelOne, 0, new HashSet<>()));
        updatedIntervalList.add(new IntervalValue(updatedIntervalChannelTwo, 0, new HashSet<>()));
        updatedCollectedIntervalData.add(new IntervalData(intervalEndTime3, new HashSet<>(), 0, 0, updatedIntervalList));
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(updatedCollectedIntervalData);

        meterDataStoreCommand = new MeterDataStoreCommandImpl(null, new NoDeviceCommandServices());
        collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, null, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());
        // Business method
        collectedLoadProfileDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // Asserts
        List<Channel> channels = device.getCurrentMeterActivation().get().getChannelsContainer().getChannels();
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
    public void updateLastReadingTest() throws SQLException {
        Device device = this.deviceCreator.name(DEVICE_NAME).mRDI("updateLastReadingTest").loadProfileTypes(this.loadProfileType).create(Instant.ofEpochMilli(fromClock.getTime()));
        when(getClock().instant()).thenReturn(Instant.now());
        LoadProfile loadProfile = device.getLoadProfiles().get(0);
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(loadProfile);
        when(this.getComServerDAOServiceProvider().deviceService()).thenReturn(this.deviceService);
        when(this.deviceService.findDeviceById(device.getId())).thenReturn(Optional.of(device));

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, null, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(device);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);

        // Business method
        collectedLoadProfileDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // Asserts
        assertThat(device.getLoadProfiles().get(0).getLastReading().isPresent()).isTrue();
        assertThat(device.getLoadProfiles().get(0).getLastReading().get()).isEqualTo(intervalEndTime4.toInstant());
    }

    @Test
    @Transactional
    public void successfulStoreForUnLinkedDataLoggerTest() throws SQLException {
        Device dataLogger = this.deviceCreator
                .name("DataLogger")
                .mRDI("unLinkedDataLogger")
                .loadProfileTypes(this.loadProfileType)
                .deviceTypeName(DeviceCreator.DATA_LOGGER_DEVICE_TYPE_NAME)
                .deviceConfigName(DeviceCreator.DATA_LOGGER_DEVICE_CONFIGURATION_NAME)
                .dataLoggerEnabled(true)
                .create(Instant.ofEpochMilli(fromClock.getTime()));
        long deviceId = dataLogger.getId();


        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(dataLogger.getLoadProfiles().get(0));
        when(this.getComServerDAOServiceProvider().deviceService()).thenReturn(this.deviceService);
        when(this.deviceService.findDeviceById(deviceId)).thenReturn(Optional.of(dataLogger));

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, null, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(dataLogger);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);
        freezeClock(verificationTimeStamp);

        // Business method
        collectedLoadProfileDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // Asserts
        List<Channel> channels = dataLogger.getCurrentMeterActivation().get().getChannelsContainer().getChannels();
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
    public void successfulStoreForLinkedDataLoggerTest() throws SQLException {
        Device dataLogger = this.deviceCreator
                .name("DataLogger")
                .mRDI("linkedDataLogger")
                .loadProfileTypes(this.loadProfileType)
                .deviceTypeName(DeviceCreator.DATA_LOGGER_DEVICE_TYPE_NAME)
                .deviceConfigName(DeviceCreator.DATA_LOGGER_DEVICE_CONFIGURATION_NAME)
                .dataLoggerEnabled(true)
                .create(Instant.ofEpochMilli(fromClock.getTime()));
        long dataLoggerId = dataLogger.getId();

        Device slave = this.slaveDeviceCreator
                .name("slave")
                .mRDI("slaveDevice")
                .loadProfileTypes(this.loadProfileType)
                .create(Instant.ofEpochMilli(fromClock.getTime()));
        long slaveId = slave.getId();

        when(this.getComServerDAOServiceProvider().deviceService()).thenReturn(this.deviceService);
        when(this.deviceService.findDeviceById(dataLoggerId)).thenReturn(Optional.of(dataLogger));
        when(this.deviceService.findDeviceById(slaveId)).thenReturn(Optional.of(slave));

        HashMap<com.energyict.mdc.device.data.Channel, com.energyict.mdc.device.data.Channel> channelMap = new HashMap<>();
        // Linking the slave
        LoadProfile dataLoggerLoadProfile = dataLogger.getLoadProfiles().get(0);
        LoadProfile slaveLoggerLoadProfile = slave.getLoadProfiles().get(0);
        slaveLoggerLoadProfile.getChannels().stream().forEach(slaveChannel -> {
                channelMap.put(slaveChannel, dataLoggerLoadProfile.getChannels().get(channelMap.size()));
        });
        createMockedOfflineLoadProfile(slave);

        getTopologyService().setDataLogger(slave, dataLogger, fromClock.toInstant() , channelMap, new HashMap<>() );
        //Assert the linking of the data logger channels with the slave channels
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(0), fromClock.toInstant()).get().getId()).isEqualTo(slaveLoggerLoadProfile.getChannels().get(0).getId());
        assertThat(getTopologyService().getSlaveChannel(dataLoggerLoadProfile.getChannels().get(1), fromClock.toInstant()).get().getId()).isEqualTo(slaveLoggerLoadProfile.getChannels().get(1).getId());

        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(dataLogger.getLoadProfiles().get(0));


        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, null, meterDataStoreCommand, new MdcReadingTypeUtilServiceAndClock());
        OfflineLoadProfile offlineLoadProfile = createMockedOfflineLoadProfile(dataLogger);

        final ComServerDAO comServerDAO = mockComServerDAOWithOfflineLoadProfile(offlineLoadProfile);
        freezeClock(verificationTimeStamp);

        // Business method
        collectedLoadProfileDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // Asserts
        List<Channel> dataLoggerChannels = dataLogger.getCurrentMeterActivation().get().getChannelsContainer().getChannels();
        assertThat(dataLoggerChannels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsDataLoggerChannel1 = dataLoggerChannels.get(0).getIntervalReadings(new Interval(fromClock, verificationTimeStamp).toOpenClosedRange());
        assertThat(intervalReadingsDataLoggerChannel1).hasSize(0);

        // Data is stored on the slave
        List<Channel> channels = slave.getCurrentMeterActivation().get().getChannelsContainer().getChannels();
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
}