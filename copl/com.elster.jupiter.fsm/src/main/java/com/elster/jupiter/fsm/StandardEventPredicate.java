package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ConsumerType;
import com.elster.jupiter.events.EventType;

/**
 * Models the behavior of a component that is capable of determining
 * if a "standard" {@link EventType} is candidate to be enabled
 * for use in a {@link FiniteStateMachine}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-06 (10:59)
 */
@ConsumerType
public interface StandardEventPredicate {

    /**
     * Determines if the specified {@link EventType}
     * is candidate to be used in a {@link FiniteStateMachine}.
     * Note that when a StandardEventPredicate returns <code>true</code>
     * then a {@link StandardStateTransitionEventType} will be created for it.
     *
     * @param eventType The EventType
     * @return A flag that indicates if the EventType is such a candidate
     */
    public boolean isCandidate(EventType eventType);

}