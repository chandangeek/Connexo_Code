package com.elster.jupiter.fsm;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * A finate state machine sometimes also called finate state automaton or simply state machine,
 * is a mathematical model of computation and is conceived as an abstract machine that
 * can be in a finate number of states (hence the name).
 * The machine is in exactly one state at any time and that state is called the current state.
 * The state changes as events are {@link StateTransitionTriggerEvent triggered}.
 * The changing of state due to these events is referred to as a state transition.
 * Therefore, a state machine is defined by the list of its states and the {@link StateTransitionEventType}s
 * that trigger each transition.
 * One of the states is called the initial state, i.e. the state in which the machine is
 * when first created. A subset of the states can be final, indicating that once
 * the machine is in that state, the state will no longer change.
 * Every state that has no outgoing transitions is by consequence a final state.
 * Every change of state is published on the finate state machine's topic
 * as a {@link StateTransitionChangeEvent}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (11:45)
 */
public interface FinateStateMachine {

    public long getId();

    public long getVersion();

    /**
     * Gets the timestamp on which this FinateStateMachine was created.
     *
     * @return The creation timestamp
     */
    public Instant getCreationTimestamp();

    /**
     * Gets the timestamp on which this FinateStateMachine was last modified.
     *
     * @return The timestamp of last modification
     */
    public Instant getModifiedTimestamp();

    public String getName();

    public String getTopic();

    public List<State> getStates();

    public Optional<State> getState(String name);

    public List<StateTransition> getTransitions();

    public void save();

    public void delete();

}