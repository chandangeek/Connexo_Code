package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import com.energyict.mdc.common.ObisCode;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
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
import com.energyict.mdc.engine.impl.commands.collect.VerifyLoadProfilesCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.device.data.impl.identifiers.LogBookIdentifierById;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


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
    private Device device;

    private static final ObisCode FIXED_LOAD_PROFILE_OBIS_CODE = ObisCode.fromString("1.0.99.1.0.255");
    private static final TimeDuration FIXED_LOAD_PROFILE_INTERVAL = new TimeDuration(900);
    private static final Clock LAST_READING = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private static final long FIXED_DEVICE_ID = 123;
    private static final String FIXED_DEVICE_SERIAL_NUMBER = "FIXED_DEVICE_SERIAL_NUMBER";
    private final TestSerialNumberDeviceIdentifier serialNumberDeviceIdentifier = new TestSerialNumberDeviceIdentifier(FIXED_DEVICE_SERIAL_NUMBER);
    private static final long LOAD_PROFILE_TYPE_ID = 651;
    private static final Unit FIXED_CHANNEL_UNIT = Unit.get("kWh");

    @Mock
    private LogBookService logBookService;

    private Clock clock = Clock.systemUTC();

    private OfflineLoadProfile getMockedOfflineLoadProfile() {
        OfflineLoadProfile loadProfile = mock(OfflineLoadProfile.class);
        when(loadProfile.getLoadProfileTypeId()).thenReturn(LOAD_PROFILE_TYPE_ID);
        when(loadProfile.getLastReading()).thenReturn(Optional.of(LAST_READING.instant()));
        when(loadProfile.getDeviceId()).thenReturn(FIXED_DEVICE_ID);
        when(loadProfile.getObisCode()).thenReturn(FIXED_LOAD_PROFILE_OBIS_CODE);
        when(loadProfile.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL);
        when(loadProfile.getMasterSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        when(loadProfile.getDeviceIdentifier()).thenReturn(serialNumberDeviceIdentifier);
        return loadProfile;
    }

    @Before
    public void setUp() throws Exception {
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(device.getmRID()).thenReturn(MY_MRID);
        when(offlineLogBook_A.getLogBookId()).thenReturn(LOGBOOK_ID_1);
        when(offlineLogBook_A.getLogBookIdentifier()).thenReturn(new LogBookIdentifierById(LOGBOOK_ID_1, logBookService));
        when(offlineLogBook_A.getLastLogBook()).thenReturn(Optional.of(LAST_LOGBOOK_1));
        when(offlineLogBook_A.getMasterSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        when(offlineLogBook_A.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_1);
        when(offlineLogBook_A.getObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_1);
        when(offlineLogBook_A.getDeviceIdentifier()).thenReturn(serialNumberDeviceIdentifier);

        when(offlineLogBook_B.getLogBookId()).thenReturn(LOGBOOK_ID_2);
        when(offlineLogBook_B.getLogBookIdentifier()).thenReturn(new LogBookIdentifierById(LOGBOOK_ID_2, logBookService));
        when(offlineLogBook_B.getLastLogBook()).thenReturn(Optional.of(LAST_LOGBOOK_2));
        when(offlineLogBook_B.getMasterSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        when(offlineLogBook_B.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_2);
        when(offlineLogBook_B.getObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_2);
        when(offlineLogBook_B.getDeviceIdentifier()).thenReturn(serialNumberDeviceIdentifier);

        when(offlineLogBook_C.getLogBookId()).thenReturn(LOGBOOK_ID_3);
        when(offlineLogBook_C.getLogBookIdentifier()).thenReturn(new LogBookIdentifierById(LOGBOOK_ID_3, logBookService));
        when(offlineLogBook_C.getLastLogBook()).thenReturn(Optional.of(LAST_LOGBOOK_3));
        when(offlineLogBook_C.getMasterSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        when(offlineLogBook_C.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_3);
        when(offlineLogBook_C.getObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_3);
        when(offlineLogBook_C.getDeviceIdentifier()).thenReturn(serialNumberDeviceIdentifier);

        when(logBookType_A.getId()).thenReturn(LOGBOOK_TYPE_1);
        when(logBookType_B.getId()).thenReturn(LOGBOOK_TYPE_2);
        when(logBookType_C.getId()).thenReturn(LOGBOOK_TYPE_3);
    }

    @Test(expected = CodingException.class)
    public void loadProfileTaskLogBooksTaskNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        new LegacyLoadProfileLogBooksCommandImpl(null, null, device, createCommandRoot(), comTaskExecution);
        // should have gotten an exception
    }

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        new LegacyLoadProfileLogBooksCommandImpl(mock(LoadProfilesTask.class), mock(LogBooksTask.class), device, null, comTaskExecution);
        // Was expecting a CodingException
    }

    @Test(expected = CodingException.class)
    public void offlineDeviceNullTest() {
        new LegacyLoadProfileLogBooksCommandImpl(mock(LoadProfilesTask.class), mock(LogBooksTask.class), null, createCommandRoot(), comTaskExecution);
        // Was expecting a CodingException
    }

    @Test
    public void addLoadProfileToReaderListTest() {
        OfflineLoadProfile loadProfile = getMockedOfflineLoadProfile();

        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        CommandRoot commandRoot = createCommandRoot();
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(loadProfilesTask, mock(LogBooksTask.class), mock(OfflineDevice.class), commandRoot, comTaskExecution);
        legacyCommand.addLoadProfileToReaderList(loadProfile);

        // Asserts
        assertNotNull(legacyCommand.getLoadProfileReaders());
        assertEquals("Expect 1 element in the list", 1, legacyCommand.getLoadProfileReaders().size());
        LoadProfileReader loadProfileReader = legacyCommand.getLoadProfileReaders().get(0);
        assertEquals(FIXED_LOAD_PROFILE_OBIS_CODE, loadProfileReader.getProfileObisCode());
        assertEquals(FIXED_DEVICE_SERIAL_NUMBER, loadProfileReader.getDeviceIdentifier().getIdentifier());
        assertEquals(LAST_READING.instant(), loadProfileReader.getStartReadingTime());
        assertNotNull(loadProfileReader.getEndReadingTime());
    }

    @Test
    public void verifyLoadProfilesCommandNotNullTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(true);
        CommandRoot commandRoot = createCommandRoot();
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(loadProfilesTask, mock(LogBooksTask.class), mock(OfflineDevice.class), commandRoot, comTaskExecution);

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
        CommandRoot commandRoot = createCommandRoot();
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(loadProfilesTask, mock(LogBooksTask.class), mock(OfflineDevice.class), commandRoot, comTaskExecution);

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
        CommandRoot commandRoot = createCommandRoot();
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(loadProfilesTask, mock(LogBooksTask.class), mock(OfflineDevice.class), commandRoot, comTaskExecution);

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
        CommandRoot commandRoot = createCommandRoot();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(loadProfilesTask, mock(LogBooksTask.class), offlineDevice, commandRoot, comTaskExecution);

        OfflineLoadProfile offlineLoadProfile1 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile1.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile2 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile2.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile3 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile3.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile4 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile4.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(offlineDevice.getAllOfflineLoadProfilesForMRID(MY_MRID)).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        legacyCommand.createLoadProfileReaders(comTaskExecution.getDevice().getmRID());

        // Asserts
        assertNotNull(legacyCommand.getLoadProfileReaderMap());
        assertEquals("Expected 4 readers in the map", 4, legacyCommand.getLoadProfileReaderMap().size());
        assertNotNull(legacyCommand.getLoadProfileReaders());
        assertEquals("Expected 4 readers", 4, legacyCommand.getLoadProfileReaders().size());
    }

    @Test
    public void createLoadProfileReadersWithSpecificLoadProfileTypeTest() {
        final long loadProfileTypeId = 165;
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getId()).thenReturn(loadProfileTypeId);
        when(loadProfileType.getObisCode()).thenReturn(FIXED_LOAD_PROFILE_OBIS_CODE);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.getLoadProfileTypes()).thenReturn(Arrays.asList(loadProfileType));
        CommandRoot commandRoot = createCommandRoot();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(loadProfilesTask, mock(LogBooksTask.class), offlineDevice, commandRoot, comTaskExecution);

        OfflineLoadProfile offlineLoadProfile1 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile1.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile2 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile2.getLoadProfileTypeId()).thenReturn(loadProfileTypeId);
        when(offlineLoadProfile2.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile3 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile3.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile4 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile4.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(offlineLoadProfile4.getLoadProfileTypeId()).thenReturn(loadProfileTypeId);
        when(offlineDevice.getAllOfflineLoadProfilesForMRID(MY_MRID)).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        legacyCommand.createLoadProfileReaders(comTaskExecution.getDevice().getmRID());

        // Asserts
        assertNotNull(legacyCommand.getLoadProfileReaderMap());
        assertEquals("Expected 2 readers in the map", 2, legacyCommand.getLoadProfileReaderMap().size());
        assertNotNull(legacyCommand.getLoadProfileReaders());
        assertEquals("Expected 2 readers", 2, legacyCommand.getLoadProfileReaders().size());
    }

    @Test
    public void removeIncorrectLoadProfileReadersTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        CommandRoot commandRoot = createCommandRoot();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);

        OfflineLoadProfile offlineLoadProfile1 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile1.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile2 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile2.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile3 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile3.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile4 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile4.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(offlineDevice.getAllOfflineLoadProfilesForMRID(MY_MRID)).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(loadProfilesTask, mock(LogBooksTask.class), offlineDevice, commandRoot, comTaskExecution);

        List<LoadProfileReader> loadProfileReaders = legacyCommand.getLoadProfileReaders();
        List<LoadProfileReader> readersToRemove = new ArrayList<>(2);
        readersToRemove.add(loadProfileReaders.get(0));
        readersToRemove.add(loadProfileReaders.get(1));
        legacyCommand.removeIncorrectLoadProfileReaders(readersToRemove);

        // Asserts
        assertEquals("Expected only two readers left", 2, legacyCommand.getLoadProfileReaders().size());
    }

    @Test
    public void dontFindLoadProfileIntervalForLoadProfileReaderTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        CommandRoot commandRoot = createCommandRoot();
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(loadProfilesTask, mock(LogBooksTask.class), mock(OfflineDevice.class), commandRoot, comTaskExecution);
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);

        // Asserts
        assertEquals("Should not have found the LoadProfileReader", LoadProfileCommand.INVALID_LOAD_PROFILE_INTERVAL, legacyCommand.findLoadProfileIntervalForLoadProfileReader(loadProfileReader));
    }

    @Test
    public void findLoadProfileIntervalForLoadProfileReaderTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        CommandRoot commandRoot = createCommandRoot();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(loadProfilesTask, mock(LogBooksTask.class), offlineDevice, commandRoot, comTaskExecution);

        OfflineLoadProfile offlineLoadProfile1 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile1.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(offlineLoadProfile1.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL);
        OfflineLoadProfile offlineLoadProfile2 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile2.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(offlineLoadProfile2.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL);
        OfflineLoadProfile offlineLoadProfile3 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile3.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(offlineLoadProfile3.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL);
        OfflineLoadProfile offlineLoadProfile4 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile4.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(offlineLoadProfile4.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL);
        when(offlineDevice.getAllOfflineLoadProfiles()).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        legacyCommand.createLoadProfileReaders(comTaskExecution.getDevice().getmRID());

        // Asserts
        int count = 0;
        for (LoadProfileReader loadProfileReader : legacyCommand.getLoadProfileReaders()) {
            assertEquals("Expected reader " + count++ + " to have a correct interval", FIXED_LOAD_PROFILE_INTERVAL.getSeconds(), legacyCommand.findLoadProfileIntervalForLoadProfileReader(loadProfileReader));
        }
    }

    @Test
    public void createChannelInfosTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        CommandRoot commandRoot = createCommandRoot();
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(loadProfilesTask, mock(LogBooksTask.class), mock(OfflineDevice.class), commandRoot, comTaskExecution);

        ObisCode[] channelObisCodes = {ObisCode.fromString("1.0.1.8.1.255"), ObisCode.fromString("1.0.1.8.2.255"), ObisCode.fromString("1.0.1.8.3.255")};

        OfflineLoadProfileChannel loadProfileChannel1 = createMockedOfflineLoadProfileChannel(channelObisCodes[0]);
        OfflineLoadProfileChannel loadProfileChannel2 = createMockedOfflineLoadProfileChannel(channelObisCodes[1]);
        OfflineLoadProfileChannel loadProfileChannel3 = createMockedOfflineLoadProfileChannel(channelObisCodes[2]);
        OfflineLoadProfile offlineLoadProfile = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL);
        when(offlineLoadProfile.getAllChannels()).thenReturn(Arrays.asList(loadProfileChannel1, loadProfileChannel2, loadProfileChannel3));

        // Asserts
        List<ChannelInfo> channelInfos = legacyCommand.createChannelInfos(offlineLoadProfile);
        assertNotNull(channelInfos);
        assertEquals("Expected three channels", 3, channelInfos.size());

        int count = 0;
        for (ChannelInfo channelInfo : channelInfos) {
            assertEquals(channelInfo.getId(), count);
            assertEquals(channelInfo.getName(), channelObisCodes[count++].toString());
            assertEquals(channelInfo.getUnit(), FIXED_CHANNEL_UNIT);
            assertEquals(channelInfo.getMeterIdentifier(), FIXED_DEVICE_SERIAL_NUMBER);
        }
    }

    @Test
    public void completeConstructionInCorrectOrderTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(true);
        when(loadProfilesTask.createMeterEventsFromStatusFlags()).thenReturn(true);
        when(loadProfilesTask.isMarkIntervalsAsBadTime()).thenReturn(true);
        CommandRoot commandRoot = createCommandRoot();
        LegacyLoadProfileLogBooksCommand legacyCommand = commandRoot.getLegacyLoadProfileLogBooksCommand(loadProfilesTask, mock(LogBooksTask.class), commandRoot, comTaskExecution);

        // Asserts
        assertEquals("Expected 5 subCommands in the command list of the LoadProfileCommand", 5, legacyCommand.getCommands().size());
        assertEquals("The commandRoot should only contain 1 command, the LoadProfileCommand", 1, commandRoot.getCommands().size());
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
        CommandRoot commandRoot = mock(CommandRoot.class);

        // Business method
        LegacyLoadProfileLogBooksCommand logBooksCommand = new LegacyLoadProfileLogBooksCommandImpl(mock(LoadProfilesTask.class), logBooksTask, device, commandRoot, comTaskExecution);

        // asserts
        assertEquals(ComCommandTypes.LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND, logBooksCommand.getCommandType());
        assertNotNull(logBooksCommand.getLogBooksTask());
    }

    @Test
    public void createLogBookReadersForEmptyLogBookTaskLogBookTypesTest() {
        TestSerialNumberDeviceIdentifier deviceIdentifier = serialNumberDeviceIdentifier;
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        when(logBooksTask.getLogBookTypes()).thenReturn(new ArrayList<LogBookType>());  // No LogBookTypes are specified in the LogBooksTask

        OfflineDevice device = mock(OfflineDevice.class);
        when(device.getSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        List<OfflineLogBook> logBooksForDevice = Arrays.asList(offlineLogBook_A, offlineLogBook_B, offlineLogBook_C);
        when(device.getAllOfflineLogBooksForMRID(MY_MRID)).thenReturn(logBooksForDevice);
        when(device.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        CommandRoot commandRoot = mock(CommandRoot.class);
        CommandRoot.ServiceProvider serviceProvider = mock(CommandRoot.ServiceProvider.class);
        when(commandRoot.getServiceProvider()).thenReturn(serviceProvider);
        LegacyLoadProfileLogBooksCommand legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(mock(LoadProfilesTask.class), logBooksTask, device, commandRoot, comTaskExecution);
        List<LogBookReader> logBookReaders = legacyCommand.getLogBookReaders();

        LogBookReader expectedLogBookReader_1 = new LogBookReader(this.clock, DEVICE_OBISCODE_LOGBOOK_1, Optional.of(LAST_LOGBOOK_1), new LogBookIdentifierById(LOGBOOK_ID_1, logBookService), deviceIdentifier);
        LogBookReader expectedLogBookReader_2 = new LogBookReader(this.clock, DEVICE_OBISCODE_LOGBOOK_2, Optional.of(LAST_LOGBOOK_2), new LogBookIdentifierById(LOGBOOK_ID_2, logBookService), deviceIdentifier);
        LogBookReader expectedLogBookReader_3 = new LogBookReader(this.clock, DEVICE_OBISCODE_LOGBOOK_3, Optional.of(LAST_LOGBOOK_3), new LogBookIdentifierById(LOGBOOK_ID_3, logBookService), deviceIdentifier);

        // asserts
        assertEquals(ComCommandTypes.LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND, legacyCommand.getCommandType());
        assertNotNull(legacyCommand.getLogBooksTask());
        assertEquals(logBooksForDevice.size(), logBookReaders.size());
        assertThat(logBookReaders.get(0)).isEqualsToByComparingFields(expectedLogBookReader_1);
        assertThat(logBookReaders.get(1)).isEqualsToByComparingFields(expectedLogBookReader_2);
        assertThat(logBookReaders.get(2)).isEqualsToByComparingFields(expectedLogBookReader_3);
    }

    @Test
    public void createLogBookReadersForSpecificLogBookTaskLogBookTypesTest() {
        TestSerialNumberDeviceIdentifier deviceIdentifier = serialNumberDeviceIdentifier;
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        when(logBooksTask.getLogBookTypes()).thenReturn(Arrays.asList(logBookType_A, logBookType_C));    // The logBookTypes are specified in the LogBooksTask

        OfflineDevice device = mock(OfflineDevice.class);
        when(device.getSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        List<OfflineLogBook> logBooksForDevice = Arrays.asList(offlineLogBook_A, offlineLogBook_B, offlineLogBook_C);
        when(device.getAllOfflineLogBooksForMRID(MY_MRID)).thenReturn(logBooksForDevice);
        when(device.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        CommandRoot commandRoot = mock(CommandRoot.class);
        CommandRoot.ServiceProvider serviceProvider = mock(CommandRoot.ServiceProvider.class);
        when(commandRoot.getServiceProvider()).thenReturn(serviceProvider);
        LegacyLoadProfileLogBooksCommand legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(mock(LoadProfilesTask.class), logBooksTask, device, commandRoot, comTaskExecution);
        List<LogBookReader> logBookReaders = legacyCommand.getLogBookReaders();

        LogBookReader expectedLogBookReader_1 = new LogBookReader(this.clock, DEVICE_OBISCODE_LOGBOOK_1, Optional.of(LAST_LOGBOOK_1), new LogBookIdentifierById(LOGBOOK_ID_1, logBookService), deviceIdentifier);
        LogBookReader expectedLogBookReader_3 = new LogBookReader(this.clock, DEVICE_OBISCODE_LOGBOOK_3, Optional.of(LAST_LOGBOOK_3), new LogBookIdentifierById(LOGBOOK_ID_3, logBookService), deviceIdentifier);

        // asserts
        assertEquals(ComCommandTypes.LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND, legacyCommand.getCommandType());
        assertNotNull(legacyCommand.getLogBooksTask());
        assertEquals(logBooksTask.getLogBookTypes().size(), logBookReaders.size());
        assertThat(logBookReaders.get(0)).isEqualsToByComparingFields(expectedLogBookReader_1);
        assertThat(logBookReaders.get(1)).isEqualsToByComparingFields(expectedLogBookReader_3);
    }

    @Test
    public void createWithOnlyLogbooksTaskTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        when(logBooksTask.getLogBookTypes()).thenReturn(Arrays.asList(logBookType_A, logBookType_C));    // The logBookTypes are specified in the LogBooksTask

        OfflineDevice device = mock(OfflineDevice.class);
        when(device.getSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        List<OfflineLogBook> logBooksForDevice = Arrays.asList(offlineLogBook_A, offlineLogBook_B, offlineLogBook_C);
        when(device.getAllOfflineLogBooksForMRID(MY_MRID)).thenReturn(logBooksForDevice);
        when(device.getSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);

        CommandRoot commandRoot = mock(CommandRoot.class);
        CommandRoot.ServiceProvider serviceProvider = mock(CommandRoot.ServiceProvider.class);
        when(commandRoot.getServiceProvider()).thenReturn(serviceProvider);
        ReadLegacyLoadProfileLogBooksDataCommand readLegacyLoadProfileLogBooksDataCommand = mock(ReadLegacyLoadProfileLogBooksDataCommand.class);
        when(commandRoot.getReadLegacyLoadProfileLogBooksDataCommand(any(LegacyLoadProfileLogBooksCommand.class), any(ComTaskExecution.class))).thenReturn(readLegacyLoadProfileLogBooksDataCommand);
        LegacyLoadProfileLogBooksCommand legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(null, logBooksTask, device, commandRoot, comTaskExecution);

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

    @Test
    public void createWithOnlyLoadProfilesTaskTest() {
        final long loadProfileTypeId = 165;
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getId()).thenReturn(loadProfileTypeId);
        when(loadProfileType.getObisCode()).thenReturn(FIXED_LOAD_PROFILE_OBIS_CODE);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.getLoadProfileTypes()).thenReturn(Arrays.asList(loadProfileType));
        CommandRoot commandRoot = mock(CommandRoot.class);
        when(commandRoot.getServiceProvider()).thenReturn(commandRootServiceProvider);

        ReadLegacyLoadProfileLogBooksDataCommand readLegacyLoadProfileLogBooksDataCommand = mock(ReadLegacyLoadProfileLogBooksDataCommand.class);
        when(commandRoot.getReadLegacyLoadProfileLogBooksDataCommand(any(LegacyLoadProfileLogBooksCommand.class), any(ComTaskExecution.class))).thenReturn(readLegacyLoadProfileLogBooksDataCommand);

        VerifyLoadProfilesCommand verifyLoadProfilesCommand = mock(VerifyLoadProfilesCommand.class);
        when(commandRoot.getVerifyLoadProfileCommand(any(LegacyLoadProfileLogBooksCommand.class), any(ComTaskExecution.class))).thenReturn(verifyLoadProfilesCommand);

        MarkIntervalsAsBadTimeCommand markIntervalsAsBadTimeCommand = mock(MarkIntervalsAsBadTimeCommand.class);
        when(commandRoot.getMarkIntervalsAsBadTimeCommand(any(LegacyLoadProfileLogBooksCommand.class), any(ComTaskExecution.class))).thenReturn(markIntervalsAsBadTimeCommand);

        CreateMeterEventsFromStatusFlagsCommand createMeterEventsFromStatusFlagsCommand = mock(CreateMeterEventsFromStatusFlagsCommand.class);
        when(commandRoot.getCreateMeterEventsFromStatusFlagsCommand(any(LegacyLoadProfileLogBooksCommand.class), any(ComTaskExecution.class))).thenReturn(createMeterEventsFromStatusFlagsCommand);

        TimeDifferenceCommand timeDifferenceCommand = mock(TimeDifferenceCommand.class);
        when(commandRoot.getTimeDifferenceCommand(any(LegacyLoadProfileLogBooksCommand.class), any(ComTaskExecution.class))).thenReturn(timeDifferenceCommand);

        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        OfflineLoadProfile offlineLoadProfile = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(offlineLoadProfile.getLoadProfileTypeId()).thenReturn(loadProfileTypeId);
        when(offlineDevice.getAllOfflineLoadProfilesForMRID(MY_MRID)).thenReturn(Arrays.asList(offlineLoadProfile));
        LegacyLoadProfileLogBooksCommandImpl legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(loadProfilesTask, null, offlineDevice, commandRoot, comTaskExecution);

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

    private static OfflineLoadProfileChannel createMockedOfflineLoadProfileChannel(final ObisCode obisCode) {
        OfflineLoadProfileChannel loadProfileChannel = mock(OfflineLoadProfileChannel.class);
        when(loadProfileChannel.getMasterSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        when(loadProfileChannel.getObisCode()).thenReturn(obisCode);
        when(loadProfileChannel.getUnit()).thenReturn(FIXED_CHANNEL_UNIT);
        when(loadProfileChannel.isStoreData()).thenReturn(true);
        return loadProfileChannel;
    }

}