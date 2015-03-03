package com.elster.jupiter.fsm;

import java.util.Map;

/**
 * A StateTransitionEvent triggers the current {@link State} of a {@link FinateStateMachine}
 * to {@link StateTransition transition} to another State.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (14:49)
 */
public interface StateTransitionEvent {

    public StateTransitionEventType getType();

    /**
     * Gets the String that uniquely identifies the source of this event.
     *
     * @return The unique identifier of the source of this event
     */
    public String getSourceId();

    public Map<String, Object> getProperties();

}