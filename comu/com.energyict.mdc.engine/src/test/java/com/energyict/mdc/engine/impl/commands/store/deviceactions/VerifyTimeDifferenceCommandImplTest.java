package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.collect.BasicCheckCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.tasks.BasicCheckTask;
import org.fest.assertions.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Test;

import java.time.Clock;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 9/07/13
 * Time: 10:09
 * Author: khe
 */
public class VerifyTimeDifferenceCommandImplTest extends CommonCommandImplTests {

    @Test
    public void testToJournalMessageDescription() {
        BasicCheckCommand basicCheckCommand = mock(BasicCheckCommand.class);
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckCommand.getBasicCheckTask()).thenReturn(basicCheckTask);
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(Optional.of(new TimeDuration(100)));
        VerifyTimeDifferenceCommandImpl command = new VerifyTimeDifferenceCommandImpl(basicCheckCommand, createCommandRoot());
        assertThat(command.toJournalMessageDescription(LogLevel.ERROR)).contains("{maximumDifference: 100 seconds}");
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
        when(basicCheckCommand.getBasicCheckTask()).thenReturn(basicCheckTask);
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(Optional.of(TimeDuration.seconds(1)));
        VerifyTimeDifferenceCommandImpl verifyTimeDifferenceCommand = new VerifyTimeDifferenceCommandImpl(basicCheckCommand, createCommandRoot());

        verifyTimeDifferenceCommand.execute(deviceProtocol, this.newTestExecutionContext());

        Assertions.assertThat(verifyTimeDifferenceCommand.getIssues().size()).isEqualTo(1);
        Assertions.assertThat(verifyTimeDifferenceCommand.getIssues().get(0).getDescription()).isEqualTo("maxTimeDiffExceeded");
        Assertions.assertThat(verifyTimeDifferenceCommand.getCompletionCode()).isEqualTo(CompletionCode.TimeError);
    }

}