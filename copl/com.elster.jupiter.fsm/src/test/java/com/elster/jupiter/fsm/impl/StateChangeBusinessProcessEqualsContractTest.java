/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableSet;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class StateChangeBusinessProcessEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    @Mock
    private DataModel dataModel;

    private StateChangeBusinessProcess stateChangeBusinessProcess;

    private void setId(StateChangeBusinessProcess stateChangeBusinessProcess, Long id) {
        field("id").ofType(Long.TYPE).in(stateChangeBusinessProcess).set(id);
    }

    @Override
    protected Object getInstanceA() {
        if (stateChangeBusinessProcess == null) {
            stateChangeBusinessProcess = new StateChangeBusinessProcessImpl(dataModel);
            setId(stateChangeBusinessProcess, ID);
        }
        return stateChangeBusinessProcess;
    }

    @Override
    protected Object getInstanceEqualToA() {
        StateChangeBusinessProcess stateChangeBusinessProcess = new StateChangeBusinessProcessImpl(dataModel);
        setId(stateChangeBusinessProcess, ID);
        return stateChangeBusinessProcess;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        StateChangeBusinessProcess stateChangeBusinessProcess = new StateChangeBusinessProcessImpl(dataModel);
        setId(stateChangeBusinessProcess, OTHER_ID);
        return ImmutableSet.of(stateChangeBusinessProcess);
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
