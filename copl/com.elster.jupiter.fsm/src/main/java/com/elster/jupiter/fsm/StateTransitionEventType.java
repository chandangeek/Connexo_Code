package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * A StateTransitionEventType models a {@link StateTransitionTriggerEvent}
 * that triggers the current {@link State} of a {@link FiniteStateMachine}
 * to {@link StateTransition transition} to another State.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (12:50)
 */
@ProviderType
public interface StateTransitionEventType {

    public long getId();

    /**
     * Gets the symbolic representation of this event
     * that is guaranteed not to change over time.
     *
     * @return The symbolic representation
     */
    public String getSymbol();

    public long getVersion();

    /**
     * Gets the timestamp on which this FiniteStateMachine was created.
     *
     * @return The creation timestamp
     */
    public Instant getCreationTimestamp();

    /**
     * Gets the timestamp on which this FiniteStateMachine was last modified.
     *
     * @return The timestamp of last modification
     */
    public Instant getModifiedTimestamp();

    public void save();

    public void delete();

}