package com.energyict.mdc.engine.impl.commands.store.core;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.ProgrammableClock;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.CompositeComCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.TimeDifferenceCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the SimpleComCommand component
 *
 * @author gna
 * @since 10/05/12 - 10:54
 */
public class SingleExecutionCommandTest extends CommonCommandImplTests {

    private CommandRoot getMockedCommandRoot(){
        CommandRoot commandRoot = mock(CommandRoot.class);
        when(commandRoot.getTimeDifferenceCommand(Matchers.<CompositeComCommand>any(), any(ComTaskExecution.class))).thenReturn(new TimeDifferenceCommandImpl(commandRoot));
        return commandRoot;
    }


    @Test(expected = CodingException.class)
    public void noDeviceProtocolTest () {
        TimeDifferenceCommandImpl timeDifferenceCommand = new TimeDifferenceCommandImpl(getMockedCommandRoot());
        timeDifferenceCommand.execute(null, this.newTestExecutionContext());
        // should have gotten the codingException
    }

    @Test(expected = CodingException.class)
    public void noExecutionContextTest () {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        TimeDifferenceCommandImpl timeDifferenceCommand = new TimeDifferenceCommandImpl(getMockedCommandRoot());
        timeDifferenceCommand.execute(deviceProtocol, null);
        // should have gotten the codingException
    }

    @Test
    public void executionTest() {
        Clock frozenClock = new ProgrammableClock().frozenAt(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate());
        final long timeDifferenceInMillis = 1000L;
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);

        long deviceTime = frozenClock.now().getTime() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 1 seconds time difference
        TimeDifferenceCommandImpl timeDifferenceCommand = new TimeDifferenceCommandImpl(getMockedCommandRoot());

        assertFalse(timeDifferenceCommand.hasExecuted());
        assertEquals(SimpleComCommand.ExecutionState.NOT_EXECUTED, timeDifferenceCommand.getExecutionState());

        timeDifferenceCommand.execute(deviceProtocol, this.newTestExecutionContext());

        assertTrue(timeDifferenceCommand.hasExecuted());
        assertEquals(SimpleComCommand.ExecutionState.SUCCESSFULLY_EXECUTED, timeDifferenceCommand.getExecutionState());
        assertEquals(timeDifferenceInMillis, timeDifferenceCommand.getTimeDifference().getMilliSeconds());
    }

    @Test
    public void failedTest() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getTime()).thenThrow(new RuntimeException("Some exception to fail the reading of the timedifference"));
        TimeDifferenceCommandImpl timeDifferenceCommand = new TimeDifferenceCommandImpl(getMockedCommandRoot());

        try {
            timeDifferenceCommand.execute(deviceProtocol, this.newTestExecutionContext());
        } catch (Exception e) {
            // we should get here because of the RuntimeException
        }

        assertTrue(timeDifferenceCommand.hasExecuted());
        assertEquals(SimpleComCommand.ExecutionState.FAILED, timeDifferenceCommand.getExecutionState());
    }

    @Test
    public void multipleExecutionTest(){
        Clock frozenClock = new ProgrammableClock().frozenAt(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate());
        final long timeDifferenceInMillis = 1000L;
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);

        long deviceTime = frozenClock.now().getTime() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 1 seconds time difference
        TimeDifferenceCommandImpl timeDifferenceCommand = new TimeDifferenceCommandImpl(getMockedCommandRoot());
        ExecutionContext executionContext = this.newTestExecutionContext();
        timeDifferenceCommand.execute(deviceProtocol, executionContext);
        timeDifferenceCommand.execute(deviceProtocol, executionContext);
        timeDifferenceCommand.execute(deviceProtocol, executionContext);
        timeDifferenceCommand.execute(deviceProtocol, executionContext);

        // verify that the getTime is only called once
        verify(deviceProtocol).getTime();
        assertEquals(timeDifferenceInMillis, timeDifferenceCommand.getTimeDifference().getMilliSeconds());
    }

}
