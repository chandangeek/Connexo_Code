package com.elster.jupiter.fsm;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to remove a {@link StateTransitionEventType} while
 * it is still in use by at least one {@link FinateStateMachine}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-10 (15:54)
 */
public class StateTransitionEventTypeStillInUseException extends LocalizedException {

    public StateTransitionEventTypeStillInUseException(Thesaurus thesaurus, StateTransitionEventType eventType, Collection<FinateStateMachine> finateStateMachines) {
        super(thesaurus, MessageSeeds.EVENT_TYPE_STILL_IN_USE, eventType.getSymbol(), toCommaSeparatedList(finateStateMachines));
        this.set("eventTypeId", eventType.getId());
    }

    private static String toCommaSeparatedList(Collection<FinateStateMachine> finateStateMachines) {
        return finateStateMachines.stream().map(FinateStateMachine::getName).collect(Collectors.joining(", "));
    }

}