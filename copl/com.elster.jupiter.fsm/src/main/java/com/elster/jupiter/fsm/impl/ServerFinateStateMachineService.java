package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineService;
import com.elster.jupiter.fsm.StateTransitionEventType;

import java.util.List;

/**
 * Adds behavior to the {@link FinateStateMachineService} interface
 * that is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (13:22)
 */
public interface ServerFinateStateMachineService extends FinateStateMachineService {

    /**
     * Finds all {@link FinateStateMachine}s that has a {@link com.elster.jupiter.fsm.StateTransition}
     * for the specified {@link StateTransitionEventType}.
     *
     * @param eventType The StateTransitionEventType
     * @return The List of FinateStateMachine
     */
    public List<FinateStateMachine> findFinateStateMachinesUsing(StateTransitionEventType eventType);

}