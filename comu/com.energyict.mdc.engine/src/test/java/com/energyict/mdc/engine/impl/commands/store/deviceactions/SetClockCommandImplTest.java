package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.FakeIssueService;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.tasks.ClockTask;

import java.time.Clock;
import java.util.Date;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
        assertThat(setClockCommand.toJournalMessageDescription(LogLevel.ERROR)).contains("{minimumDifference: 10000ms; maximumDifference: 100000ms}");
    }

    @Test
    public void testClockIsSetWhenTimeDifferenceBetweenLimits() throws ClassNotFoundException {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);

        CommandRoot.ServiceProvider serviceProvider = mock(CommandRoot.ServiceProvider.class);
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());

        CommandRoot commandRoot = mock(CommandRoot.class);
        when(commandRoot.getServiceProvider()).thenReturn(serviceProvider);

        ClockCommand clockCommand = mock(ClockCommand.class);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(TimeDuration.TimeUnit.MILLISECONDS.during(99999)));

        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(new TimeDuration(10)));
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(new TimeDuration(100)));

        when(clockCommand.getClockTask()).thenReturn(clockTask);

        SetClockCommandImpl setClockCommand = new SetClockCommandImpl(clockCommand, commandRoot, null);

        setClockCommand.doExecute(deviceProtocol, null);

        verify(deviceProtocol).setTime(any(Date.class));
    }

    @Test
    public void testIssueIsCreatedWhenTimeDifferenceAboveMax() throws ClassNotFoundException {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);

        CommandRoot.ServiceProvider serviceProvider = mock(CommandRoot.ServiceProvider.class);
        when(serviceProvider.issueService()).thenReturn(new FakeIssueService());

        CommandRoot commandRoot = mock(CommandRoot.class);
        when(commandRoot.getServiceProvider()).thenReturn(serviceProvider);

        ClockCommand clockCommand = mock(ClockCommand.class);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(TimeDuration.TimeUnit.MILLISECONDS.during(100001)));

        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(new TimeDuration(10)));
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(new TimeDuration(100)));

        when(clockCommand.getClockTask()).thenReturn(clockTask);

        SetClockCommandImpl setClockCommand = new SetClockCommandImpl(clockCommand, commandRoot, null);

        setClockCommand.doExecute(deviceProtocol, null);

        assertThat(setClockCommand.getIssues()).hasSize(1);
    }

}