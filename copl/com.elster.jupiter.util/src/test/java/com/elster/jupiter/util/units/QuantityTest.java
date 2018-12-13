/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.units;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

public class QuantityTest extends EqualsContractTest {

    private Quantity instanceA;

    @Test
    public void testName() {
    	for (Unit unit : Unit.values()) {
    		Quantity quantity = unit.amount(BigDecimal.ONE);
    		quantity.asSi();
    	}
    }

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = Unit.AMPERE.amount(BigDecimal.valueOf(512, 2));
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return Unit.AMPERE.amount(BigDecimal.valueOf(5120, 3));
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(Unit.VOLT.amount(BigDecimal.valueOf(512, 2)), Unit.AMPERE.amount(BigDecimal.valueOf(513, 2)));
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
