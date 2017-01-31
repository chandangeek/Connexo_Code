/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Map;

/**
 * A StateTransitionTriggerEvent triggers the {@link StateTransition transition}
 * of the current {@link State} to another State.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (14:49)
 */
@ProviderType
public interface StateTransitionTriggerEvent {

    /**
     * Gets the {@link FiniteStateMachine} to which this trigger applies.
     *
     * @return The FiniteStateMachine
     */
    FiniteStateMachine getFiniteStateMachine();

    StateTransitionEventType getType();

    /**
     * Gets the String that uniquely identifies the source of this event.
     *
     * @return The unique identifier of the source of this event
     */
    String getSourceId();

    /**
     * Gets the String that uniquely identifies the type of the source of this event.
     *
     * @return The unique identifier of the type of the source of this event
     */
    String getSourceType();

    /**
     * Gets the name of the current state of the source of this event.
     *
     * @return The current state name
     * @see State#getName()
     */
    String getSourceCurrentStateName();

    /**
     * The point in time when the resulting state change should be effective.
     *
     * @return The point in time when the resulting state change should be effective
     */
    Instant getEffectiveTimestamp();

    Map<String, Object> getProperties();

    void publish();

}