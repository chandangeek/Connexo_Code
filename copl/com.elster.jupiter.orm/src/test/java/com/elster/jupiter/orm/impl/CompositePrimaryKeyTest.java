package com.elster.jupiter.orm.impl;

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
    protected Object getInstanceNotEqualToA() {
        return new CompositePrimaryKey("A", "R", "C");
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
