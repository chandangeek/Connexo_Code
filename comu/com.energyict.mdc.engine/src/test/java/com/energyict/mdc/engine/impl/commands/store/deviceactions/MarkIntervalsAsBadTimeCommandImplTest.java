package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.MarkIntervalsAsBadTimeCommand;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.ComCommandDescriptionTitle;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfile;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.Warning;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProtocolReadingQualities;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests for the MarkIntervalsAsBadTimeCommandImpl component
 *
 * @author gna
 * @since 31/05/12 - 10:12
 */
@RunWith(MockitoJUnitRunner.class)
public class MarkIntervalsAsBadTimeCommandImplTest extends CommonCommandImplTests {

    private final String mrid = "MyMrid";

    @Mock
    ComTaskExecution comTaskExecution;
    @Mock
    private OfflineDevice offlineDevice;
    @Mock
    private Device device;

    private static DeviceLoadProfile createDeviceCollectedLoadProfile() {
        LoadProfileIdentifier loadProfileIdentifier = mock(LoadProfileIdentifier.class);
        when(loadProfileIdentifier.toString()).thenReturn("0.0.99.98.0.255");
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(loadProfileIdentifier);
        List<IntervalData> intervalDatas = new ArrayList<>();
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            intervalDatas.add(new IntervalData(new Date(), new HashSet<>()));
        }
        deviceLoadProfile.setCollectedIntervalData(intervalDatas, channelInfos);
        return deviceLoadProfile;
    }

    @Before
    public void initBefore() {
        when(device.getmRID()).thenReturn(mrid);
        when(comTaskExecution.getDevice()).thenReturn(device);
    }

    @Test
    public void doExecuteWithLargerTimeDifferenceTest() {
        final TimeDuration timeDifference = new TimeDuration(13, TimeDuration.TimeUnit.MINUTES);
        final TimeDuration minClockDiffBeforeBadTime = new TimeDuration(1, TimeDuration.TimeUnit.MINUTES);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        Date tooFarAway = Date.from(commandRootServiceProvider.clock().instant().minus(Duration.ofMinutes(10)));
        when(deviceProtocol.getTime()).thenReturn(tooFarAway);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.isMarkIntervalsAsBadTime()).thenReturn(true);

        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getObisCode()).thenReturn(ObisCode.fromString("1.1.1.1.1.1"));
        when(loadProfilesTask.getLoadProfileTypes()).thenReturn(Arrays.asList(loadProfileType));

        // it is unnatural to NOT set the markIntervalsAsBadTime flag, but otherwise we can't spy the LoadProfileCommand ...
