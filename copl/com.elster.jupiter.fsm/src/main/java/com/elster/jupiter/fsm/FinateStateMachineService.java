package com.elster.jupiter.fsm;

import com.elster.jupiter.events.EventType;

import java.util.Optional;

/**
 * Provides services to manage {@link FinateStateMachine}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:25)
 */
public interface FinateStateMachineService {

    String COMPONENT_NAME = "FSM";

    public void addStandardEventPredicate(StandardEventPredicate predicate);

    /**
     * Creates a new {@link CustomStateTransitionEventType} with the specified symbol.
     * Note that you are responsible for saving the CustomStateTransitionEventType.
     *
     * @param symbol The symbolic representation of the event
     * @return The CustomStateTransitionEventType
     */
    public CustomStateTransitionEventType newCustomStateTransitionEventType(String symbol);

    /**
     * Creates a new {@link StandardStateTransitionEventType} with the specified symbol.
     * Note that you are responsible for saving the StandardStateTransitionEventType.
     *
     * @param eventType The standard EventType
     * @return The StandardStateTransitionEventType
     */
    public StandardStateTransitionEventType newStandardStateTransitionEventType(EventType eventType);

    /**
     * Finds the {@link CustomStateTransitionEventType} with the specified symbolic representation
     * if such a CustomStateTransitionEventType exists.
     *
     * @param symbol The symbolic representation
     * @return The CustomStateTransitionEventType
     */
    public Optional<CustomStateTransitionEventType> findCustomStateTransitionEventType(String symbol);

    /**
     * Finds the {@link StandardStateTransitionEventType} for the specified {@link EventType}
     * if such a StandardStateTransitionEventType exists.
     *
     * @param eventType The standard EventType
     * @return The StandardStateTransitionEventType
     */
    public Optional<StandardStateTransitionEventType> findStandardStateTransitionEventType(EventType eventType);

    /**
     * Starts the building process for a new {@link FinateStateMachine}
     * with the specified name. The name must be unique across the system,
     * i.e. no two FinateStateMachines can have the same name.
     * Note that you are responsible for saving the FinateStateMachine.
     *
     * @param name The name
     * @param topic The name of the topic on which state change events will be published
     * @return The FinateStateMachineBuilder
     */
    public FinateStateMachineBuilder newFinateStateMachine(String name, String topic);

    /**
     * Finds the {@link FinateStateMachine} with the specified name
     * if such a FinateStateMachine exists.
     *
     * @param name The name
     * @return The FinateStateMachine
     */
    public Optional<FinateStateMachine> findFinateStateMachineByName(String name);

}