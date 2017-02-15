/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.ClockTaskOptions;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.ComCommandDescriptionTitle;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.FakeIssueService;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.ClockTask;

import java.time.Clock;
import java.util.Date;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SetClockCommandImplTest {

    @Mock
    private DeviceProtocol deviceProtocol;

    @Test
    public void testToJournalMessageDescription() throws ClassNotFoundException {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot commandRoot = mock(CommandRoot.class);
        CommandRoot.ServiceProvider serviceProvider = mock(CommandRoot.ServiceProvider.class);
        when(commandRoot.getServiceProvider()).thenReturn(serviceProvider);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, device, deviceProtocol, null);
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(new TimeDuration(10)));
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(new TimeDuration(100)));
        ClockTaskOptions clockTaskOptions = new ClockTaskOptions(clockTask);
        when(clockCommand.getClockTaskOptions()).thenReturn(clockTaskOptions);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(new TimeDuration(45)));

        // Business methods
        SetClockCommandImpl setClockCommand = new SetClockCommandImpl(groupedDeviceCommand, clockCommand, null);
        String journalEntry = setClockCommand.toJournalMessageDescription(LogLevel.DEBUG);

        // Asserts
        assertEquals(ComCommandDescriptionTitle.SetClockCommandImpl.getDescription()
                + " {executionState: NOT_EXECUTED; completionCode: Ok; minimumDifference: 10 seconds; maximumDifference: 100 seconds; timeDifference: 45 seconds}", journalEntry);
    }

    @Test
    public void testClockIsSetWhenTimeDifferenceBetweenLimits() throws ClassNotFoundException {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);

        CommandRoot.ServiceProvider serviceProvider = mock(CommandRoot.ServiceProvider.class);
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());

        CommandRoot commandRoot = mock(CommandRoot.class);
        when(commandRoot.getServiceProvider()).thenReturn(serviceProvider);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, mock(OfflineDevice.class), deviceProtocol, null);

        ClockCommand clockCommand = mock(ClockCommand.class);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(TimeDuration.TimeUnit.MILLISECONDS.during(99999)));

        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(new TimeDuration(10)));
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(new TimeDuration(100)));

        ClockTaskOptions clockTaskOptions = new ClockTaskOptions(clockTask);
        when(clockCommand.getClockTaskOptions()).thenReturn(clockTaskOptions);

        SetClockCommandImpl setClockCommand = new SetClockCommandImpl(groupedDeviceCommand, clockCommand, null);

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
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, mock(OfflineDevice.class), deviceProtocol, null);

        ClockCommand clockCommand = mock(ClockCommand.class);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(TimeDuration.TimeUnit.MILLISECONDS.during(100001)));

        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(new TimeDuration(10)));
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(new TimeDuration(100)));

        ClockTaskOptions clockTaskOptions = new ClockTaskOptions(clockTask);
        when(clockCommand.getClockTaskOptions()).thenReturn(clockTaskOptions);

        SetClockCommandImpl setClockCommand = new SetClockCommandImpl(groupedDeviceCommand, clockCommand, null);

        setClockCommand.doExecute(deviceProtocol, null);

        assertThat(setClockCommand.getIssues()).hasSize(1);
    }
}