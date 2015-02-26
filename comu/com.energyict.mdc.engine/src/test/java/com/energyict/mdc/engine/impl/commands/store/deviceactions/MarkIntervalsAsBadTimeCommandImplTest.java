package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.MarkIntervalsAsBadTimeCommand;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfile;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.tasks.LoadProfilesTask;

import com.elster.jupiter.time.TimeDuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
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
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);

        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getObisCode()).thenReturn(ObisCode.fromString("1.1.1.1.1.1"));
        when(loadProfilesTask.getLoadProfileTypes()).thenReturn(Arrays.asList(loadProfileType));

        // it is unnatural to NOT set the markIntervalsAsBadTime flag, but otherwise we can't spy the LoadProfileCommand ...
//        when(loadProfilesTask.doMarkIntervalsAsBadTime()).thenReturn(true);

        when(loadProfilesTask.getMinClockDiffBeforeBadTime()).thenReturn(Optional.of(minClockDiffBeforeBadTime));
        CommandRoot commandRoot = createCommandRoot();
        LoadProfileCommand loadProfileCommand = spy(commandRoot.getLoadProfileCommand(loadProfilesTask, commandRoot, comTaskExecution));
        TimeDifferenceCommand timeDifferenceCommand = mock(TimeDifferenceCommand.class);
        when(timeDifferenceCommand.getTimeDifference()).thenReturn(Optional.of(timeDifference));
        when(loadProfileCommand.getTimeDifferenceCommand()).thenReturn(timeDifferenceCommand);

        MarkIntervalsAsBadTimeCommand markIntervalsAsBadTimeCommand = commandRoot.getMarkIntervalsAsBadTimeCommand(loadProfileCommand, comTaskExecution);
        loadProfileCommand.addCollectedDataItem(createDeviceCollectedLoadProfile());
        markIntervalsAsBadTimeCommand.execute(deviceProtocol, this.newTestExecutionContext());
        assertThat(markIntervalsAsBadTimeCommand.toJournalMessageDescription(LogLevel.ERROR)).contains("{minimumClockDifference: 1 minutes");

        // asserts
        assertNotNull(loadProfileCommand.getCollectedData());
        Assert.assertEquals("Should only contain 1 collectedData object", 1, loadProfileCommand.getCollectedData().size());
        DeviceLoadProfile deviceLoadProfile = (DeviceLoadProfile) loadProfileCommand.getCollectedData().get(0);
        // all intervals should be marked as BADTIME
        for (IntervalData intervalData : deviceLoadProfile.getCollectedIntervalData()) {
            Assert.assertEquals("Status should be BADTIME", IntervalStateBits.BADTIME, intervalData.getEiStatus());
        }
    }

    @Test
    public void doExecuteWithLowerTimeDifferenceTest() {
        final TimeDuration timeDifference = new TimeDuration(1, TimeDuration.TimeUnit.MINUTES);
        final TimeDuration minClockDiffBeforeBadTime = new TimeDuration(5, TimeDuration.TimeUnit.MINUTES);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);

        // it is unnatural to NOT set the markIntervalsAsBadTime flag, but otherwise we can't spy the LoadProfileCommand ...
//        when(loadProfilesTask.doMarkIntervalsAsBadTime()).thenReturn(true);

        when(loadProfilesTask.getMinClockDiffBeforeBadTime()).thenReturn(Optional.of(minClockDiffBeforeBadTime));
        CommandRoot commandRoot = createCommandRoot();
        LoadProfileCommand loadProfileCommand = spy(commandRoot.getLoadProfileCommand(loadProfilesTask, commandRoot, comTaskExecution));
        TimeDifferenceCommand timeDifferenceCommand = mock(TimeDifferenceCommand.class);
        when(timeDifferenceCommand.getTimeDifference()).thenReturn(Optional.of(timeDifference));
        when(loadProfileCommand.getTimeDifferenceCommand()).thenReturn(timeDifferenceCommand);

        MarkIntervalsAsBadTimeCommand markIntervalsAsBadTimeCommand = commandRoot.getMarkIntervalsAsBadTimeCommand(loadProfileCommand, comTaskExecution);
        loadProfileCommand.addCollectedDataItem(createDeviceCollectedLoadProfile());
        markIntervalsAsBadTimeCommand.execute(deviceProtocol, this.newTestExecutionContext());

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
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(null);
        List<IntervalData> intervalDatas = new ArrayList<>();
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            intervalDatas.add(new IntervalData(new Date(), IntervalStateBits.OK));
        }
        deviceLoadProfile.setCollectedData(intervalDatas, channelInfos);
        return deviceLoadProfile;
    }

}
