package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Map;

/**
 * Models a user-defined {@link StateTransitionEventType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-05 (10:07)
 */
@ProviderType
public interface CustomStateTransitionEventType extends StateTransitionEventType {

    /**
     * Creates a new instance from this CustomStateTransitionEventType.
     *
     * @param finiteStateMachine The FiniteStateMachine to which the trigger event applies
     * @param sourceId The String that uniquely identifies the source of the new event
     * @param sourceCurrentStateName The name of the current {@link State} for the source of the new event
     * @param effectiveTimestamp The point in time when the state change should become effective
     * @param properties The named properties  @return The new event
     */
    public StateTransitionTriggerEvent newInstance(FiniteStateMachine finiteStateMachine, String sourceId, String sourceCurrentStateName, Instant effectiveTimestamp, Map<String, Object> properties);

}