/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pubsub.Publisher;
import com.google.common.collect.ImmutableSet;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class FiniteStateMachineEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Clock clock;
    @Mock
    private Publisher publisher;

    private FiniteStateMachineImpl finiteStateMachine;

    @Override
    protected Object getInstanceA() {
        if (finiteStateMachine == null) {
            finiteStateMachine = new FiniteStateMachineImpl(dataModel, thesaurus, clock, publisher);
            setId(finiteStateMachine, ID);
        }
        return finiteStateMachine;
    }

    private void setId(FiniteStateMachine finiteStateMachine, Long id) {
        field("id").ofType(Long.TYPE).in(finiteStateMachine).set(id);
    }

    @Override
    protected Object getInstanceEqualToA() {
        FiniteStateMachineImpl finiteStateMachine = new FiniteStateMachineImpl(dataModel, thesaurus, clock, publisher);
        setId(finiteStateMachine, ID);
        return finiteStateMachine;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        FiniteStateMachineImpl finiteStateMachine = new FiniteStateMachineImpl(dataModel, thesaurus, clock, publisher);
        setId(finiteStateMachine, OTHER_ID);
        return ImmutableSet.of(finiteStateMachine);
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
