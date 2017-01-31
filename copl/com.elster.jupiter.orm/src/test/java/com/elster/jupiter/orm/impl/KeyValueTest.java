/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.google.common.collect.ImmutableList;

public class KeyValueTest extends EqualsContractTest {

    private static final KeyValue KEY_VALUE = KeyValue.of(new Object[]{"A", "B", "C"});

    @Override
    protected Object getInstanceA() {
        return KEY_VALUE;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return KeyValue.of(new Object[]{"A", "B", "C"});
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        KeyValue middleSpot = KeyValue.of(new Object[]{"A", "D", "C"});
        KeyValue firstSpot = KeyValue.of(new Object[]{"D", "B", "C"});
        KeyValue lastSpot = KeyValue.of(new Object[]{"A", "B", "D"});
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
