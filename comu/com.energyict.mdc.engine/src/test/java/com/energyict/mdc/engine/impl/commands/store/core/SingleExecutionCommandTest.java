/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.TimeDifferenceCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import org.joda.time.DateTime;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Date;

import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the SimpleComCommand component
 *
 * @author gna
 * @since 10/05/12 - 10:54
 */
public class SingleExecutionCommandTest extends CommonCommandImplTests {

    @Mock
    private OfflineDevice offlineDevice;

    @Test(expected = CodingException.class)
    public void noDeviceProtocolTest() {
        TimeDifferenceCommandImpl timeDifferenceCommand = new TimeDifferenceCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol));
        timeDifferenceCommand.execute(null, newTestExecutionContext());
        // should have gotten the codingException
    }

    @Test(expected = CodingException.class)
    public void noExecutionContextTest() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        TimeDifferenceCommandImpl timeDifferenceCommand = new TimeDifferenceCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol));
        timeDifferenceCommand.execute(deviceProtocol, null);
        // should have gotten the codingException
    }

    @Test
    public void executionTest() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(frozenClock);
        final long timeDifferenceInMillis = 1000L;
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);

        long deviceTime = frozenClock.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 1 seconds time difference
        TimeDifferenceCommandImpl timeDifferenceCommand = new TimeDifferenceCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol));

        assertFalse(timeDifferenceCommand.hasExecuted());
        assertEquals(BasicComCommandBehavior.ExecutionState.NOT_EXECUTED, timeDifferenceCommand.getExecutionState());

        timeDifferenceCommand.execute(deviceProtocol, newTestExecutionContext());

        assertTrue(timeDifferenceCommand.hasExecuted());
        assertEquals(BasicComCommandBehavior.ExecutionState.SUCCESSFULLY_EXECUTED, timeDifferenceCommand.getExecutionState());
        assertEquals(timeDifferenceInMillis, timeDifferenceCommand.getTimeDifference().get().getMilliSeconds());
    }

    @Test
    public void failedTest() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getTime()).thenThrow(new RuntimeException("Some exception to fail the reading of the timedifference"));
        TimeDifferenceCommandImpl timeDifferenceCommand = new TimeDifferenceCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol));

        try {
            timeDifferenceCommand.execute(deviceProtocol, newTestExecutionContext());
        } catch (Exception e) {
            // we should get here because of the RuntimeException
        }

        assertTrue(timeDifferenceCommand.hasExecuted());
        assertEquals(BasicComCommandBehavior.ExecutionState.FAILED, timeDifferenceCommand.getExecutionState());
    }

    @Test
    public void multipleExecutionTest() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(frozenClock);
        final long timeDifferenceInMillis = 1000L;
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);

        long deviceTime = frozenClock.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 1 seconds time difference
        TimeDifferenceCommandImpl timeDifferenceCommand = new TimeDifferenceCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol));
        ExecutionContext executionContext = newTestExecutionContext();
        timeDifferenceCommand.execute(deviceProtocol, executionContext);
        timeDifferenceCommand.execute(deviceProtocol, executionContext);
        timeDifferenceCommand.execute(deviceProtocol, executionContext);
        timeDifferenceCommand.execute(deviceProtocol, executionContext);

        // verify that the getTime is only called once
        verify(deviceProtocol).getTime();
        assertEquals(timeDifferenceInMillis, timeDifferenceCommand.getTimeDifference().get().getMilliSeconds());
    }
}