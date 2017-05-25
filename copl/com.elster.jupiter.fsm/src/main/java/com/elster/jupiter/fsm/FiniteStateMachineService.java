/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm;

import com.elster.jupiter.events.EventType;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

/**
 * Provides services to manage {@link FiniteStateMachine}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:25)
 */
@ProviderType
public interface FiniteStateMachineService {

    String COMPONENT_NAME = "FSM";

    /**
     * Returns the topic onto which {@link StateTransitionChangeEvent}s are published.
     *
     * @return The topic
     */
    String stateTransitionChangeEventTopic();

    void addStandardEventPredicate(StandardEventPredicate predicate);

    /**
     * Creates a new {@link CustomStateTransitionEventType} with the specified symbol.
     *
     * @param symbol The symbolic representation of the event
     * @param context Name in which this custom event type can be used, e.g. devicelifecycle, servicecalllifecycle.
     * @return The CustomStateTransitionEventType
     */
    CustomStateTransitionEventType newCustomStateTransitionEventType(String symbol, String context);

    /**
     * Creates a new {@link StandardStateTransitionEventType} with the specified symbol.
     *
     * @param eventType The standard EventType
     * @return The StandardStateTransitionEventType
     */
    StandardStateTransitionEventType newStandardStateTransitionEventType(EventType eventType);

    /**
     * Finds the {@link CustomStateTransitionEventType} with the specified symbolic representation
     * if such a CustomStateTransitionEventType exists.
     *
     * @param symbol The symbolic representation
     * @return The CustomStateTransitionEventType
     */
    Optional<CustomStateTransitionEventType> findCustomStateTransitionEventType(String symbol);

    /**
     * Finds the {@link StandardStateTransitionEventType} for the specified {@link EventType}
     * if such a StandardStateTransitionEventType exists.
     *
     * @param eventType The standard EventType
     * @return The StandardStateTransitionEventType
     */
    Optional<StandardStateTransitionEventType> findStandardStateTransitionEventType(EventType eventType);

    /**
     * Finds the {@link StateTransitionEventType} with the specified unique symbolic representation.
     *
     * @param symbol The symbolic representation
     * @see StateTransitionEventType#getSymbol()
     * @return The StateTransitionEventType
     */
    Optional<StateTransitionEventType> findStateTransitionEventTypeBySymbol(String symbol);

    /**
     * Gets all the {@link StateTransitionEventType} that have been defined in the system.
     *
     * @return The List of StateTransitionEventType
     * @param context    The context/component for which event types are to be retrieved. For standard event types, this would be 'System'
     */
    List<StateTransitionEventType> getStateTransitionEventTypes(String... context);


    /**
     * Starts the building process for a new {@link StageSet}
     * with the specified name. The name must be unique across the system
     *
     * @param name The name of the {@link StageSet}
     * @return The {@link StageSetBuilder}
     */
    StageSetBuilder newStageSet(String name);

    /**
     * Starts the building process for a new {@link FiniteStateMachine}
     * with the specified name. The name must be unique across the system,
     * i.e. no two FiniteStateMachines can have the same name.
     *
     * @param name The name
     * @return The FiniteStateMachineBuilder
     */
    FiniteStateMachineBuilder newFiniteStateMachine(String name);

    /**
     * Starts the building process for a new {@link FiniteStateMachine}
     * with the specified name and the specified {@link StageSet}. The name must be unique across the system,
     * i.e. no two FiniteStateMachines can have the same name.
     *
     * @param name The name
     * @param stageSet The {@link StageSet} for the FiniteStateMachine
     * @return The FiniteStateMachineBuilder
     */
    FiniteStateMachineBuilder newFiniteStateMachine(String name, StageSet stageSet);

    /**
     * Clones the specified {@link FiniteStateMachine} with the specified name.
     * The name must be unique across the system,
     * i.e. no two FiniteStateMachines can have the same name.
     * Note that the cloned FiniteStateMachine has already been saved.
     *
     * @param source The FiniteStateMachine that is being cloned
     * @param name The name
     * @return The cloned FiniteStateMachine
     */
    FiniteStateMachine cloneFiniteStateMachine(FiniteStateMachine source, String name);

    /**
     * Finds the {@link FiniteStateMachine} that is uniquely identified
     * by the specified id.
     *
     * @param id The unique identifier
     * @return The FiniteStateMachine
     */
    Optional<FiniteStateMachine> findFiniteStateMachineById(long id);

    /**
     * Finds the {@link FiniteStateMachine} with the specified name
     * if such a FiniteStateMachine exists.
     *
     * @param name The name
     * @return The FiniteStateMachine
     */
    Optional<FiniteStateMachine> findFiniteStateMachineByName(String name);

    /**
     * Finds the {@link StageSet} with the specified name
     * if such a StageSet exists.
     *
     * @param name The name
     * @return The optional StageSet
     */
    Optional<StageSet> findStageSetByName(String name);

    /**
     * Finds the {@link State} that is uniquely identified
     * by the specified id.
     *
     * @param id The unique identifier
     * @return The State
     */
    Optional<State> findFiniteStateById(long id);

    Optional<State> findAndLockStateByIdAndVersion(long id, long version);
}