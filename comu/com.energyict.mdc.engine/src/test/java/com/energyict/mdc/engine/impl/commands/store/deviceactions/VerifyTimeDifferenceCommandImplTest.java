package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.comserver.logging.LogLevel;
import com.energyict.comserver.time.Clocks;
import com.energyict.comserver.time.FrozenClock;
import com.energyict.mdc.commands.BasicCheckCommand;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.test.MockEnvironmentTranslations;
import java.util.Calendar;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 9/07/13
 * Time: 10:09
 * Author: khe
 */
@RunWith(MockitoJUnitRunner.class)
public class VerifyTimeDifferenceCommandImplTest extends CommonCommandImplTests {

    @ClassRule
    public static TestRule mockEnvironmentTranslactions = new MockEnvironmentTranslations();

    @Test
    public void testToJournalMessageDescription () {
        BasicCheckCommand basicCheckCommand = mock(BasicCheckCommand.class);
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckCommand.getBasicCheckTask()).thenReturn(basicCheckTask);
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(new TimeDuration(100));
        VerifyTimeDifferenceCommandImpl command = new VerifyTimeDifferenceCommandImpl(basicCheckCommand, createCommandRoot());
        assertEquals("VerifyTimeDifferenceCommandImpl {maximumDifference: 100 seconds}", command.toJournalMessageDescription(LogLevel.ERROR));
    }

    @Test(expected = DeviceConfigurationException.class)
    public void timeDifferenceShouldFailAfterMaxClockDiffTest() {
        FrozenClock meterTime = FrozenClock.frozenOn(2013, Calendar.SEPTEMBER, 18, 16, 0, 0, 0);
        FrozenClock systemTime = FrozenClock.frozenOn(2013, Calendar.SEPTEMBER, 18, 15, 0, 0, 0);
        Clocks.setAppServerClock(systemTime);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getTime()).thenReturn(meterTime.now());
        BasicCheckCommand basicCheckCommand = mock(BasicCheckCommand.class);
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckCommand.getTimeDifference()).thenReturn(new TimeDuration(1, TimeDuration.HOURS));
        when(basicCheckCommand.getBasicCheckTask()).thenReturn(basicCheckTask);        when(basicCheckTask.getMaximumClockDifference()).thenReturn(new TimeDuration(1, TimeDuration.SECONDS));
        VerifyTimeDifferenceCommandImpl verifyTimeDifferenceCommand = new VerifyTimeDifferenceCommandImpl(basicCheckCommand, createCommandRoot());
        try {
            verifyTimeDifferenceCommand.execute(deviceProtocol, AbstractComCommandExecuteTest.newTestExecutionContext());
        } catch (DeviceConfigurationException e) {
            if(!e.getMessageId().equals("CSC-CONF-134")){
                Assertions.fail("Should have gotten exception indicating that the timeDifference is to large, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }
}