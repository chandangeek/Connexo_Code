/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * A finite state machine, sometimes also called finite state automaton or simply state machine,
 * is a mathematical model of computation and is conceived as an abstract machine that
 * can be in a finite number of states (hence the name).
 * The machine is in exactly one state at any time and that state is called the current state.
 * The state changes as events are {@link StateTransitionTriggerEvent triggered}.
 * The changing of state due to these events is referred to as a state transition.
 * Therefore, a state machine is defined by the list of its states and the {@link StateTransitionEventType}s
 * that trigger each transition.
 * One of the states is called the initial state, i.e. the state in which the machine is
 * when first created. A subset of the states can be final, indicating that once
 * the machine is in that state, the state will no longer change.
 * Every state that has no outgoing transitions is by consequence a final state.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (11:45)
 */
@ProviderType
public interface FiniteStateMachine {

    long getId();

    long getVersion();

    /**
     * Gets the timestamp on which this FiniteStateMachine was created.
     *
     * @return The creation timestamp
     */
    Instant getCreationTimestamp();

    /**
     * Gets the timestamp on which this FiniteStateMachine was last modified.
     *
     * @return The timestamp of last modification
     */
    Instant getModifiedTimestamp();

    /**
     * Tests if this FiniteStateMachine was previously marked as obsolete.
     *
     * @return A flag that indicates if this FiniteStateMachine has been marked as obsolete
     */
    boolean isObsolete();

    /**
     * Gets the timestamp on which this FiniteStateMachine was
     * marked as obsolete.
     *
     * @return The obsolete timestamp
     */
    Instant getObsoleteTimestamp();

    String getName();

    List<State> getStates();

    /**
     * Gets the initial {@link State}.
     *
     * @return The initial State
     * @see State#isInitial()
     */
    State getInitialState();

    Optional<State> getState(String name);

    List<StateTransition> getTransitions();

    FiniteStateMachineUpdater startUpdate();

    /**
     * Saves this FiniteStateMachine after having applied changes to it
     * via a {@link FiniteStateMachineUpdater}.
     */
    void update();

    /**
     * Marks this FiniteStateMachine as obsolete
     * so that it can no longer be used.
     */
    void makeObsolete();

    /**
     * Deletes this FiniteStateMachine,
     * along with its {@link State}s and {@link StateTransition transitions}.
     * This is only intended for cleanup operations,
     * it makes not attempt to cleanup references to this
     * FiniteStateMachine outside of the finite state machine bundle.
     */
    void delete();

}