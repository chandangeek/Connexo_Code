/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.LoadProfilesTask;
import com.energyict.mdc.common.tasks.LogBooksTask;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.identifiers.LogBookIdentifierById;
import com.energyict.mdc.engine.TestSerialNumberDeviceIdentifier;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.CreateMeterEventsFromStatusFlagsCommand;
import com.energyict.mdc.engine.impl.commands.collect.LegacyLoadProfileLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.MarkIntervalsAsBadTimeCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLegacyLoadProfileLogBooksDataCommand;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceImpl;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLoadProfileChannel;
import com.energyict.mdc.upl.offline.OfflineLogBook;
import com.energyict.mdc.upl.offline.OfflineLogBookSpec;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Tests for the {@link LegacyLoadProfileLogBooksCommandImpl} component  <br>
 * This is basically a combination of all tests written for {@link LoadProfileCommandImpl} and
 * {@link LogBooksCommandImpl}.
 *
 * @author sva
 * @since 19/12/12 - 14:49
 */
@RunWith(MockitoJUnitRunner.class)
public class LegacyLoadProfileLogBooksCommandImplTest extends CommonCommandImplTests {

    private static final long LOGBOOK_ID_1 = 1;
    private static final long LOGBOOK_ID_2 = 2;
    private static final long LOGBOOK_ID_3 = 3;

    private static final ObisCode LOGBOOK1_OBIS = ObisCode.fromString("1.1.0.0.0.255");
    private static final ObisCode LOGBOOK2_OBIS = ObisCode.fromString("1.2.0.0.0.255");
    private static final ObisCode LOGBOOK3_OBIS = ObisCode.fromString("1.3.0.0.0.255");

    private static final long LOGBOOK_TYPE_1 = 10;
    private static final long LOGBOOK_TYPE_2 = 20;
    private static final long LOGBOOK_TYPE_3 = 30;

    private static final Instant LAST_LOGBOOK_1 = Instant.ofEpochMilli(1354320000L * 1000L); // 01 Dec 2012 00:00:00 GMT
    private static final Instant LAST_LOGBOOK_2 = Instant.ofEpochMilli(1351728000L * 1000L); // 01 Nov 2012 00:00:00 GMT
    private static final Instant LAST_LOGBOOK_3 = Instant.ofEpochMilli(1349049600L * 1000L); // 01 Oct 2012 00:00:00 GMT

    private static final ObisCode DEVICE_OBISCODE_LOGBOOK_1 = ObisCode.fromString("1.1.1.1.1.1");
    private static final ObisCode DEVICE_OBISCODE_LOGBOOK_2 = ObisCode.fromString("2.2.2.2.2.2");
    private static final ObisCode DEVICE_OBISCODE_LOGBOOK_3 = ObisCode.fromString("3.3.3.3.3.3");

    private static final String MY_MRID = "MyMrid";
    private static final ObisCode FIXED_LOAD_PROFILE_OBIS_CODE = ObisCode.fromString("1.0.99.1.0.255");
    private static final TimeDuration FIXED_LOAD_PROFILE_INTERVAL = new TimeDuration(900);
    private static final String FIXED_DEVICE_SERIAL_NUMBER = "FIXED_DEVICE_SERIAL_NUMBER";
    private static final Unit FIXED_CHANNEL_UNIT = Unit.get("kWh");
    private static final TestSerialNumberDeviceIdentifier SERIAL_NUMBER_DEVICE_IDENTIFIER = new TestSerialNumberDeviceIdentifier(FIXED_DEVICE_SERIAL_NUMBER);

    @Mock
    private OfflineLogBook offlineLogBook_A;
    @Mock
    private OfflineLogBook offlineLogBook_B;
    @Mock
    private OfflineLogBook offlineLogBook_C;
    @Mock
    private LogBookType logBookType_A;
    @Mock
    private LogBookType logBookType_B;
    @Mock
    private LogBookType logBookType_C;
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private OfflineDeviceImpl offlineDevice;
    @Mock
    private Device device;
    @Mock
    private LogBookService logBookService;

