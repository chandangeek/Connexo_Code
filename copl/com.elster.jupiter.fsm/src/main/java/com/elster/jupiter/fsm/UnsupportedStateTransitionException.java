package com.elster.jupiter.fsm;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to remove a {@link StateTransition} from a {@link FiniteStateMachine}
 * that does not exist in that FiniteStateMachine.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-09 (10:39)
 */
public final class UnsupportedStateTransitionException extends LocalizedException {

    public UnsupportedStateTransitionException(Thesaurus thesaurus, FiniteStateMachine finiteStateMachine, State from, StateTransitionEventType eventType) {
        super(thesaurus, MessageSeeds.UNKNOWN_STATE_TRANSITION, from.getName(), eventType.getSymbol(), finiteStateMachine.getName());
        this.set("finiteStateMachineId", finiteStateMachine.getId());
        this.set("fromStateId", from.getId());
        this.set("eventTypeId", eventType.getId());
    }

}