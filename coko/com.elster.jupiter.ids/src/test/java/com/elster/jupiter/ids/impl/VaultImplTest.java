package com.elster.jupiter.ids.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.google.common.collect.ImmutableList;

public class VaultImplTest extends EqualsContractTest {

    private static final long ID = 15L;
    private static final int SLOT_COUNT = 54;
    private static final String COMPONENT_NAME = "CMP";
    private static final String DESCRIPTION = "description";
    private static final VaultImpl VAULT = new VaultImpl(COMPONENT_NAME, ID, DESCRIPTION, SLOT_COUNT, true);

    @Override
    protected Object getInstanceA() {
        return VAULT;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new VaultImpl(COMPONENT_NAME, ID, DESCRIPTION, SLOT_COUNT, true);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(new VaultImpl(COMPONENT_NAME, ID + 1, DESCRIPTION, SLOT_COUNT, true));
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
