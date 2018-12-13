/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import com.elster.jupiter.devtools.tests.EqualsContractTest;

import java.util.Arrays;

public class RationalNumberTest extends EqualsContractTest {

    private RationalNumber instanceA;

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = new RationalNumber(4, 5);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new RationalNumber(4, 5);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(new RationalNumber(4, 6), new RationalNumber(3, 5), new RationalNumber(1, 8));
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
