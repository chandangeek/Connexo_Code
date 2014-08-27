package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.impl.commands.collect.BasicCheckCommand;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.exceptions.TimeDifferenceExceededException;
import com.energyict.mdc.tasks.BasicCheckTask;

import com.elster.jupiter.util.time.Clock;
import org.joda.time.DateTime;

import java.util.Date;

import org.junit.*;

import static junit.framework.Assert.assertEquals;
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
    public void testToJournalMessageDescription () {
        BasicCheckCommand basicCheckCommand = mock(BasicCheckCommand.class);
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckCommand.getBasicCheckTask()).thenReturn(basicCheckTask);
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(new TimeDuration(100));
        VerifyTimeDifferenceCommandImpl command = new VerifyTimeDifferenceCommandImpl(basicCheckCommand, createCommandRoot());
        assertEquals("VerifyTimeDifferenceCommandImpl {maximumDifference: 100 seconds}", command.toJournalMessageDescription(LogLevel.ERROR));
    }

    @Test(expected = TimeDifferenceExceededException.class)
    public void timeDifferenceShouldFailAfterMaxClockDiffTest() {
        Date meterTime = new DateTime(2013, 9, 18, 16, 0, 0, 0).toDate();
        Clock systemTime = mock(Clock.class);
        when(systemTime.now()).thenReturn(new DateTime(2013, 9, 18, 15, 0, 0, 0).toDate());
        serviceProvider.setClock(systemTime);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getTime()).thenReturn(meterTime);
        BasicCheckCommand basicCheckCommand = mock(BasicCheckCommand.class);
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckCommand.getTimeDifference()).thenReturn(new TimeDuration(1, TimeDuration.HOURS));
        when(basicCheckCommand.getBasicCheckTask()).thenReturn(basicCheckTask);        when(basicCheckTask.getMaximumClockDifference()).thenReturn(new TimeDuration(1, TimeDuration.SECONDS));
        VerifyTimeDifferenceCommandImpl verifyTimeDifferenceCommand = new VerifyTimeDifferenceCommandImpl(basicCheckCommand, createCommandRoot());
        verifyTimeDifferenceCommand.execute(deviceProtocol, AbstractComCommandExecuteTest.newTestExecutionContext());
    }
}