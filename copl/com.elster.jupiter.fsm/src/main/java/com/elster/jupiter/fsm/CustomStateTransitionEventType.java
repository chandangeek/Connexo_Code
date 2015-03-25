package com.elster.jupiter.fsm;

import java.util.Map;

/**
 * Models a user-defined {@link StateTransitionEventType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-05 (10:07)
 */
public interface CustomStateTransitionEventType extends StateTransitionEventType {

    /**
     * Creates a new instance from this CustomStateTransitionEventType.
     *
     * @param finateStateMachine The FinateStateMachine to which the trigger event applies
     * @param sourceId The String that uniquely identifies the source of the new event
     * @param sourceCurrentStateName The name of the current {@link State} for the source of the new event
     *@param properties The named properties  @return The new event
     */
    public StateTransitionTriggerEvent newInstance(FinateStateMachine finateStateMachine, String sourceId, String sourceCurrentStateName, Map<String, Object> properties);

}