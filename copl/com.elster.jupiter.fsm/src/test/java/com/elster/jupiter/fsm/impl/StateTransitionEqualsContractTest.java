/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.fsm.StateTransition;
import com.google.common.collect.ImmutableSet;

import static org.fest.reflect.core.Reflection.field;

public class StateTransitionEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    private StateTransitionImpl stateTransition;

    private void setId(StateTransition stateTransition, Long id) {
        field("id").ofType(Long.TYPE).in(stateTransition).set(id);
    }

    @Override
    protected Object getInstanceA() {
        if (stateTransition == null) {
            stateTransition = new StateTransitionImpl();
            setId(stateTransition, ID);
        }
        return stateTransition;
    }

    @Override
    protected Object getInstanceEqualToA() {
        StateTransition stateTransition = new StateTransitionImpl();
        setId(stateTransition, ID);
        return stateTransition;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        StateTransition stateTransition = new StateTransitionImpl();
        setId(stateTransition, OTHER_ID);
        return ImmutableSet.of(stateTransition);
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
