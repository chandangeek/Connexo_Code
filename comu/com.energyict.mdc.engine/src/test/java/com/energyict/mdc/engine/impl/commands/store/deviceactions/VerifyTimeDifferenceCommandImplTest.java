/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.collect.BasicCheckCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.ComCommandDescriptionTitle;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.BasicCheckTask;

import org.fest.assertions.api.Assertions;
import org.joda.time.DateTime;

import java.time.Clock;
import java.util.Date;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerifyTimeDifferenceCommandImplTest extends CommonCommandImplTests {

    @Mock
    private OfflineDevice offlineDevice;

    @Test
    public void testToJournalMessageDescription() {
        BasicCheckCommand basicCheckCommand = mock(BasicCheckCommand.class);
        when(basicCheckCommand.getMaximumClockDifference()).thenReturn(Optional.of(new TimeDuration(100)));
        VerifyTimeDifferenceCommandImpl command = new VerifyTimeDifferenceCommandImpl(basicCheckCommand, createGroupedDeviceCommand(offlineDevice, deviceProtocol));
        assertEquals(ComCommandDescriptionTitle.VerifyTimeDifferenceCommandImpl.getDescription() + " {maximumDifference: 100 seconds}", command.toJournalMessageDescription(LogLevel.ERROR));
    }

    @Test
    public void timeDifferenceShouldFailAfterMaxClockDiffTest() {
        Date meterTime = new DateTime(2013, 9, 18, 16, 0, 0, 0).toDate();
        Clock systemTime = mock(Clock.class);
        when(systemTime.instant()).thenReturn(new DateTime(2013, 9, 18, 15, 0, 0, 0).toDate().toInstant());
        when(executionContextServiceProvider.clock()).thenReturn(systemTime);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getTime()).thenReturn(meterTime);
        BasicCheckCommand basicCheckCommand = mock(BasicCheckCommand.class);
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckCommand.getTimeDifference()).thenReturn(Optional.of(new TimeDuration(1, TimeDuration.TimeUnit.HOURS)));
        when(basicCheckCommand.getMaximumClockDifference()).thenReturn(Optional.of(TimeDuration.seconds(1)));
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(Optional.of(TimeDuration.seconds(1)));
        VerifyTimeDifferenceCommandImpl verifyTimeDifferenceCommand = new VerifyTimeDifferenceCommandImpl(basicCheckCommand, createGroupedDeviceCommand(offlineDevice, deviceProtocol));
        verifyTimeDifferenceCommand.execute(deviceProtocol, newTestExecutionContext());

        Assertions.assertThat(verifyTimeDifferenceCommand.getIssues().size()).isEqualTo(1);
        Assertions.assertThat(verifyTimeDifferenceCommand.getIssues().get(0).getDescription()).isEqualTo("maxTimeDiffExceeded");
        Assertions.assertThat(verifyTimeDifferenceCommand.getCompletionCode()).isEqualTo(CompletionCode.TimeError);
    }
}