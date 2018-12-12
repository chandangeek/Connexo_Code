/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableSet;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class StateEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Clock clock;

    private StateImpl state;

    private void setId(State state, Long id) {
        field("id").ofType(Long.TYPE).in(state).set(id);
    }

    @Override
    protected Object getInstanceA() {
        if (state == null) {
            state = new StateImpl(dataModel, thesaurus, clock);
            setId(state, ID);
        }
        return state;
    }

    @Override
    protected Object getInstanceEqualToA() {
        State state = new StateImpl(dataModel, thesaurus, clock);
        setId(state, ID);
        return state;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        State state = new StateImpl(dataModel, thesaurus, clock);
        setId(state, OTHER_ID);
        return ImmutableSet.of(state);
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
