package com.elster.jupiter.orm.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.google.common.collect.ImmutableList;

public class ArrayWrapperTest extends EqualsContractTest {

    private static final TableCache.ArrayWrapper ARRAY_WRAPPER = new TableCache.ArrayWrapper(new Object[]{"A", "B", "C"});

    @Override
    protected Object getInstanceA() {
        return ARRAY_WRAPPER;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new TableCache.ArrayWrapper(new Object[]{"A", "B", "C"});
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        TableCache.ArrayWrapper middleSpot = new TableCache.ArrayWrapper(new Object[]{"A", "D", "C"});
        TableCache.ArrayWrapper firstSpot = new TableCache.ArrayWrapper(new Object[]{"D", "B", "C"});
        TableCache.ArrayWrapper lastSpot = new TableCache.ArrayWrapper(new Object[]{"A", "B", "D"});
        return ImmutableList.of(middleSpot, firstSpot, lastSpot);
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
