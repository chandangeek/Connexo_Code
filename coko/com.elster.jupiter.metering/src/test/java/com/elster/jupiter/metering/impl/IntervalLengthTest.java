package com.elster.jupiter.metering.impl;

public class IntervalLengthTest extends EqualsContractTest {

    private static final IntervalLength INTERVAL_LENGTH = IntervalLength.ofMinutes(15);

    @Override
    protected Object getInstanceA() {
        return INTERVAL_LENGTH;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return IntervalLength.ofMinutes(15);
    }

    @Override
    protected Object getInstanceNotEqualToA() {
        return IntervalLength.ofMinutes(10);
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
