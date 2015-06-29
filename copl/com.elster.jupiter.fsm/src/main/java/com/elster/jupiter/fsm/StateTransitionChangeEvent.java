package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Map;

/**
 * A StateTransitionChangeEvent notifies interested parties
 * that the current {@link State} of a {@link FiniteStateMachine}
 * for a particular source object has changed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-04 (09:44)
 */
@ProviderType
public interface StateTransitionChangeEvent {

    public State getOldState();

    public State getNewState();

    /**
     * Gets the String that uniquely identifies the source of this event.
     *
     * @return The unique identifier of the source of this event
     * @see StateTransitionTriggerEvent#getSourceId()
     */
    public String getSourceId();

    /**
     * The point in time when the state change is effective.
     *
     * @return The point in time when the state change is effective
     */
    public Instant getEffectiveTimestamp();

    /**
     * Gets properties that were provided by the {@link StateTransitionTriggerEvent}
     * that caused the State change.
     *
     * @return The properties
     * @see StateTransitionTriggerEvent#getProperties()
     */
    public Map<String, Object> getProperties();

}