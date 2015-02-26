package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.ObisCode;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.TestSerialNumberDeviceIdentifier;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.CreateMeterEventsFromStatusFlagsCommand;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.MarkIntervalsAsBadTimeCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLoadProfileDataCommand;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;
import com.energyict.mdc.tasks.LoadProfilesTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the LoadProfileCommandImpl component
 *
 * @author gna
 * @since 21/05/12 - 14:40
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadProfileCommandImplTest extends CommonCommandImplTests {

    private final static String MY_MRID = "MyMrid";
    @Mock
    ComTaskExecution comTaskExecution;
    @Mock
    private Device device;

    private static final ObisCode FIXED_LOAD_PROFILE_OBIS_CODE = ObisCode.fromString("1.0.99.1.0.255");
    private static final TimeDuration FIXED_LOAD_PROFILE_INTERVAL = new TimeDuration(900);
    private static final Instant LAST_READING = Instant.now();
    private static final long FIXED_DEVICE_ID = 123;
    private static final String FIXED_DEVICE_SERIAL_NUMBER = "FIXED_DEVICE_SERIAL_NUMBER";
    private static final long LOAD_PROFILE_TYPE_ID = 651;
    private static final Unit FIXED_CHANNEL_UNIT = Unit.get("kWh");

    @Before
    public void initBefore() {
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(device.getmRID()).thenReturn(MY_MRID);
    }

    private OfflineLoadProfile getMockedOfflineLoadProfile() {
        OfflineLoadProfile loadProfile = mock(OfflineLoadProfile.class);
        when(loadProfile.getLoadProfileTypeId()).thenReturn(LOAD_PROFILE_TYPE_ID);
        when(loadProfile.getLastReading()).thenReturn(Optional.of(LAST_READING));
        when(loadProfile.getDeviceId()).thenReturn(FIXED_DEVICE_ID);
        when(loadProfile.getObisCode()).thenReturn(FIXED_LOAD_PROFILE_OBIS_CODE);
        when(loadProfile.getInterval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL);
        when(loadProfile.getMasterSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        when(loadProfile.getDeviceIdentifier()).thenReturn(new TestSerialNumberDeviceIdentifier(FIXED_DEVICE_SERIAL_NUMBER));
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
        assertThat(loadProfileCommand.getLoadProfileReaders()).isNotNull();
        assertThat(loadProfileCommand.getLoadProfileReaders()).hasSize(1);
        LoadProfileReader loadProfileReader = loadProfileCommand.getLoadProfileReaders().get(0);
        assertThat(loadProfileReader.getProfileObisCode()).isEqualTo(FIXED_LOAD_PROFILE_OBIS_CODE);
        assertThat(loadProfileReader.getDeviceIdentifier().getIdentifier()).isEqualTo(FIXED_DEVICE_SERIAL_NUMBER);
        assertThat(loadProfileReader.getStartReadingTime()).isEqualTo(LAST_READING);
        assertThat(loadProfileReader.getEndReadingTime()).isNotNull();
    }

    @Test
    public void verifyLoadProfilesCommandNotNullTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(true);
        CommandRoot commandRoot = createCommandRoot();
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, mock(OfflineDevice.class), commandRoot, comTaskExecution);

        // asserts
        assertThat(loadProfileCommand.getVerifyLoadProfilesCommand()).isNotNull();
        assertThat(loadProfileCommand.getMarkIntervalsAsBadTimeCommand()).isNull();
        assertThat(loadProfileCommand.getCreateMeterEventsFromStatusFlagsCommand()).isNull();
        assertThat(loadProfileCommand.getTimeDifferenceCommand()).isNull();
        assertThat(loadProfileCommand.getReadLoadProfileDataCommand()).isNotNull();
    }

    @Test
    public void createMeterEventsFromStatusFlagsNotNullTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.createMeterEventsFromStatusFlags()).thenReturn(true);
        CommandRoot commandRoot = createCommandRoot();
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, mock(OfflineDevice.class), commandRoot, comTaskExecution);

        // asserts
        assertThat(loadProfileCommand.getVerifyLoadProfilesCommand()).isNotNull();
        assertThat(loadProfileCommand.getMarkIntervalsAsBadTimeCommand()).isNull();
        assertThat(loadProfileCommand.getCreateMeterEventsFromStatusFlagsCommand()).isNotNull();
        assertThat(loadProfileCommand.getTimeDifferenceCommand()).isNull();
        assertThat(loadProfileCommand.getReadLoadProfileDataCommand()).isNotNull();
    }

    @Test
    public void markIntervalsAsBadTimeNotNullTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.isMarkIntervalsAsBadTime()).thenReturn(true);
        CommandRoot commandRoot = createCommandRoot();
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, mock(OfflineDevice.class), commandRoot, comTaskExecution);

        // asserts
        assertThat(loadProfileCommand.getVerifyLoadProfilesCommand()).isNotNull();
        assertThat(loadProfileCommand.getMarkIntervalsAsBadTimeCommand()).isNotNull();
        assertThat(loadProfileCommand.getCreateMeterEventsFromStatusFlagsCommand()).isNull();
        assertThat(loadProfileCommand.getTimeDifferenceCommand()).isNotNull();
        assertThat(loadProfileCommand.getReadLoadProfileDataCommand()).isNotNull();
    }

    @Test
    public void createLoadProfileReadersTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        CommandRoot commandRoot = createCommandRoot();

        OfflineLoadProfile offlineLoadProfile1 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile1.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile2 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile2.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile3 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile3.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile4 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile4.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getAllOfflineLoadProfilesForMRID(MY_MRID)).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        // Business method
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, offlineDevice, commandRoot, comTaskExecution);
        //loadProfileCommand.createLoadProfileReaders(offlineDevice);

        // Asserts
        assertThat(loadProfileCommand.getLoadProfileReaders()).isNotNull();
        assertThat(loadProfileCommand.getLoadProfileReaders()).hasSize(4);
        assertThat(loadProfileCommand.getLoadProfileReaders()).isNotNull();
        assertThat(loadProfileCommand.getLoadProfileReaders()).hasSize(4);
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

        OfflineLoadProfile offlineLoadProfile1 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile1.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile2 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile2.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(offlineLoadProfile2.getLoadProfileTypeId()).thenReturn(loadProfileTypeId);
        OfflineLoadProfile offlineLoadProfile3 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile3.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile4 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile4.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(offlineLoadProfile4.getLoadProfileTypeId()).thenReturn(loadProfileTypeId);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getAllOfflineLoadProfilesForMRID(MY_MRID)).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        // Business method
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, offlineDevice, commandRoot, comTaskExecution);

        // Asserts
        assertThat(loadProfileCommand.getLoadProfileReaders()).isNotNull();
        assertThat(loadProfileCommand.getLoadProfileReaders()).hasSize(2);
        assertThat(loadProfileCommand.getLoadProfileReaders()).isNotNull();
        assertThat(loadProfileCommand.getLoadProfileReaders()).hasSize(2);
        assertThat(loadProfileCommand.toJournalMessageDescription(LogLevel.ERROR)).contains("{loadProfileObisCodes: 1.0.99.1.0.255; markAsBadTime: false; createEventsFromStatusFlag: false}");
    }

    @Test
    public void removeIncorrectLoadProfileReadersTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        CommandRoot commandRoot = createCommandRoot();

        OfflineLoadProfile offlineLoadProfile1 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile1.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile2 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile2.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile3 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile3.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineLoadProfile offlineLoadProfile4 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile4.getLastReading()).thenReturn(Optional.<Instant>empty());
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getAllOfflineLoadProfilesForMRID(MY_MRID)).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        // Business method
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, offlineDevice, commandRoot, comTaskExecution);
        //loadProfileCommand.createLoadProfileReaders(offlineDevice);

        List<LoadProfileReader> loadProfileReaders = loadProfileCommand.getLoadProfileReaders();
        List<LoadProfileReader> readersToRemove = new ArrayList<>(2);
        readersToRemove.add(loadProfileReaders.get(0));
        readersToRemove.add(loadProfileReaders.get(1));
        loadProfileCommand.removeIncorrectLoadProfileReaders(readersToRemove);

        // Asserts
        assertThat(loadProfileCommand.getLoadProfileReaders()).hasSize(2);
    }

    @Test
    public void dontFindLoadProfileIntervalForLoadProfileReaderTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        CommandRoot commandRoot = createCommandRoot();
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, mock(OfflineDevice.class), commandRoot, comTaskExecution);
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);

        // Asserts
        assertThat(loadProfileCommand.findLoadProfileIntervalForLoadProfileReader(loadProfileReader)).isEqualTo(LoadProfileCommand.INVALID_LOAD_PROFILE_INTERVAL);
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
            assertThat(loadProfileCommand.findLoadProfileIntervalForLoadProfileReader(loadProfileReader))
                .as("Expected reader " + count++ + " to have a correct interval")
                .isEqualTo(FIXED_LOAD_PROFILE_INTERVAL.getSeconds());
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
        assertThat(channelInfos).isNotNull();
        assertThat(channelInfos).hasSize(3);

        int count = 0;
        for (ChannelInfo channelInfo : channelInfos) {
            assertThat(count).isEqualTo(channelInfo.getId());
            assertThat(channelObisCodes[count++].toString()).isEqualTo(channelInfo.getName());
            assertThat(FIXED_CHANNEL_UNIT).isEqualTo(channelInfo.getUnit());
            assertThat(FIXED_DEVICE_SERIAL_NUMBER).isEqualTo(channelInfo.getMeterIdentifier());
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
        assertThat(loadProfileCommand.getCommands()).hasSize(5);
        assertThat(commandRoot.getCommands()).hasSize(1);
        assertThat(loadProfileCommand.getCommands().values().toArray()[0]).isInstanceOf(VerifyLoadProfilesCommandImpl.class);
        assertThat(loadProfileCommand.getCommands().values().toArray()[1]).isInstanceOf(ReadLoadProfileDataCommand.class);
        assertThat(loadProfileCommand.getCommands().values().toArray()[2]).isInstanceOf(TimeDifferenceCommand.class);
        assertThat(loadProfileCommand.getCommands().values().toArray()[3]).isInstanceOf(MarkIntervalsAsBadTimeCommand.class);
        assertThat(loadProfileCommand.getCommands().values().toArray()[4]).isInstanceOf(CreateMeterEventsFromStatusFlagsCommand.class);
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
