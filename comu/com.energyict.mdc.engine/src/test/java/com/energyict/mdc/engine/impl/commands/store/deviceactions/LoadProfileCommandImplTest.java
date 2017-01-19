package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.cbo.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.CreateMeterEventsFromStatusFlagsCommand;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.MarkIntervalsAsBadTimeCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLoadProfileDataCommand;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLoadProfileChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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

    private static final String MY_MRID = "MyMrid";
    private static final ObisCode FIXED_LOAD_PROFILE_OBIS_CODE = ObisCode.fromString("1.0.99.1.0.255");
    private static final TimeDuration FIXED_LOAD_PROFILE_INTERVAL = new TimeDuration(900);
    private static final Instant LAST_READING = Instant.now();
    private static final long FIXED_DEVICE_ID = 123;
    private static final String FIXED_DEVICE_SERIAL_NUMBER = "FIXED_DEVICE_SERIAL_NUMBER";
    private static final long LOAD_PROFILE_TYPE_ID = 651;
    private static final Unit FIXED_CHANNEL_UNIT = Unit.get("kWh");
    @Mock
    ComTaskExecution comTaskExecution;
    @Mock
    private Device device;
    @Mock
    private OfflineDevice offlineDevice;

    private static OfflineLoadProfileChannel createMockedOfflineLoadProfileChannel(final ObisCode obisCode) {
        OfflineLoadProfileChannel loadProfileChannel = mock(OfflineLoadProfileChannel.class);
        when(loadProfileChannel.getMasterSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        when(loadProfileChannel.getObisCode()).thenReturn(obisCode);
        when(loadProfileChannel.getUnit()).thenReturn(FIXED_CHANNEL_UNIT);
        when(loadProfileChannel.isStoreData()).thenReturn(true);
        return loadProfileChannel;
    }

    @Before
    public void initBefore() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(0L);
        when(device.getmRID()).thenReturn(MY_MRID);
        when(comTaskExecution.getDevice()).thenReturn(device);
    }

    @Test(expected = CodingException.class)
    public void loadProfileTaskNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        new LoadProfileCommandImpl(createGroupedDeviceCommand(device, deviceProtocol), null, comTaskExecution);
        // should have gotten an exception
    }

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        new LoadProfileCommandImpl(null, mock(LoadProfilesTask.class), comTaskExecution);
        // Was expecting a CodingException
    }

    @Test(expected = CodingException.class)
    public void offlineDeviceNullTest() {
        new LoadProfileCommandImpl(createGroupedDeviceCommand(null, deviceProtocol), mock(LoadProfilesTask.class), comTaskExecution);
        // Was expecting a CodingException
    }

    private OfflineLoadProfileChannel getMockChannel() {
        return getMockChannel(ObisCode.fromString("1.x.128.1.0.255"));
    }

    private OfflineLoadProfile mockOfflineLoadProfile(long loadProfileId) {
        OfflineLoadProfile offlineLoadProfile = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile.getLoadProfileId()).thenReturn(loadProfileId);
        return offlineLoadProfile;
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
    public void verifyLoadProfilesCommandNotNullTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(true);
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(groupedDeviceCommand, loadProfilesTask, comTaskExecution);

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
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(groupedDeviceCommand, loadProfilesTask, comTaskExecution);

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
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(groupedDeviceCommand, loadProfilesTask, comTaskExecution);

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
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);

        OfflineLoadProfile offlineLoadProfile1 = mockOfflineLoadProfile(1);
        OfflineLoadProfile offlineLoadProfile2 = mockOfflineLoadProfile(2);
        OfflineLoadProfile offlineLoadProfile3 = mockOfflineLoadProfile(3);
        OfflineLoadProfile offlineLoadProfile4 = mockOfflineLoadProfile(4);

        OfflineLoadProfileChannel mockChannel = getMockChannel();
        when(offlineLoadProfile1.getOfflineChannels()).thenReturn(Arrays.asList(mockChannel));
        when(offlineLoadProfile2.getOfflineChannels()).thenReturn(Arrays.asList(mockChannel));
        when(offlineLoadProfile3.getOfflineChannels()).thenReturn(Arrays.asList(mockChannel));
        when(offlineLoadProfile4.getOfflineChannels()).thenReturn(Arrays.asList(mockChannel));

        when(offlineDevice.getAllOfflineLoadProfiles()).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        // Business method
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(groupedDeviceCommand, loadProfilesTask, comTaskExecution);

        // Asserts
        assertNotNull(loadProfileCommand.getLoadProfileReaderMap());
        assertEquals("Expected 4 readers in the map", 4, loadProfileCommand.getLoadProfileReaderMap().size());
        assertNotNull(loadProfileCommand.getLoadProfileReaders());
        assertEquals("Expected 4 readers", 4, loadProfileCommand.getLoadProfileReaders().size());
    }

    @Test
    public void createLoadProfileReadersWithSpecificLoadProfileTypeTest() {
        final long loadProfileTypeId = 165;
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getId()).thenReturn(loadProfileTypeId);
        when(loadProfileType.getObisCode()).thenReturn(FIXED_LOAD_PROFILE_OBIS_CODE);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.getLoadProfileTypes()).thenReturn(Arrays.asList(loadProfileType));
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);

        OfflineLoadProfile offlineLoadProfile1 = mockOfflineLoadProfile(1);
        OfflineLoadProfile offlineLoadProfile2 = mockOfflineLoadProfile(2);
        when(offlineLoadProfile2.getLoadProfileTypeId()).thenReturn(loadProfileTypeId);
        List<OfflineLoadProfileChannel> channels = Arrays.asList(getMockChannel(), getMockChannel());
        when(offlineLoadProfile2.getOfflineChannels()).thenReturn(channels);
        OfflineLoadProfile offlineLoadProfile3 = mockOfflineLoadProfile(3);
        OfflineLoadProfile offlineLoadProfile4 = mockOfflineLoadProfile(4);
        when(offlineLoadProfile4.getLoadProfileTypeId()).thenReturn(loadProfileTypeId);
        when(offlineLoadProfile4.getOfflineChannels()).thenReturn(channels);
        when(offlineDevice.getAllOfflineLoadProfiles()).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        // Business method
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(groupedDeviceCommand, loadProfilesTask, comTaskExecution);
        //loadProfileCommand.createLoadProfileReaders(offlineDevice);

        // Asserts
        assertNotNull(loadProfileCommand.getLoadProfileReaderMap());
        assertEquals("Expected 2 readers in the map", 2, loadProfileCommand.getLoadProfileReaderMap().size());
        assertNotNull(loadProfileCommand.getLoadProfileReaders());
        assertEquals("Expected 2 readers", 2, loadProfileCommand.getLoadProfileReaders().size());
    }

    @Test
    public void removeIncorrectLoadProfileReadersTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);


        OfflineLoadProfile offlineLoadProfile1 = mockOfflineLoadProfile(1);
        OfflineLoadProfile offlineLoadProfile2 = mockOfflineLoadProfile(2);
        OfflineLoadProfile offlineLoadProfile3 = mockOfflineLoadProfile(3);
        OfflineLoadProfile offlineLoadProfile4 = mockOfflineLoadProfile(4);

        OfflineLoadProfileChannel mockChannel = getMockChannel();
        when(offlineLoadProfile1.getOfflineChannels()).thenReturn(Arrays.asList(mockChannel));
        when(offlineLoadProfile2.getOfflineChannels()).thenReturn(Arrays.asList(mockChannel));
        when(offlineLoadProfile3.getOfflineChannels()).thenReturn(Arrays.asList(mockChannel));
        when(offlineLoadProfile4.getOfflineChannels()).thenReturn(Arrays.asList(mockChannel));

        when(offlineDevice.getAllOfflineLoadProfiles()).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        // Business method
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(groupedDeviceCommand, loadProfilesTask, comTaskExecution);
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
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(groupedDeviceCommand, loadProfilesTask, comTaskExecution);
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);

        // Asserts
        assertEquals("Should not have found the LoadProfileReader", LoadProfileCommand.INVALID_LOAD_PROFILE_INTERVAL, loadProfileCommand.findLoadProfileIntervalForLoadProfileReader(loadProfileReader));
    }

    @Test
    public void findLoadProfileIntervalForLoadProfileReaderTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);

        OfflineLoadProfile offlineLoadProfile1 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile1.interval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL.asTemporalAmount());
        OfflineLoadProfile offlineLoadProfile2 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile2.interval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL.asTemporalAmount());
        OfflineLoadProfile offlineLoadProfile3 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile3.interval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL.asTemporalAmount());
        OfflineLoadProfile offlineLoadProfile4 = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile4.interval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL.asTemporalAmount());
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getAllOfflineLoadProfiles()).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        // Business method
        LoadProfileCommandImpl loadProfileCommand = new LoadProfileCommandImpl(groupedDeviceCommand, loadProfilesTask, comTaskExecution);
        //loadProfileCommand.createLoadProfileReaders(offlineDevice);

        // Asserts
        int count = 0;
        for (LoadProfileReader loadProfileReader : loadProfileCommand.getLoadProfileReaders()) {
            assertEquals("Expected reader " + count++ + " to have a correct interval", FIXED_LOAD_PROFILE_INTERVAL.getSeconds(), loadProfileCommand.findLoadProfileIntervalForLoadProfileReader(loadProfileReader));
        }
    }

    @Test
    public void completeConstructionInCorrectOrderTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(true);
        when(loadProfilesTask.createMeterEventsFromStatusFlags()).thenReturn(true);
        when(loadProfilesTask.isMarkIntervalsAsBadTime()).thenReturn(true);
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        LoadProfileCommand loadProfileCommand = groupedDeviceCommand.getLoadProfileCommand(loadProfilesTask, groupedDeviceCommand, comTaskExecution);

        // Asserts
        assertEquals("Expected 5 subCommands in the command list of the LoadProfileCommand", 5, loadProfileCommand.getCommands().size());
        assertEquals("The groupedDeviceCommand should only contain 1 command, the LoadProfileCommand", 1, groupedDeviceCommand.getComTaskRoot(comTaskExecution).getCommands().size());
        assertTrue("The first command should be the verifyLoadProfileCommand", loadProfileCommand.getCommands().values().toArray()[0] instanceof VerifyLoadProfilesCommandImpl);
        assertTrue("The second command should be the readLoadProfileDataCommand", loadProfileCommand.getCommands().values().toArray()[1] instanceof ReadLoadProfileDataCommand);
        assertTrue("The third command should be the timeDifferenceCommand", loadProfileCommand.getCommands().values().toArray()[2] instanceof TimeDifferenceCommand);
        assertTrue("The fourth command should be the markIntervalsAsBadTimeCommand", loadProfileCommand.getCommands().values().toArray()[3] instanceof MarkIntervalsAsBadTimeCommand);
        assertTrue("The fifth command should be the createMeterEventsCommand", loadProfileCommand.getCommands().values().toArray()[4] instanceof CreateMeterEventsFromStatusFlagsCommand);
    }

}