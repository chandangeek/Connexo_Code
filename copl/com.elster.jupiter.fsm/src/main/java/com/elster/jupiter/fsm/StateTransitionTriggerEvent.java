package com.elster.jupiter.fsm;

import java.util.Map;

/**
 * A StateTransitionTriggerEvent triggers the current {@link State} of a
 * {@link FinateStateMachine} to {@link StateTransition transition} to another State.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (14:49)
 */
public interface StateTransitionTriggerEvent {

    /**
     * Gets the {@link FinateStateMachine} to which this trigger applies.
     *
     * @return The FinateStateMachine
     */
    public FinateStateMachine getFinateStateMachine();

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

    public Map<String, Object> getProperties();

    public void publish();

}