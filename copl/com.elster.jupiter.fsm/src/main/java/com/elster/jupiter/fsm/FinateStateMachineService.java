package com.elster.jupiter.fsm;

import java.util.Optional;

/**
 * Provides services to manage {@link FinateStateMachine}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:25)
 */
public interface FinateStateMachineService {

    String COMPONENT_NAME = "FSM";

    /**
     * Creates a new {@link StateTransitionEventType} with the specified symbol.
     * Note that you are responsible for saving the StateTransitionEventType.
     *
     * @param symbol The symbolic representation of the event
     * @return The StateTransitionEventType
     */
    public StateTransitionEventType newStateTransitionEventType(String symbol);

    /**
     * Finds the {@link StateTransitionEventType} with the specified symbolic representation
     * if such a StateTransitionEventType exists.
     *
     * @param symbol The symbolic representation
     * @return The StateTransitionEventType
     */
    public Optional<StateTransitionEventType> findStateTransitionEventTypeBySymbol(String symbol);

    /**
     * Starts the building process for a new {@link FinateStateMachine}
     * with the specified name. The name must be unique across the system,
     * i.e. no two FinateStateMachines can have the same name.
     * Note that you are responsible for saving the FinateStateMachine.
     *
     * @param name The name
     * @return The FinateStateMachineBuilder
     */
    public FinateStateMachineBuilder newFinateStateMachine(String name);

    /**
     * Finds the {@link FinateStateMachine} with the specified name
     * if such a FinateStateMachine exists.
     *
     * @param name The name
     * @return The FinateStateMachine
     */
    public Optional<FinateStateMachine> findFinateStateMachineByName(String name);

}