    @Before
    public void setUp() throws Exception {
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(device.getmRID()).thenReturn(MY_MRID);
        when(offlineLogBook_A.getLogBookId()).thenReturn(LOGBOOK_ID_1);
        when(offlineLogBook_A.getLogBookIdentifier()).thenReturn(new LogBookIdentifierById(LOGBOOK_ID_1, LOGBOOK1_OBIS, mock(DeviceIdentifier.class)));
        when(offlineLogBook_A.getLastReading()).thenReturn(Date.from(LAST_LOGBOOK_1));
        when(offlineLogBook_A.getMasterSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        OfflineLogBookSpec offlineLogBookSpec1 = mock(OfflineLogBookSpec.class);
        when(offlineLogBookSpec1.getDeviceObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_1);
        when(offlineLogBookSpec1.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_1);
        when(offlineLogBook_A.getOfflineLogBookSpec()).thenReturn(offlineLogBookSpec1);
        when(offlineLogBook_A.getDeviceIdentifier()).thenReturn(SERIAL_NUMBER_DEVICE_IDENTIFIER);

        when(offlineLogBook_B.getLogBookId()).thenReturn(LOGBOOK_ID_2);
        when(offlineLogBook_B.getLogBookIdentifier()).thenReturn(new LogBookIdentifierById(LOGBOOK_ID_2, LOGBOOK2_OBIS, mock(DeviceIdentifier.class)));
        when(offlineLogBook_B.getLastReading()).thenReturn(Date.from(LAST_LOGBOOK_2));
        when(offlineLogBook_B.getMasterSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        OfflineLogBookSpec offlineLogBookSpec2 = mock(OfflineLogBookSpec.class);
        when(offlineLogBookSpec2.getDeviceObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_2);
        when(offlineLogBookSpec2.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_2);
        when(offlineLogBook_B.getOfflineLogBookSpec()).thenReturn(offlineLogBookSpec2);
        when(offlineLogBook_B.getDeviceIdentifier()).thenReturn(SERIAL_NUMBER_DEVICE_IDENTIFIER);

        when(offlineLogBook_C.getLogBookId()).thenReturn(LOGBOOK_ID_3);
        when(offlineLogBook_C.getLogBookIdentifier()).thenReturn(new LogBookIdentifierById(LOGBOOK_ID_3, LOGBOOK3_OBIS, mock(DeviceIdentifier.class)));
        when(offlineLogBook_C.getLastReading()).thenReturn(Date.from(LAST_LOGBOOK_3));
        when(offlineLogBook_C.getMasterSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        OfflineLogBookSpec offlineLogBookSpec3 = mock(OfflineLogBookSpec.class);
        when(offlineLogBookSpec3.getDeviceObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_3);
        when(offlineLogBookSpec3.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_3);
        when(offlineLogBook_C.getOfflineLogBookSpec()).thenReturn(offlineLogBookSpec3);
        when(offlineLogBook_C.getDeviceIdentifier()).thenReturn(SERIAL_NUMBER_DEVICE_IDENTIFIER);

        when(logBookType_A.getId()).thenReturn(LOGBOOK_TYPE_1);
        when(logBookType_B.getId()).thenReturn(LOGBOOK_TYPE_2);
        when(logBookType_C.getId()).thenReturn(LOGBOOK_TYPE_3);

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(0L);
        when(comTaskExecution.getDevice()).thenReturn(device);
    }

    @Test(expected = CodingException.class)
    public void loadProfileTaskLogBooksTaskNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        new LegacyLoadProfileLogBooksCommandImpl(createGroupedDeviceCommand(device, deviceProtocol), null, null, comTaskExecution);
        // should have gotten an exception
    }

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        new LegacyLoadProfileLogBooksCommandImpl(null, mock(LoadProfilesTask.class), mock(LogBooksTask.class), comTaskExecution);
        // Was expecting a CodingException
    }

    @Test(expected = CodingException.class)
    public void offlineDeviceNullTest() {
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(null, deviceProtocol);
        new LegacyLoadProfileLogBooksCommandImpl(groupedDeviceCommand, mock(LoadProfilesTask.class), mock(LogBooksTask.class), comTaskExecution);
        // Was expecting a CodingException
    }

    @Test
    public void verifyLoadProfilesCommandNotNullTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(true);
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfilesTask, mock(LogBooksTask.class), comTaskExecution);

