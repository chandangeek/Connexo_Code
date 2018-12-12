/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionEventType;

import java.util.List;

/**
 * Adds behavior to the {@link FiniteStateMachineService} interface
 * that is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (13:22)
 */
public interface ServerFiniteStateMachineService extends FiniteStateMachineService {

    /**
     * Finds all {@link FiniteStateMachine}s that has a {@link com.elster.jupiter.fsm.StateTransition}
     * for the specified {@link StateTransitionEventType}.
     *
     * @param eventType The StateTransitionEventType
     * @return The List of FiniteStateMachine
     */
    public List<FiniteStateMachine> findFiniteStateMachinesUsing(StateTransitionEventType eventType);

}