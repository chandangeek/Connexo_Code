/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the equality aspects of a {@link ComCommandKey} that only has a {@link ComCommandType}
 * and no device, comTaskExecution or security command group.
 */
public class ComCommandKeyWithOnlyTypeEqualsTest extends EqualsContractTest {

    private static final long DEVICE_ID = 97L;
    private static final long COM_TASK_EXECUTION_ID = DEVICE_ID + 1;
    private static final long SECURITY_SET_GROUP_ID = COM_TASK_EXECUTION_ID + 1;

    private static ComCommandKey TEST_INSTANCE = new ComCommandKey(ComCommandTypes.SET_CLOCK_COMMAND, null);

    @Override
    protected Object getInstanceA() {
        return TEST_INSTANCE;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new ComCommandKey(ComCommandTypes.SET_CLOCK_COMMAND, null);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComTaskExecution comTaskExectuion = mock(ComTaskExecution.class);
        when(comTaskExectuion.getDevice()).thenReturn(device);
        when(comTaskExectuion.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        return Arrays.asList(
                new ComCommandKey(ComCommandTypes.BASIC_CHECK_COMMAND, null),
                new ComCommandKey(ComCommandTypes.BASIC_CHECK_COMMAND, device),
                new ComCommandKey(ComCommandTypes.BASIC_CHECK_COMMAND, comTaskExectuion, SECURITY_SET_GROUP_ID));
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

}