        // asserts
        assertNotNull(legacyCommand.getVerifyLoadProfilesCommand());
        assertNull(legacyCommand.getMarkIntervalsAsBadTimeCommand());
        assertNull(legacyCommand.getCreateMeterEventsFromStatusFlagsCommand());
        assertNull(legacyCommand.getTimeDifferenceCommand());
        assertNotNull(legacyCommand.getReadLegacyLoadProfileLogBooksDataCommand());
    }

    @Test
    public void createMeterEventsFromStatusFlagsNotNullTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.createMeterEventsFromStatusFlags()).thenReturn(true);
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfilesTask, mock(LogBooksTask.class), comTaskExecution);

        // asserts
        assertNotNull(legacyCommand.getVerifyLoadProfilesCommand());
        assertNull(legacyCommand.getMarkIntervalsAsBadTimeCommand());
        assertNotNull(legacyCommand.getCreateMeterEventsFromStatusFlagsCommand());
        assertNull(legacyCommand.getTimeDifferenceCommand());
        assertNotNull(legacyCommand.getReadLegacyLoadProfileLogBooksDataCommand());
    }

    @Test
    public void markIntervalsAsBadTimeNotNullTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.isMarkIntervalsAsBadTime()).thenReturn(true);
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfilesTask, mock(LogBooksTask.class), comTaskExecution);

        // asserts
        assertNotNull(legacyCommand.getVerifyLoadProfilesCommand());
        assertNotNull(legacyCommand.getMarkIntervalsAsBadTimeCommand());
        assertNull(legacyCommand.getCreateMeterEventsFromStatusFlagsCommand());
        assertNotNull(legacyCommand.getTimeDifferenceCommand());
        assertNotNull(legacyCommand.getReadLegacyLoadProfileLogBooksDataCommand());
    }

    @Test
    public void createLoadProfileReadersTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);

        OfflineLoadProfile offlineLoadProfile1 = mockOfflineLoadProfile(1);
        OfflineLoadProfile offlineLoadProfile2 = mockOfflineLoadProfile(2);
        OfflineLoadProfile offlineLoadProfile3 = mockOfflineLoadProfile(3);
        OfflineLoadProfile offlineLoadProfile4 = mockOfflineLoadProfile(4);

        OfflineLoadProfileChannel mockChannel = getMockChannel();
        when(offlineLoadProfile1.getOfflineChannels()).thenReturn(Collections.singletonList(mockChannel));
        when(offlineLoadProfile2.getOfflineChannels()).thenReturn(Collections.singletonList(mockChannel));
        when(offlineLoadProfile3.getOfflineChannels()).thenReturn(Collections.singletonList(mockChannel));
        when(offlineLoadProfile4.getOfflineChannels()).thenReturn(Collections.singletonList(mockChannel));
        when(offlineDevice.getAllOfflineLoadProfiles()).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        LegacyLoadProfileLogBooksCommandImpl legacyLoadProfileLogBooksCommand = new LegacyLoadProfileLogBooksCommandImpl(groupedDeviceCommand, loadProfilesTask, mock(LogBooksTask.class), comTaskExecution);
        groupedDeviceCommand.addCommand(legacyLoadProfileLogBooksCommand, comTaskExecution);

        // Asserts
        assertNotNull(legacyLoadProfileLogBooksCommand.getLoadProfileReaderMap());
        assertEquals("Expected 4 readers in the map", 4, legacyLoadProfileLogBooksCommand.getLoadProfileReaderMap().size());
        assertNotNull(legacyLoadProfileLogBooksCommand.getLoadProfileReaders());
        assertEquals("Expected 4 readers", 4, legacyLoadProfileLogBooksCommand.getLoadProfileReaders().size());
    }

    private OfflineLoadProfile mockOfflineLoadProfile(long loadProfileId) {
        OfflineLoadProfile offlineLoadProfile = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile.getLoadProfileId()).thenReturn(loadProfileId);
        return offlineLoadProfile;
    }

    @Test
    public void createLoadProfileReadersWithSpecificLoadProfileTypeTest() {
        final long loadProfileTypeId = 165;
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getId()).thenReturn(loadProfileTypeId);
        when(loadProfileType.getObisCode()).thenReturn(FIXED_LOAD_PROFILE_OBIS_CODE);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.getLoadProfileTypes()).thenReturn(Collections.singletonList(loadProfileType));
        CommandRoot commandRoot = createCommandRoot();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);

        OfflineLoadProfile offlineLoadProfile1 = mockOfflineLoadProfile(1);
        OfflineLoadProfile offlineLoadProfile2 = mockOfflineLoadProfile(2);
        when(offlineLoadProfile2.getLoadProfileTypeId()).thenReturn(loadProfileTypeId);
        OfflineLoadProfileChannel mockChannel = getMockChannel();
        when(offlineLoadProfile2.getOfflineChannels()).thenReturn(Collections.singletonList(mockChannel));
        OfflineLoadProfile offlineLoadProfile3 = mockOfflineLoadProfile(3);
        OfflineLoadProfile offlineLoadProfile4 = mockOfflineLoadProfile(4);
        when(offlineLoadProfile4.getLoadProfileTypeId()).thenReturn(loadProfileTypeId);
        when(offlineLoadProfile4.getOfflineChannels()).thenReturn(Collections.singletonList(mockChannel));
        when(offlineDevice.getAllOfflineLoadProfiles()).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfilesTask, mock(LogBooksTask.class), comTaskExecution);

        // Asserts
        assertNotNull(legacyCommand.getLoadProfileReaderMap());
        assertEquals("Expected 2 readers in the map", 2, legacyCommand.getLoadProfileReaderMap().size());
        assertNotNull(legacyCommand.getLoadProfileReaders());
        assertEquals("Expected 2 readers", 2, legacyCommand.getLoadProfileReaders().size());
    }

    private OfflineLoadProfileChannel getMockChannel() {
        return getMockChannel(ObisCode.fromString("1.x.128.1.0.255"));
    }

    private OfflineLoadProfileChannel getMockChannel(ObisCode obisCode) {
        OfflineLoadProfileChannel mockChannel = mock(OfflineLoadProfileChannel.class);
        when(mockChannel.isStoreData()).thenReturn(true);
        when(mockChannel.getObisCode()).thenReturn(obisCode);
        when(mockChannel.getUnit()).thenReturn(FIXED_CHANNEL_UNIT);
        when(mockChannel.getMasterSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        return mockChannel;
    }

    @Test
    public void removeIncorrectLoadProfileReadersTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);

        OfflineLoadProfile offlineLoadProfile1 = mockOfflineLoadProfile(1);
        OfflineLoadProfile offlineLoadProfile2 = mockOfflineLoadProfile(2);
        OfflineLoadProfile offlineLoadProfile3 = mockOfflineLoadProfile(3);
        OfflineLoadProfile offlineLoadProfile4 = mockOfflineLoadProfile(4);

        OfflineLoadProfileChannel mockChannel = getMockChannel();
        when(offlineLoadProfile1.getOfflineChannels()).thenReturn(Collections.singletonList(mockChannel));
        when(offlineLoadProfile2.getOfflineChannels()).thenReturn(Collections.singletonList(mockChannel));
        when(offlineLoadProfile3.getOfflineChannels()).thenReturn(Collections.singletonList(mockChannel));
        when(offlineLoadProfile4.getOfflineChannels()).thenReturn(Collections.singletonList(mockChannel));

        when(offlineDevice.getAllOfflineLoadProfiles()).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksCommand = getLegacyLoadProfileLogBooksCommand(mock(LogBooksTask.class), loadProfilesTask, groupedDeviceCommand);

        List<LoadProfileReader> loadProfileReaders = legacyLoadProfileLogBooksCommand.getLoadProfileReaders();
        List<LoadProfileReader> readersToRemove = new ArrayList<>(2);
        readersToRemove.add(loadProfileReaders.get(0));
        readersToRemove.add(loadProfileReaders.get(1));
        legacyLoadProfileLogBooksCommand.removeIncorrectLoadProfileReaders(readersToRemove);

        // Asserts
        assertEquals("Expected only two readers left", 2, legacyLoadProfileLogBooksCommand.getLoadProfileReaders().size());
    }

    @Test
    public void dontFindLoadProfileIntervalForLoadProfileReaderTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfilesTask, mock(LogBooksTask.class), comTaskExecution);
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);

        // Asserts
        assertEquals("Should not have found the LoadProfileReader", LoadProfileCommand.INVALID_LOAD_PROFILE_INTERVAL, legacyCommand.findLoadProfileIntervalForLoadProfileReader(loadProfileReader));
    }

    @Test
    public void findLoadProfileIntervalForLoadProfileReaderTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);

        OfflineLoadProfile offlineLoadProfile1 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile1.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL.asTemporalAmount());
        OfflineLoadProfile offlineLoadProfile2 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile2.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL.asTemporalAmount());
        OfflineLoadProfile offlineLoadProfile3 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile3.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL.asTemporalAmount());
        OfflineLoadProfile offlineLoadProfile4 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile4.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL.asTemporalAmount());
        when(offlineDevice.getAllOfflineLoadProfiles()).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfilesTask, mock(LogBooksTask.class), comTaskExecution);

        // Asserts
        int count = 0;
        for (LoadProfileReader loadProfileReader : legacyCommand.getLoadProfileReaders()) {
            assertEquals("Expected reader " + count++ + " to have a correct interval", FIXED_LOAD_PROFILE_INTERVAL.getSeconds(), legacyCommand.findLoadProfileIntervalForLoadProfileReader(loadProfileReader));
        }
    }

    @Test
    public void completeConstructionInCorrectOrderTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(true);
        when(loadProfilesTask.createMeterEventsFromStatusFlags()).thenReturn(true);
        when(loadProfilesTask.isMarkIntervalsAsBadTime()).thenReturn(true);
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        LegacyLoadProfileLogBooksCommand legacyCommand = groupedDeviceCommand.getLegacyLoadProfileLogBooksCommand(loadProfilesTask, mock(LogBooksTask.class), groupedDeviceCommand, comTaskExecution);

        // Asserts
        assertEquals("Expected 5 subCommands in the command list of the LoadProfileCommand", 5, legacyCommand.getCommands().size());
        assertEquals("The groupedDeviceCommand should only contain 1 command, the LoadProfileCommand", 1, groupedDeviceCommand.getComTaskRoot(comTaskExecution).getCommands().size());
        assertTrue("The first command should be the verifyLoadProfileCommand", legacyCommand.getCommands().values().toArray()[0] instanceof VerifyLoadProfilesCommandImpl);
        assertTrue("The second command should be the readLoadProfileDataCommand", legacyCommand.getCommands().values().toArray()[1] instanceof ReadLegacyLoadProfileLogBooksDataCommand);
        assertTrue("The third command should be the timeDifferenceCommand", legacyCommand.getCommands().values().toArray()[2] instanceof TimeDifferenceCommand);
        assertTrue("The fourth command should be the markIntervalsAsBadTimeCommand", legacyCommand.getCommands().values().toArray()[3] instanceof MarkIntervalsAsBadTimeCommand);
        assertTrue("The fifth command should be the createMeterEventsCommand", legacyCommand.getCommands().values().toArray()[4] instanceof CreateMeterEventsFromStatusFlagsCommand);
    }

    @Test
    public void commandTypeTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        OfflineDevice device = mock(OfflineDevice.class);

        // Business method
        LegacyLoadProfileLogBooksCommand logBooksCommand = new LegacyLoadProfileLogBooksCommandImpl(createGroupedDeviceCommand(device, deviceProtocol), mock(LoadProfilesTask.class), logBooksTask, comTaskExecution);

        // asserts
        assertEquals(ComCommandTypes.LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND, logBooksCommand.getCommandType());
    }

    @Test
    public void createLogBookReadersForEmptyLogBookTaskLogBookTypesTest() {
        TestSerialNumberDeviceIdentifier deviceIdentifier = SERIAL_NUMBER_DEVICE_IDENTIFIER;
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        when(logBooksTask.getLogBookTypes()).thenReturn(new ArrayList<>());  // No LogBookTypes are specified in the LogBooksTask

        OfflineDeviceImpl device = mock(OfflineDeviceImpl.class);
        when(device.getSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        List<OfflineLogBook> logBooksForDevice = Arrays.asList(offlineLogBook_A, offlineLogBook_B, offlineLogBook_C);
        when(device.getAllOfflineLogBooks()).thenReturn(logBooksForDevice);
        when(device.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        LegacyLoadProfileLogBooksCommand legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(createGroupedDeviceCommand(device, deviceProtocol), mock(LoadProfilesTask.class), logBooksTask, comTaskExecution);
        List<LogBookReader> logBookReaders = legacyCommand.getLogBookReaders();

        LogBookReader expectedLogBookReader_1 = new LogBookReader(DEVICE_OBISCODE_LOGBOOK_1, Date.from(LAST_LOGBOOK_1), new LogBookIdentifierById(LOGBOOK_ID_1, LOGBOOK1_OBIS, mock(DeviceIdentifier.class)), FIXED_DEVICE_SERIAL_NUMBER);
        LogBookReader expectedLogBookReader_2 = new LogBookReader(DEVICE_OBISCODE_LOGBOOK_2, Date.from(LAST_LOGBOOK_2), new LogBookIdentifierById(LOGBOOK_ID_2, LOGBOOK2_OBIS, mock(DeviceIdentifier.class)), FIXED_DEVICE_SERIAL_NUMBER);
        LogBookReader expectedLogBookReader_3 = new LogBookReader(DEVICE_OBISCODE_LOGBOOK_3, Date.from(LAST_LOGBOOK_3), new LogBookIdentifierById(LOGBOOK_ID_3, LOGBOOK3_OBIS, mock(DeviceIdentifier.class)), FIXED_DEVICE_SERIAL_NUMBER);

        // asserts
        assertEquals(ComCommandTypes.LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND, legacyCommand.getCommandType());
        assertEquals(logBooksForDevice.size(), logBookReaders.size());
        assertThat(logBookReaders.get(0)).isEqualsToByComparingFields(expectedLogBookReader_1);
        assertThat(logBookReaders.get(1)).isEqualsToByComparingFields(expectedLogBookReader_2);
        assertThat(logBookReaders.get(2)).isEqualsToByComparingFields(expectedLogBookReader_3);
    }

    @Test
    public void createLogBookReadersForSpecificLogBookTaskLogBookTypesTest() {
        TestSerialNumberDeviceIdentifier deviceIdentifier = SERIAL_NUMBER_DEVICE_IDENTIFIER;
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        when(logBooksTask.getLogBookTypes()).thenReturn(Arrays.asList(logBookType_A, logBookType_C));    // The logBookTypes are specified in the LogBooksTask

        OfflineDeviceImpl device = mock(OfflineDeviceImpl.class);
        when(device.getSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        List<OfflineLogBook> logBooksForDevice = Arrays.asList(offlineLogBook_A, offlineLogBook_B, offlineLogBook_C);
        when(device.getAllOfflineLogBooks()).thenReturn(logBooksForDevice);
        when(device.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        LegacyLoadProfileLogBooksCommand legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(createGroupedDeviceCommand(device, deviceProtocol), mock(LoadProfilesTask.class), logBooksTask, comTaskExecution);
        List<LogBookReader> logBookReaders = legacyCommand.getLogBookReaders();

        LogBookReader expectedLogBookReader_1 = new LogBookReader(DEVICE_OBISCODE_LOGBOOK_1, Date.from(LAST_LOGBOOK_1), new LogBookIdentifierById(LOGBOOK_ID_1, LOGBOOK1_OBIS, mock(DeviceIdentifier.class)), FIXED_DEVICE_SERIAL_NUMBER);
        LogBookReader expectedLogBookReader_3 = new LogBookReader(DEVICE_OBISCODE_LOGBOOK_3, Date.from(LAST_LOGBOOK_3), new LogBookIdentifierById(LOGBOOK_ID_3, LOGBOOK3_OBIS, mock(DeviceIdentifier.class)), FIXED_DEVICE_SERIAL_NUMBER);

        // asserts
        assertEquals(ComCommandTypes.LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND, legacyCommand.getCommandType());
        assertEquals(logBooksTask.getLogBookTypes().size(), logBookReaders.size());
        assertThat(logBookReaders.get(0)).isEqualsToByComparingFields(expectedLogBookReader_1);
        assertThat(logBookReaders.get(1)).isEqualsToByComparingFields(expectedLogBookReader_3);
    }

    @Test
    public void createWithOnlyLogbooksTaskTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        when(logBooksTask.getLogBookTypes()).thenReturn(Arrays.asList(logBookType_A, logBookType_C));    // The logBookTypes are specified in the LogBooksTask

        OfflineDeviceImpl device = mock(OfflineDeviceImpl.class);
        when(device.getSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        List<OfflineLogBook> logBooksForDevice = Arrays.asList(offlineLogBook_A, offlineLogBook_B, offlineLogBook_C);
        when(device.getAllOfflineLogBooks()).thenReturn(logBooksForDevice);
        when(device.getSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);

        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(device, deviceProtocol);
        LegacyLoadProfileLogBooksCommand legacyCommand = getLegacyLoadProfileLogBooksCommand(logBooksTask, null, groupedDeviceCommand);

        // asserts
        assertThat(legacyCommand.getLogBookReaders()).isNotNull();
        assertThat(legacyCommand.getLogBookReaders()).isNotEmpty();
        assertThat(legacyCommand.getLoadProfileReaders()).isNotNull();
        assertThat(legacyCommand.getLoadProfileReaders()).isEmpty();
        assertThat(legacyCommand.getReadLegacyLoadProfileLogBooksDataCommand()).isNotNull();
        assertThat(legacyCommand.getVerifyLoadProfilesCommand()).isNull();
        assertThat(legacyCommand.getMarkIntervalsAsBadTimeCommand()).isNull();
        assertThat(legacyCommand.getCreateMeterEventsFromStatusFlagsCommand()).isNull();
        assertThat(legacyCommand.getTimeDifferenceCommand()).isNull();
    }

    private LegacyLoadProfileLogBooksCommandImpl getLegacyLoadProfileLogBooksCommand(LogBooksTask logBooksTask, LoadProfilesTask loadProfilesTask, GroupedDeviceCommand groupedDeviceCommand) {
        LegacyLoadProfileLogBooksCommandImpl legacyLoadProfileLogBooksCommand = spy(new LegacyLoadProfileLogBooksCommandImpl(groupedDeviceCommand, loadProfilesTask, logBooksTask, comTaskExecution));
        groupedDeviceCommand.addCommand(legacyLoadProfileLogBooksCommand, comTaskExecution);
        return legacyLoadProfileLogBooksCommand;
    }

    @Test
    public void createWithOnlyLoadProfilesTaskTest() {
        final long loadProfileTypeId = 165;
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getId()).thenReturn(loadProfileTypeId);
        when(loadProfileType.getObisCode()).thenReturn(FIXED_LOAD_PROFILE_OBIS_CODE);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.getLoadProfileTypes()).thenReturn(Collections.singletonList(loadProfileType));
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);

        OfflineLoadProfileChannel mockChannel = getMockChannel();
        OfflineLoadProfile offlineLoadProfile = mockOfflineLoadProfile(4);
        when(offlineLoadProfile.getOfflineChannels()).thenReturn(Collections.singletonList(mockChannel));
        when(offlineLoadProfile.getLoadProfileTypeId()).thenReturn(loadProfileTypeId);
        when(offlineDevice.getAllOfflineLoadProfiles()).thenReturn(Collections.singletonList(offlineLoadProfile));
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = getLegacyLoadProfileLogBooksCommand(null, loadProfilesTask, groupedDeviceCommand);

        // asserts
        assertThat(legacyCommand.getLogBookReaders()).isNotNull();
        assertThat(legacyCommand.getLogBookReaders()).isEmpty();
        assertThat(legacyCommand.getLoadProfileReaders()).isNotNull();
        assertThat(legacyCommand.getLoadProfileReaders()).isNotEmpty();
        assertThat(legacyCommand.getReadLegacyLoadProfileLogBooksDataCommand()).isNotNull();
        assertThat(legacyCommand.getVerifyLoadProfilesCommand()).isNotNull();
        assertThat(legacyCommand.getCreateMeterEventsFromStatusFlagsCommand()).isNull();
        assertThat(legacyCommand.getMarkIntervalsAsBadTimeCommand()).isNull();
        assertThat(legacyCommand.getTimeDifferenceCommand()).isNull();
        verify(loadProfilesTask).createMeterEventsFromStatusFlags();
        verify(loadProfilesTask).isMarkIntervalsAsBadTime();
    }

}