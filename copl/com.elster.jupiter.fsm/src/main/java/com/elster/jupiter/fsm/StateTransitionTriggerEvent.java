package com.elster.jupiter.fsm;

import java.time.Instant;
import java.util.Map;

/**
 * A StateTransitionTriggerEvent triggers the {@link StateTransition transition}
 * of the current {@link State} to another State.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (14:49)
 */
public interface StateTransitionTriggerEvent {

    /**
     * Gets the {@link FiniteStateMachine} to which this trigger applies.
     *
     * @return The FiniteStateMachine
     */
    public FiniteStateMachine getFiniteStateMachine();

    public StateTransitionEventType getType();

    /**
     * Gets the String that uniquely identifies the source of this event.
     *
     * @return The unique identifier of the source of this event
     */
    public String getSourceId();

    /**
     * Gets the name of the current state of the source of this event.
     *
     * @return The current state name
     * @see State#getName()
     */
    public String getSourceCurrentStateName();

    /**
     * The point in time when the resulting state change should be effective.
     *
     * @return The point in time when the resulting state change should be effective
     */
    public Instant getEffectiveTimestamp();

    public Map<String, Object> getProperties();

    public void publish();

}