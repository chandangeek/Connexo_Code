package com.energyict.mdc.engine.impl.commands.store.core;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.collect.TopologyCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.ClockCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.LoadProfileCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.TimeDifferenceCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.TopologyCommandImpl;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.upl.tasks.TopologyAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the CompositeComCommandImpl component
 *
 * @author gna
 * @since 29/05/12 - 11:55
 */
@RunWith(MockitoJUnitRunner.class)
public class CompositeComCommandTest extends CommonCommandImplTests {

    private final TimeDuration MAX_CLOCK_DIFF = new TimeDuration(8);
    private final TimeDuration MIN_CLOCK_DIFF = new TimeDuration(2);
    private final TimeDuration MAX_CLOCK_SHIFT = new TimeDuration(5);
    @Mock
    private ComTaskExecution comTaskExecution1;
    @Mock
    private ComTaskExecution comTaskExecution2;
    @Mock
    private OfflineDevice offlineDevice;
    @Mock
    private Device device;

    @Test
    public void chronologicalOrderTest() {

        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        // set all the options to false
        when(loadProfilesTask.createMeterEventsFromStatusFlags()).thenReturn(false);
        when(loadProfilesTask.isMarkIntervalsAsBadTime()).thenReturn(false);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(false);

        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getClockTaskType()).thenReturn(ClockTaskType.FORCECLOCK);
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(MAX_CLOCK_DIFF));
        when(clockTask.getMaximumClockShift()).thenReturn(Optional.of(MAX_CLOCK_SHIFT));
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(MIN_CLOCK_DIFF));

        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
//        CommandRoot groupedDeviceCommand = createCommandRoot();
        groupedDeviceCommand.addCommand(new TimeDifferenceCommandImpl(groupedDeviceCommand), comTaskExecution1);
        groupedDeviceCommand.addCommand(new TopologyCommandImpl(groupedDeviceCommand, TopologyAction.UPDATE, comTaskExecution1), comTaskExecution1);
        groupedDeviceCommand.addCommand(new LoadProfileCommandImpl(groupedDeviceCommand, loadProfilesTask, comTaskExecution1), comTaskExecution1);
        groupedDeviceCommand.addCommand(new ClockCommandImpl(groupedDeviceCommand, clockTask, comTaskExecution1), comTaskExecution1);

        int count = 0;
        for (ComCommand command : groupedDeviceCommand.getComTaskRoot(comTaskExecution1)) {
            switch (count) {
                case 0:
                    assertTrue(command instanceof TimeDifferenceCommand);
                    break;
                case 1:
                    assertTrue(command instanceof TopologyCommand);
                    break;
                case 2:
                    assertTrue(command instanceof LoadProfileCommand);
                    break;
                case 3:
                    assertTrue(command instanceof ClockCommand);
                    break;
            }
            count++;
        }
    }

    @Test
    public void chronologicalOrderDifferentComTasksTest() {

        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        // set all the options to false
        when(loadProfilesTask.createMeterEventsFromStatusFlags()).thenReturn(false);
        when(loadProfilesTask.isMarkIntervalsAsBadTime()).thenReturn(false);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(false);

        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getClockTaskType()).thenReturn(ClockTaskType.FORCECLOCK);
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(MAX_CLOCK_DIFF));
        when(clockTask.getMaximumClockShift()).thenReturn(Optional.of(MAX_CLOCK_SHIFT));
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(MIN_CLOCK_DIFF));

        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        groupedDeviceCommand.addCommand(new TimeDifferenceCommandImpl(groupedDeviceCommand), comTaskExecution1);
        groupedDeviceCommand.addCommand(new TopologyCommandImpl(groupedDeviceCommand, TopologyAction.UPDATE, comTaskExecution1), comTaskExecution1);
        groupedDeviceCommand.addCommand(new LoadProfileCommandImpl(groupedDeviceCommand, loadProfilesTask, comTaskExecution1), comTaskExecution2);
        groupedDeviceCommand.addCommand(new ClockCommandImpl(groupedDeviceCommand, clockTask, comTaskExecution1), comTaskExecution2);

        int count = 0;
        for (ComCommand command : groupedDeviceCommand.getComTaskRoot(comTaskExecution1)) {
            switch (count) {
                case 0:
                    assertTrue(command instanceof TimeDifferenceCommand);
                    break;
                case 1:
                    assertTrue(command instanceof TopologyCommand);
                    break;
            }
            count++;
        }
        for (ComCommand command : groupedDeviceCommand.getComTaskRoot(comTaskExecution2)) {
            switch (count) {
                case 2:
                    assertTrue(command instanceof LoadProfileCommand);
                    break;
                case 3:
                    assertTrue(command instanceof ClockCommand);
                    break;
            }
            count++;
        }
    }
}