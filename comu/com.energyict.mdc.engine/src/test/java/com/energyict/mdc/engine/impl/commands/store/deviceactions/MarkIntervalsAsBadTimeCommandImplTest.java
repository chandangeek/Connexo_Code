package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.MarkIntervalsAsBadTimeCommand;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfile;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.Warning;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.tasks.LoadProfilesTask;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

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
    private Device device;

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
        CommandRoot commandRoot = createCommandRoot();
        LoadProfileCommand loadProfileCommand = spy(commandRoot.getLoadProfileCommand(loadProfilesTask, commandRoot, comTaskExecution));
        TimeDifferenceCommand timeDifferenceCommand = commandRoot.getTimeDifferenceCommand(loadProfileCommand, comTaskExecution);
        ExecutionContext executionContext = this.newTestExecutionContext();
        timeDifferenceCommand.execute(deviceProtocol, executionContext);

        MarkIntervalsAsBadTimeCommand markIntervalsAsBadTimeCommand = commandRoot.getMarkIntervalsAsBadTimeCommand(loadProfileCommand, comTaskExecution);
        loadProfileCommand.addCollectedDataItem(createDeviceCollectedLoadProfile());
        markIntervalsAsBadTimeCommand.execute(deviceProtocol, executionContext);

        // asserts
        assertThat(markIntervalsAsBadTimeCommand.toJournalMessageDescription(LogLevel.DEBUG)).contains("minimumClockDifference: 1 minutes");
        assertThat(loadProfileCommand.getIssues()).isNotNull()
                .hasSize(1);
        Issue issue = loadProfileCommand.getIssues().get(0);
        assertThat(issue).isInstanceOf(Warning.class);
        assertThat(issue.getDescription()).isEqualTo("intervalsMarkedAsBadTime");

        assertNotNull(loadProfileCommand.getCollectedData());
        Assert.assertEquals("Should only contain 1 collectedData object", 1, loadProfileCommand.getCollectedData().size());
        DeviceLoadProfile deviceLoadProfile = (DeviceLoadProfile) loadProfileCommand.getCollectedData().get(0);
        // all intervals should be marked as BADTIME
        for (IntervalData intervalData : deviceLoadProfile.getCollectedIntervalData()) {
            Assert.assertEquals("Status should be BADTIME", IntervalStateBits.BADTIME, intervalData.getEiStatus());
        }
        String journalMessage = markIntervalsAsBadTimeCommand.toJournalMessageDescription(LogLevel.DEBUG);
        assertThat(journalMessage).isEqualTo("Mark load profile intervals as bad time {nrOfWarnings: 1; nrOfProblems: 0; minimumClockDifference: 1 minutes; badTimeLoadProfiles: 0.0.99.98.0.255}");
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

        when(loadProfilesTask.getMinClockDiffBeforeBadTime()).thenReturn(Optional.of(minClockDiffBeforeBadTime));
        CommandRoot commandRoot = createCommandRoot();
        LoadProfileCommand loadProfileCommand = commandRoot.getLoadProfileCommand(loadProfilesTask, commandRoot, comTaskExecution);
        TimeDifferenceCommand timeDifferenceCommand = commandRoot.getTimeDifferenceCommand(loadProfileCommand, comTaskExecution);
        ExecutionContext executionContext = this.newTestExecutionContext();
        timeDifferenceCommand.execute(deviceProtocol, executionContext);

        MarkIntervalsAsBadTimeCommand markIntervalsAsBadTimeCommand = commandRoot.getMarkIntervalsAsBadTimeCommand(loadProfileCommand, comTaskExecution);
        loadProfileCommand.addCollectedDataItem(createDeviceCollectedLoadProfile());
        markIntervalsAsBadTimeCommand.execute(deviceProtocol, executionContext);

        // asserts
        assertNotNull(loadProfileCommand.getCollectedData());
        Assert.assertEquals("Should only contain 1 collectedData object", 1, loadProfileCommand.getCollectedData().size());
        DeviceLoadProfile deviceLoadProfile = (DeviceLoadProfile) loadProfileCommand.getCollectedData().get(0);
        // all intervals should be marked as OK
        for (IntervalData intervalData : deviceLoadProfile.getCollectedIntervalData()) {
            Assert.assertEquals("Status should be OK", IntervalStateBits.OK, intervalData.getEiStatus());
        }
    }

    private static DeviceLoadProfile createDeviceCollectedLoadProfile() {
        LoadProfileIdentifier loadProfileIdentifier = mock(LoadProfileIdentifier.class);
        when(loadProfileIdentifier.toString()).thenReturn("0.0.99.98.0.255");
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(loadProfileIdentifier);
        List<IntervalData> intervalDatas = new ArrayList<>();
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            intervalDatas.add(new IntervalData(new Date(), IntervalStateBits.OK));
        }
        deviceLoadProfile.setCollectedData(intervalDatas, channelInfos);
        return deviceLoadProfile;
    }

}
