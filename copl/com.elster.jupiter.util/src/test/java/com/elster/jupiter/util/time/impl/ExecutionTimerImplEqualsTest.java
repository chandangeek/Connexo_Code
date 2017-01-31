/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.util.time.ExecutionTimer;

import java.time.Duration;
import java.util.Arrays;

import org.junit.BeforeClass;

import static org.mockito.Mockito.mock;

public class ExecutionTimerImplEqualsTest extends EqualsContractTest {

    private static ExecutionTimer INSTANCE_A;

    private static IExecutionTimerService executionTimerService;

    @BeforeClass
    public static void setupInstances() {
        executionTimerService = mock(IExecutionTimerService.class);
        INSTANCE_A = new ExecutionTimerImpl(executionTimerService, "A", Duration.ofMillis(101L));
    }

    @Override
    protected Object getInstanceA() {
        return INSTANCE_A;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new ExecutionTimerImpl(executionTimerService, "A", Duration.ofMillis(101L));
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                new ExecutionTimerImpl(executionTimerService, "a", Duration.ofMillis(101L)),
                new ExecutionTimerImpl(executionTimerService, "B", Duration.ofMillis(101L)),
                new ExecutionTimerImpl(executionTimerService, "B", Duration.ofMillis(1001L))
        );
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