//        when(loadProfilesTask.doMarkIntervalsAsBadTime()).thenReturn(true);

        when(loadProfilesTask.getMinClockDiffBeforeBadTime()).thenReturn(Optional.of(minClockDiffBeforeBadTime));
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        LoadProfileCommand loadProfileCommand = spy(groupedDeviceCommand.getLoadProfileCommand(loadProfilesTask, groupedDeviceCommand, comTaskExecution));
        TimeDifferenceCommand timeDifferenceCommand = mock(TimeDifferenceCommand.class);
        when(timeDifferenceCommand.getTimeDifference()).thenReturn(Optional.of(timeDifference));
        when(loadProfileCommand.getTimeDifferenceCommand()).thenReturn(timeDifferenceCommand);

        MarkIntervalsAsBadTimeCommand markIntervalsAsBadTimeCommand = groupedDeviceCommand.getMarkIntervalsAsBadTimeCommand(loadProfileCommand, comTaskExecution);
        loadProfileCommand.addCollectedDataItem(createDeviceCollectedLoadProfile());
        markIntervalsAsBadTimeCommand.execute(deviceProtocol, newTestExecutionContext());
        String journalMessage = markIntervalsAsBadTimeCommand.toJournalMessageDescription(LogLevel.DEBUG);

        // asserts
        assertNotNull(loadProfileCommand.getIssues());
        assertEquals("Should only contain 1 issue", 1, loadProfileCommand.getIssues().size());
        Issue issue = loadProfileCommand.getIssues().get(0);
        assertThat(issue).isInstanceOf(Warning.class);
        assertThat(issue.getDescription()).isEqualTo(MessageSeeds.INTERVALS_MARKED_AS_BAD_TIME.getKey());

        assertNotNull(loadProfileCommand.getCollectedData());
        assertEquals("Should only contain 1 collectedData object", 1, loadProfileCommand.getCollectedData().size());
        DeviceLoadProfile deviceLoadProfile = (DeviceLoadProfile) loadProfileCommand.getCollectedData().get(0);
        // all intervals should be marked as BADTIME
        for (IntervalData intervalData : deviceLoadProfile.getCollectedIntervalData()) {
            assertThat(intervalData.getReadingQualityTypes()).hasSize(1);
            assertThat(intervalData.getReadingQualityTypes().toArray()[0]).isEqualTo(ProtocolReadingQualities.BADTIME.getCimCode());
        }
        assertEquals(ComCommandDescriptionTitle.MarkIntervalsAsBadTimeCommandImpl.getDescription() + " {nrOfWarnings: 1; nrOfProblems: 0; minimumClockDifference: 1 minutes; badTimeLoadProfiles: 0.0.99.98.0.255}", journalMessage);
    }

    @Test
    public void doExecuteWithLowerTimeDifferenceTest() {
        final TimeDuration timeDifference = new TimeDuration(1, TimeDuration.TimeUnit.MINUTES);
        final TimeDuration minClockDiffBeforeBadTime = new TimeDuration(5, TimeDuration.TimeUnit.MINUTES);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        Date tooFarAway = Date.from(commandRootServiceProvider.clock().instant().minus(Duration.ofSeconds(5)));
        when(deviceProtocol.getTime()).thenReturn(tooFarAway);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.isMarkIntervalsAsBadTime()).thenReturn(true);

        // it is unnatural to NOT set the markIntervalsAsBadTime flag, but otherwise we can't spy the LoadProfileCommand ...
//        when(loadProfilesTask.doMarkIntervalsAsBadTime()).thenReturn(true);

        when(loadProfilesTask.getMinClockDiffBeforeBadTime()).thenReturn(Optional.of(minClockDiffBeforeBadTime));
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        LoadProfileCommand loadProfileCommand = spy(groupedDeviceCommand.getLoadProfileCommand(loadProfilesTask, groupedDeviceCommand, comTaskExecution));
        TimeDifferenceCommand timeDifferenceCommand = mock(TimeDifferenceCommand.class);
        when(timeDifferenceCommand.getTimeDifference()).thenReturn(Optional.of(timeDifference));
        when(loadProfileCommand.getTimeDifferenceCommand()).thenReturn(timeDifferenceCommand);

        MarkIntervalsAsBadTimeCommand markIntervalsAsBadTimeCommand = groupedDeviceCommand.getMarkIntervalsAsBadTimeCommand(loadProfileCommand, comTaskExecution);
        loadProfileCommand.addCollectedDataItem(createDeviceCollectedLoadProfile());
        markIntervalsAsBadTimeCommand.execute(deviceProtocol, newTestExecutionContext());
        String journalMessage = markIntervalsAsBadTimeCommand.toJournalMessageDescription(LogLevel.INFO);

        // asserts
        assertNotNull(loadProfileCommand.getCollectedData());
        assertEquals("Should only contain 1 collectedData object", 1, loadProfileCommand.getCollectedData().size());
        DeviceLoadProfile deviceLoadProfile = (DeviceLoadProfile) loadProfileCommand.getCollectedData().get(0);
        // all intervals should be marked as OK
        for (IntervalData intervalData : deviceLoadProfile.getCollectedIntervalData()) {
            assertThat(intervalData.getReadingQualityTypes()).hasSize(0);
        }
        assertEquals(ComCommandDescriptionTitle.MarkIntervalsAsBadTimeCommandImpl.getDescription() + " {badTimeLoadProfiles: None}", journalMessage);
    }

}
