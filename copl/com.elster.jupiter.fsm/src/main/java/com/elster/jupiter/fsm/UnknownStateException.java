package com.elster.jupiter.fsm;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to remove a {@link State} from a {@link FiniteStateMachine}
 * that does not exist in that FiniteStateMachine.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-09 (10:39)
 */
public class UnknownStateException extends LocalizedException {

    public UnknownStateException(Thesaurus thesaurus, FiniteStateMachine finiteStateMachine, String stateName) {
        super(thesaurus, MessageSeeds.UNKNOWN_STATE, stateName, finiteStateMachine.getName());
        this.set("finiteStateMachineId", finiteStateMachine.getId());
        this.set("stateName", stateName);
    }

}