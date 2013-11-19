package com.elster.jupiter.ids.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.google.common.collect.ImmutableList;

public class RecordSpecImplTest extends EqualsContractTest {

    private static final String NAME = "name";
    private static final int ID = 15;
    private static final String COMPONENT_NAME = "CMP";

    private RecordSpecImpl instanceA;

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = new RecordSpecImpl(COMPONENT_NAME, ID, NAME);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new RecordSpecImpl(COMPONENT_NAME, ID, NAME);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(new RecordSpecImpl(COMPONENT_NAME, ID + 1, NAME));
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
