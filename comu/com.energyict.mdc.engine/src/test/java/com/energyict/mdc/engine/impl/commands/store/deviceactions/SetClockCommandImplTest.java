package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.comserver.logging.LogLevel;
import com.energyict.mdc.commands.ClockCommand;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.tasks.ClockTask;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 8/07/13
 * Time: 13:54
 * Author: khe
 */
public class SetClockCommandImplTest {

    @Test
    public void testToJournalMessageDescription() throws ClassNotFoundException {
        CommandRoot commandRoot = mock(CommandRoot.class);
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMinimumClockDifference()).thenReturn(new TimeDuration(10));
        when(clockTask.getMaximumClockDifference()).thenReturn(new TimeDuration(100));
        when(clockCommand.getClockTask()).thenReturn(clockTask);

        SetClockCommandImpl setClockCommand = new SetClockCommandImpl(clockCommand, commandRoot, null);
        assertEquals("SetClockCommandImpl {minimumDifference: 10s; maximumDifference: 100s}", setClockCommand.toJournalMessageDescription(LogLevel.ERROR));
    }

}