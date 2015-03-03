package com.elster.jupiter.fsm;

import java.time.Instant;
import java.util.Map;

/**
 * A StateTransitionEventType models a {@link StateTransitionEvent}
 * that triggers the current {@link State} of a {@link FinateStateMachine}
 * to {@link StateTransition transition} to another State.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (12:50)
 */
public interface StateTransitionEventType {

    public long getId();

    /**
     * Gets the symbolic representation of this event
     * that is guaranteed not to change over time.
     *
     * @return The symbolic representation
     */
    public String getSymbol();

    /**
     * Creates a new instance from this StateTransitionEventType.
     *
     * @param sourceId The String that uniquely identifies the source of the new event
     * @param properties The named properties
     * @return The new event
     */
    public StateTransitionEvent newInstance(String sourceId, Map<String, Object> properties);

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

    public void save();

    public void delete();

}