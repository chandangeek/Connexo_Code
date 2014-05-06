package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.comserver.commands.deviceactions.ClockCommandImpl;
import com.energyict.comserver.commands.deviceactions.LoadProfileCommandImpl;
import com.energyict.comserver.commands.deviceactions.TimeDifferenceCommandImpl;
import com.energyict.comserver.commands.deviceactions.TopologyCommandImpl;
import com.energyict.comserver.exceptions.ComCommandException;
import com.energyict.mdc.commands.ClockCommand;
import com.energyict.mdc.commands.ComCommand;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.commands.LoadProfileCommand;
import com.energyict.mdc.commands.TimeDifferenceCommand;
import com.energyict.mdc.commands.TopologyCommand;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.LoadProfilesTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.comserver.commands.core.CompositeComCommandImpl} component
 *
 * @author gna
 * @since 29/05/12 - 11:55
 */
@RunWith(MockitoJUnitRunner.class)
public class CompositeComCommandTest extends CommonCommandImplTests {

    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private OfflineDevice offlineDevice;

    private final TimeDuration MAX_CLOCK_DIFF = new TimeDuration(8);
    private final TimeDuration MIN_CLOCK_DIFF = new TimeDuration(2);
    private final TimeDuration MAX_CLOCK_SHIFT = new TimeDuration(5);

    @Test(expected = ComCommandException.class)
    public void uniqueCommandViolationTest(){
        CommandRoot commandRoot = createCommandRoot();
        commandRoot.addCommand(new TimeDifferenceCommandImpl(commandRoot), comTaskExecution);
        commandRoot.addCommand(new TimeDifferenceCommandImpl(commandRoot), comTaskExecution);
    }

    @Test
    public void chronologicalOrderTest(){

        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        // set all the options to false
        when(loadProfilesTask.createMeterEventsFromStatusFlags()).thenReturn(false);
        when(loadProfilesTask.isMarkIntervalsAsBadTime()).thenReturn(false);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(false);

        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getClockTaskType()).thenReturn(ClockTaskType.FORCECLOCK);
        when(clockTask.getMaximumClockDifference()).thenReturn(MAX_CLOCK_DIFF);
        when(clockTask.getMaximumClockShift()).thenReturn(MAX_CLOCK_SHIFT);
        when(clockTask.getMinimumClockDifference()).thenReturn(MIN_CLOCK_DIFF);

        CommandRoot commandRoot = createCommandRoot();
        commandRoot.addCommand(new TimeDifferenceCommandImpl(commandRoot), comTaskExecution);
        commandRoot.addCommand(new TopologyCommandImpl(commandRoot, TopologyAction.UPDATE, this.offlineDevice, comTaskExecution), comTaskExecution);
        commandRoot.addCommand(new LoadProfileCommandImpl(loadProfilesTask, mock(OfflineDevice.class), commandRoot, comTaskExecution), comTaskExecution);
        commandRoot.addCommand(new ClockCommandImpl(clockTask, commandRoot, comTaskExecution), comTaskExecution);

        int count = 0;
        for (ComCommand command : commandRoot) {
            switch (count){
                case 0: assertTrue(command instanceof TimeDifferenceCommand);break;
                case 1: assertTrue(command instanceof TopologyCommand);break;
                case 2: assertTrue(command instanceof LoadProfileCommand);break;
                case 3: assertTrue(command instanceof ClockCommand);break;
            }
            count++;
        }
    }

}
