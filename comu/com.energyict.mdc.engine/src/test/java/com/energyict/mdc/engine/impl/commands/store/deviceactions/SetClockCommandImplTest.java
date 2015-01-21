package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.tasks.ClockTask;

import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(new TimeDuration(10)));
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(new TimeDuration(100)));
        when(clockCommand.getClockTask()).thenReturn(clockTask);

        SetClockCommandImpl setClockCommand = new SetClockCommandImpl(clockCommand, commandRoot, null);
        assertThat(setClockCommand.toJournalMessageDescription(LogLevel.ERROR)).contains("{minimumDifference: 10s; maximumDifference: 100s}");
    }

}