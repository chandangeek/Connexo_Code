/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the equality aspects of a {@link ComCommandKey} that has a {@link ComCommandType}
 * and a device but no comTaskExecution or security command group.
 */
public class ComCommandKeyEqualsTest extends EqualsContractTest {

    private static final long DEVICE_ID = 97L;
    private static final long OTHER_DEVICE_ID = DEVICE_ID + 1;
    private static final long COM_TASK_EXECUTION_ID = OTHER_DEVICE_ID + 1;
    private static final long OTHER_COM_TASK_EXECUTION_ID = COM_TASK_EXECUTION_ID + 1;
    private static final long SECURITY_SET_GROUP_ID = COM_TASK_EXECUTION_ID + 1;
    private static final long OTHER_SECURITY_SET_GROUP_ID = SECURITY_SET_GROUP_ID + 1;

    private static final Device device;
    private static final ComTaskExecution comTaskExecution;
    private static final ComCommandKey TEST_INSTANCE;

    static {
        device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        TEST_INSTANCE = new ComCommandKey(ComCommandTypes.SET_CLOCK_COMMAND, comTaskExecution, SECURITY_SET_GROUP_ID);
    }

    @Override
    protected Object getInstanceA() {
        return TEST_INSTANCE;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new ComCommandKey(ComCommandTypes.SET_CLOCK_COMMAND, comTaskExecution, SECURITY_SET_GROUP_ID);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        Device otherDevice = mock(Device.class);
        when(otherDevice.getId()).thenReturn(OTHER_DEVICE_ID);
        ComTaskExecution otherComTaskExecution = mock(ComTaskExecution.class);
        when(otherComTaskExecution.getDevice()).thenReturn(otherDevice);
        when(otherComTaskExecution.getId()).thenReturn(OTHER_COM_TASK_EXECUTION_ID);
        return Arrays.asList(
                new ComCommandKey(ComCommandTypes.BASIC_CHECK_COMMAND, null),
                new ComCommandKey(ComCommandTypes.SET_CLOCK_COMMAND, otherDevice),
                new ComCommandKey(ComCommandTypes.SET_CLOCK_COMMAND, comTaskExecution, OTHER_SECURITY_SET_GROUP_ID),
                new ComCommandKey(ComCommandTypes.SET_CLOCK_COMMAND, otherComTaskExecution, SECURITY_SET_GROUP_ID),
                new ComCommandKey(ComCommandTypes.BASIC_CHECK_COMMAND, otherComTaskExecution, SECURITY_SET_GROUP_ID));
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    @Test
    public void testEqualsIgnoreComTaskExecution() {
        ComTaskExecution otherComTaskExecution = mock(ComTaskExecution.class);
        when(otherComTaskExecution.getDevice()).thenReturn(device);
        when(otherComTaskExecution.getId()).thenReturn(OTHER_COM_TASK_EXECUTION_ID);
        ComCommandKey withOtherCTE = new ComCommandKey(ComCommandTypes.SET_CLOCK_COMMAND, otherComTaskExecution, SECURITY_SET_GROUP_ID);
        ComCommandKey alsoEqualToA = new ComCommandKey(ComCommandTypes.SET_CLOCK_COMMAND, comTaskExecution, SECURITY_SET_GROUP_ID);

        // Assert equality ignoring the ComTaskExecution
        assertThat(TEST_INSTANCE.equalsIgnoreComTaskExecution(withOtherCTE)).isTrue();

        // Assert equality ignoring the ComTaskExecution is symmetrical
        assertThat(TEST_INSTANCE.equalsIgnoreComTaskExecution(withOtherCTE) == withOtherCTE.equalsIgnoreComTaskExecution(TEST_INSTANCE))
                .describedAs("equalsIgnoreComTaskExecution is not symmetrical")
                .isTrue();

        // Assert equality ignoring the ComTaskExecution is reflexive
        assertThat(TEST_INSTANCE.equalsIgnoreComTaskExecution(TEST_INSTANCE))
                .describedAs("equalsIgnoreComTaskExecution is not reflexive")
                .isTrue();

        // Assert equality ignoring the ComTaskExecution is transitive
        assertThat(    TEST_INSTANCE.equalsIgnoreComTaskExecution(withOtherCTE)
                    && withOtherCTE.equalsIgnoreComTaskExecution(alsoEqualToA)
                    && TEST_INSTANCE.equalsIgnoreComTaskExecution(alsoEqualToA))
                .describedAs("equalsIgnoreComTaskExecution is not transitive.")
                .isTrue();
    }

    @Test
    public void testNotEqualEvenWhenIgnoringComTaskExecution() {
        Device otherDevice = mock(Device.class);
        when(otherDevice.getId()).thenReturn(OTHER_DEVICE_ID);
        ComTaskExecution otherComTaskExecution = mock(ComTaskExecution.class);
        when(otherComTaskExecution.getDevice()).thenReturn(device);
        when(otherComTaskExecution.getId()).thenReturn(OTHER_COM_TASK_EXECUTION_ID);

        assertThat(TEST_INSTANCE.equalsIgnoreComTaskExecution(new ComCommandKey(ComCommandTypes.BASIC_CHECK_COMMAND, null))).isFalse();
        assertThat(TEST_INSTANCE.equalsIgnoreComTaskExecution(new ComCommandKey(ComCommandTypes.SET_CLOCK_COMMAND, null))).isFalse();
        assertThat(TEST_INSTANCE.equalsIgnoreComTaskExecution(new ComCommandKey(ComCommandTypes.SET_CLOCK_COMMAND, otherDevice))).isFalse();
        assertThat(TEST_INSTANCE.equalsIgnoreComTaskExecution(new ComCommandKey(ComCommandTypes.BASIC_CHECK_COMMAND, otherComTaskExecution, SECURITY_SET_GROUP_ID))).isFalse();
        assertThat(TEST_INSTANCE.equalsIgnoreComTaskExecution(new ComCommandKey(ComCommandTypes.BASIC_CHECK_COMMAND, comTaskExecution, OTHER_SECURITY_SET_GROUP_ID))).isFalse();
    }

    @Test
    public void testEqualsIgnoreComTaskExecutionWithNull() {
        assertThat(TEST_INSTANCE.equalsIgnoreComTaskExecution(null)).isFalse();
    }

}