/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableList;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class StateTransitionEventTypeEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ServerFiniteStateMachineService finiteStateMachineService;

    private StateTransitionEventType stateTransitionEventType;

    private static final class ConcreteStateTransitionEventType extends StateTransitionEventTypeImpl {
        ConcreteStateTransitionEventType(DataModel dataModel, Thesaurus thesaurus, ServerFiniteStateMachineService finiteStateMachineService) {
            super(dataModel, thesaurus, finiteStateMachineService);
        }

        @Override
        public String getSymbol() {
            return "symbol";
        }
    }

    private void setId(StateTransitionEventType eventType, Long id) {
        field("id").ofType(Long.TYPE).in(eventType).set(id);
    }

    @Override
    protected Object getInstanceA() {
        if (stateTransitionEventType == null) {
            stateTransitionEventType = new ConcreteStateTransitionEventType(dataModel, thesaurus, finiteStateMachineService);
            setId(stateTransitionEventType, ID);
        }
        return stateTransitionEventType;
    }

    @Override
    protected Object getInstanceEqualToA() {
        StateTransitionEventType stateTransitionEventType = new ConcreteStateTransitionEventType(dataModel, thesaurus, finiteStateMachineService);
        setId(stateTransitionEventType, ID);
        return stateTransitionEventType;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        StateTransitionEventType stateTransitionEventType = new ConcreteStateTransitionEventType(dataModel, thesaurus, finiteStateMachineService);
        setId(stateTransitionEventType, OTHER_ID);
        return ImmutableList.of(stateTransitionEventType);
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
