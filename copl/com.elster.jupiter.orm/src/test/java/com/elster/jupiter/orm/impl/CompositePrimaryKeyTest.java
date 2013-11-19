package com.elster.jupiter.orm.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.google.common.collect.ImmutableList;

public class CompositePrimaryKeyTest extends EqualsContractTest {

    private static final CompositePrimaryKey COMPOSITE_PRIMARY_KEY = new CompositePrimaryKey("A", "B", "C");

    @Override
    protected Object getInstanceA() {
        return COMPOSITE_PRIMARY_KEY;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new CompositePrimaryKey("A", "B", "C");
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        CompositePrimaryKey middle = new CompositePrimaryKey("A", "R", "C");
        CompositePrimaryKey first = new CompositePrimaryKey("R", "B", "C");
        CompositePrimaryKey last = new CompositePrimaryKey("A", "B", "R");
        return ImmutableList.of(middle, first, last);
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
