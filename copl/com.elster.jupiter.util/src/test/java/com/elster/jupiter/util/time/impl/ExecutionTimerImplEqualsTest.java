package com.elster.jupiter.util.time.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.util.time.ExecutionTimer;

import java.time.Duration;
import java.util.Arrays;

import org.junit.BeforeClass;

public class ExecutionTimerImplEqualsTest extends EqualsContractTest {

    private static ExecutionTimer INSTANCE_A;

    @BeforeClass
    public static void setupInstances() {
        INSTANCE_A = new ExecutionTimerImpl("A", Duration.ofMillis(101L));
    }

    @Override
    protected Object getInstanceA() {
        return INSTANCE_A;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new ExecutionTimerImpl("A", Duration.ofMillis(101L));
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                new ExecutionTimerImpl("a", Duration.ofMillis(101L)),
                new ExecutionTimerImpl("B", Duration.ofMillis(101L)),
                new ExecutionTimerImpl("B", Duration.ofMillis(1001L))
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