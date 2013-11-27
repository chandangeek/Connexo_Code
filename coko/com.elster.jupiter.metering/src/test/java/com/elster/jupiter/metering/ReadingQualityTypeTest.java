package com.elster.jupiter.metering;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.google.common.collect.ImmutableList;

public class ReadingQualityTypeTest extends EqualsContractTest {

    private static final String CODE = "6.1";
    private ReadingQualityType a;

    @Override
    protected Object getInstanceA() {
        if (a == null) {
            a = new ReadingQualityType(CODE);
        }
        return a;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new ReadingQualityType(CODE);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(new ReadingQualityType("6.2"));
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
