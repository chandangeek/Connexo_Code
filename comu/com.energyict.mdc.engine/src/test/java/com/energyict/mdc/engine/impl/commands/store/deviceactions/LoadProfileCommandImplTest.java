package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.comserver.exceptions.CodingException;
import com.energyict.comserver.logging.LogLevel;
import com.energyict.comserver.time.FrozenClock;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.commands.CreateMeterEventsFromStatusFlagsCommand;
import com.energyict.mdc.commands.LoadProfileCommand;
import com.energyict.mdc.commands.MarkIntervalsAsBadTimeCommand;
import com.energyict.mdc.commands.ReadLoadProfileDataCommand;
import com.energyict.mdc.commands.TimeDifferenceCommand;
import com.energyict.mdc.protocol.tasks.LoadProfilesTask;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;
import com.energyict.mdc.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.test.MockEnvironmentTranslations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.comserver.commands.deviceactions.LoadProfileCommandImpl} component
 *
 * @author gna
 * @since 21/05/12 - 14:40
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadProfileCommandImplTest extends CommonCommandImplTests {

    @ClassRule
    public static TestRule mockEnvironmentTranslactions = new MockEnvironmentTranslations();

    @Mock
    ComTaskExecution comTaskExecution;

    private static final ObisCode FIXED_LOAD_PROFILE_OBIS_CODE = ObisCode.fromString("1.0.99.1.0.255");
    private static final TimeDuration FIXED_LOAD_PROFILE_INTERVAL = new TimeDuration(900);
    private static final FrozenClock LAST_READING = FrozenClock.currentTime();
    private static final int FIXED_DEVICE_ID = 123;
    private static final String FIXED_DEVICE_SERIAL_NUMBER = "FIXED_DEVICE_SERIAL_NUMBER";
    private static final int LOAD_PROFILE_TYPE_ID = 651;
    private static final Unit FIXED_CHANNEL_UNIT = Unit.get("kWh");

    private OfflineLoadProfile getMockedOfflineLoadProfile() {
        OfflineLoadProfile loadProfile = mock(OfflineLoadProfile.class);
        when(loadProfile.getLoadProfileTypeId()).thenReturn(LOAD_PROFILE_TYPE_ID);
        when(loadProfile.getLastReading()).thenReturn(LAST_READING.now());
        when(loadProfile.getDeviceId()).thenReturn(FIXED_DEVICE_ID);
        when(loadProfile.getObisCode()).thenReturn(FIXED_LOAD_PROFILE_OBIS_CODE);
        when(loadProfile.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL);
        when(loadProfile.getMasterSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        return loadProfile;
    }

    @Test(expected = CodingException.class)
    public void loadProfileTaskNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        new LoadProfileCommandImpl(null, device, createCommandRoot(), comTaskExecution);
        // should have gotten an exception
    }

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        new LoadProfileCommandImpl(mock(LoadProfilesTask.class), device, null, comTaskExecution);
        // Was expecting a CodingException
    }

    @Test(expected = CodingException.class)
    public void offlineDeviceNullTest() {
        new LoadProfileCommandImpl(mock(LoadProfilesTask.class), null, createCommandRoot(), comTaskExecution);
        // Was expecting a CodingException
    }

    @Test
    public void addLoadProfileToReaderListTest() {

        OfflineLoadProfile loadProfile = getMockedOfflineLoadProfile();

        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        CommandRoot commandRoot = createCommandRoot();
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, mock(OfflineDevice.class), commandRoot, comTaskExecution);
        loadProfileCommand.addLoadProfileToReaderList(loadProfile);

        // Asserts
        assertNotNull(loadProfileCommand.getLoadProfileReaders());
        assertEquals("Expect 1 element in the list", 1, loadProfileCommand.getLoadProfileReaders().size());
        LoadProfileReader loadProfileReader = loadProfileCommand.getLoadProfileReaders().get(0);
        Assert.assertEquals(FIXED_LOAD_PROFILE_OBIS_CODE, loadProfileReader.getProfileObisCode());
        Assert.assertEquals(FIXED_DEVICE_SERIAL_NUMBER, loadProfileReader.getMeterSerialNumber());
        Assert.assertEquals(LAST_READING.now(), loadProfileReader.getStartReadingTime());
        assertNotNull(loadProfileReader.getEndReadingTime());
    }

    @Test
    public void verifyLoadProfilesCommandNotNullTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(true);
        CommandRoot commandRoot = createCommandRoot();
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, mock(OfflineDevice.class), commandRoot, comTaskExecution);

        // asserts
        assertNotNull(loadProfileCommand.getVerifyLoadProfilesCommand());
        assertNull(loadProfileCommand.getMarkIntervalsAsBadTimeCommand());
        assertNull(loadProfileCommand.getCreateMeterEventsFromStatusFlagsCommand());
        assertNull(loadProfileCommand.getTimeDifferenceCommand());
        assertNotNull(loadProfileCommand.getReadLoadProfileDataCommand());
    }

    @Test
    public void createMeterEventsFromStatusFlagsNotNullTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.createMeterEventsFromStatusFlags()).thenReturn(true);
        CommandRoot commandRoot = createCommandRoot();
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, mock(OfflineDevice.class), commandRoot, comTaskExecution);

        // asserts
        assertNotNull(loadProfileCommand.getVerifyLoadProfilesCommand());
        assertNull(loadProfileCommand.getMarkIntervalsAsBadTimeCommand());
        assertNotNull(loadProfileCommand.getCreateMeterEventsFromStatusFlagsCommand());
        assertNull(loadProfileCommand.getTimeDifferenceCommand());
        assertNotNull(loadProfileCommand.getReadLoadProfileDataCommand());
    }

    @Test
    public void markIntervalsAsBadTimeNotNullTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.isMarkIntervalsAsBadTime()).thenReturn(true);
        CommandRoot commandRoot = createCommandRoot();
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, mock(OfflineDevice.class), commandRoot, comTaskExecution);

        // asserts
        assertNotNull(loadProfileCommand.getVerifyLoadProfilesCommand());
        assertNotNull(loadProfileCommand.getMarkIntervalsAsBadTimeCommand());
        assertNull(loadProfileCommand.getCreateMeterEventsFromStatusFlagsCommand());
        assertNotNull(loadProfileCommand.getTimeDifferenceCommand());
        assertNotNull(loadProfileCommand.getReadLoadProfileDataCommand());
    }

    @Test
    public void createLoadProfileReadersTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        CommandRoot commandRoot = createCommandRoot();

        OfflineLoadProfile offlineLoadProfile1 = mock(OfflineLoadProfile.class);
        OfflineLoadProfile offlineLoadProfile2 = mock(OfflineLoadProfile.class);
        OfflineLoadProfile offlineLoadProfile3 = mock(OfflineLoadProfile.class);
        OfflineLoadProfile offlineLoadProfile4 = mock(OfflineLoadProfile.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getAllOfflineLoadProfiles()).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        // Business method
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, offlineDevice, commandRoot, comTaskExecution);
        //loadProfileCommand.createLoadProfileReaders(offlineDevice);

        // Asserts
        assertNotNull(loadProfileCommand.getLoadProfileReaderMap());
        assertEquals("Expected 4 readers in the map", 4, loadProfileCommand.getLoadProfileReaderMap().size());
        assertNotNull(loadProfileCommand.getLoadProfileReaders());
        assertEquals("Expected 4 readers", 4, loadProfileCommand.getLoadProfileReaders().size());
    }

    @Test
    public void createLoadProfileReadersWithSpecificLoadProfileTypeTest() {
        final int loadProfileTypeId = 165;
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getId()).thenReturn(loadProfileTypeId);
        when(loadProfileType.getObisCode()).thenReturn(FIXED_LOAD_PROFILE_OBIS_CODE);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.getLoadProfileTypes()).thenReturn(Arrays.asList(loadProfileType));
        CommandRoot commandRoot = createCommandRoot();

        OfflineLoadProfile offlineLoadProfile1 = mock(OfflineLoadProfile.class);
        OfflineLoadProfile offlineLoadProfile2 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile2.getLoadProfileTypeId()).thenReturn(loadProfileTypeId);
        OfflineLoadProfile offlineLoadProfile3 = mock(OfflineLoadProfile.class);
        OfflineLoadProfile offlineLoadProfile4 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile4.getLoadProfileTypeId()).thenReturn(loadProfileTypeId);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getAllOfflineLoadProfiles()).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        // Business method
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, offlineDevice, commandRoot, comTaskExecution);
        //loadProfileCommand.createLoadProfileReaders(offlineDevice);

        // Asserts
        assertNotNull(loadProfileCommand.getLoadProfileReaderMap());
        assertEquals("Expected 2 readers in the map", 2, loadProfileCommand.getLoadProfileReaderMap().size());
        assertNotNull(loadProfileCommand.getLoadProfileReaders());
        assertEquals("Expected 2 readers", 2, loadProfileCommand.getLoadProfileReaders().size());
        assertEquals("LoadProfileCommandImpl {loadProfileObisCodes: 1.0.99.1.0.255; markAsBadTime: false; createEventsFromStatusFlag: false}", loadProfileCommand.toJournalMessageDescription(LogLevel.ERROR));
    }

    @Test
    public void removeIncorrectLoadProfileReadersTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        CommandRoot commandRoot = createCommandRoot();

        OfflineLoadProfile offlineLoadProfile1 = mock(OfflineLoadProfile.class);
        OfflineLoadProfile offlineLoadProfile2 = mock(OfflineLoadProfile.class);
        OfflineLoadProfile offlineLoadProfile3 = mock(OfflineLoadProfile.class);
        OfflineLoadProfile offlineLoadProfile4 = mock(OfflineLoadProfile.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getAllOfflineLoadProfiles()).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        // Business method
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, offlineDevice, commandRoot, comTaskExecution);
        //loadProfileCommand.createLoadProfileReaders(offlineDevice);

        List<LoadProfileReader> loadProfileReaders = loadProfileCommand.getLoadProfileReaders();
        List<LoadProfileReader> readersToRemove = new ArrayList<>(2);
        readersToRemove.add(loadProfileReaders.get(0));
        readersToRemove.add(loadProfileReaders.get(1));
        loadProfileCommand.removeIncorrectLoadProfileReaders(readersToRemove);

        // Asserts
        assertEquals("Expected only two readers left", 2, loadProfileCommand.getLoadProfileReaders().size());
    }

    @Test
    public void dontFindLoadProfileIntervalForLoadProfileReaderTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        CommandRoot commandRoot = createCommandRoot();
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, mock(OfflineDevice.class), commandRoot, comTaskExecution);
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);

        // Asserts
        assertEquals("Should not have found the LoadProfileReader", LoadProfileCommand.INVALID_LOAD_PROFILE_INTERVAL, loadProfileCommand.findLoadProfileIntervalForLoadProfileReader(loadProfileReader));
    }

    @Test
    public void findLoadProfileIntervalForLoadProfileReaderTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        CommandRoot commandRoot = createCommandRoot();

        OfflineLoadProfile offlineLoadProfile1 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile1.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL);
        OfflineLoadProfile offlineLoadProfile2 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile2.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL);
        OfflineLoadProfile offlineLoadProfile3 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile3.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL);
        OfflineLoadProfile offlineLoadProfile4 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile4.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getAllOfflineLoadProfiles()).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        // Business method
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, mock(OfflineDevice.class), commandRoot, comTaskExecution);
        //loadProfileCommand.createLoadProfileReaders(offlineDevice);

        // Asserts
        int count = 0;
        for (LoadProfileReader loadProfileReader : loadProfileCommand.getLoadProfileReaders()) {
            assertEquals("Expected reader " + count++ + " to have a correct interval", FIXED_LOAD_PROFILE_INTERVAL.getSeconds(), loadProfileCommand.findLoadProfileIntervalForLoadProfileReader(loadProfileReader));
        }
    }

    @Test
    public void createChannelInfosTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        CommandRoot commandRoot = createCommandRoot();
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, mock(OfflineDevice.class), commandRoot, comTaskExecution);

        ObisCode[] channelObisCodes = {ObisCode.fromString("1.0.1.8.1.255"), ObisCode.fromString("1.0.1.8.2.255"), ObisCode.fromString("1.0.1.8.3.255")};

        OfflineLoadProfileChannel loadProfileChannel1 = createMockedOfflineLoadProfileChannel(channelObisCodes[0]);
        OfflineLoadProfileChannel loadProfileChannel2 = createMockedOfflineLoadProfileChannel(channelObisCodes[1]);
        OfflineLoadProfileChannel loadProfileChannel3 = createMockedOfflineLoadProfileChannel(channelObisCodes[2]);
        OfflineLoadProfile offlineLoadProfile = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL);
        when(offlineLoadProfile.getAllChannels()).thenReturn(Arrays.asList(loadProfileChannel1, loadProfileChannel2, loadProfileChannel3));

        // Asserts
        List<ChannelInfo> channelInfos = loadProfileCommand.createChannelInfos(offlineLoadProfile);
        assertNotNull(channelInfos);
        assertEquals("Expected three channels", 3, channelInfos.size());

        int count = 0;
        for (ChannelInfo channelInfo : channelInfos) {
            Assert.assertEquals(channelInfo.getId(), count);
            Assert.assertEquals(channelInfo.getName(), channelObisCodes[count++].toString());
            Assert.assertEquals(channelInfo.getUnit(), FIXED_CHANNEL_UNIT);
            Assert.assertEquals(channelInfo.getMeterIdentifier(), FIXED_DEVICE_SERIAL_NUMBER);
        }
    }

    @Test
    public void completeConstructionInCorrectOrderTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(true);
        when(loadProfilesTask.createMeterEventsFromStatusFlags()).thenReturn(true);
        when(loadProfilesTask.isMarkIntervalsAsBadTime()).thenReturn(true);
        CommandRoot commandRoot = createCommandRoot();
        LoadProfileCommand loadProfileCommand = commandRoot.getLoadProfileCommand(loadProfilesTask, commandRoot, comTaskExecution);

        // Asserts
        Assert.assertEquals("Expected 5 subCommands in the command list of the LoadProfileCommand", 5, loadProfileCommand.getCommands().size());
        Assert.assertEquals("The commandRoot should only contain 1 command, the LoadProfileCommand", 1, commandRoot.getCommands().size());
        assertTrue("The first command should be the verifyLoadProfileCommand", loadProfileCommand.getCommands().values().toArray()[0] instanceof VerifyLoadProfilesCommandImpl);
        assertTrue("The second command should be the readLoadProfileDataCommand", loadProfileCommand.getCommands().values().toArray()[1] instanceof ReadLoadProfileDataCommand);
        assertTrue("The third command should be the timeDifferenceCommand", loadProfileCommand.getCommands().values().toArray()[2] instanceof TimeDifferenceCommand);
        assertTrue("The fourth command should be the markIntervalsAsBadTimeCommand", loadProfileCommand.getCommands().values().toArray()[3] instanceof MarkIntervalsAsBadTimeCommand);
        assertTrue("The fifth command should be the createMeterEventsCommand", loadProfileCommand.getCommands().values().toArray()[4] instanceof CreateMeterEventsFromStatusFlagsCommand);
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
