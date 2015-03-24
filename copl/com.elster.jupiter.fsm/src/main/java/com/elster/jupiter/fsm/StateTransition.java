package com.elster.jupiter.fsm;

import com.elster.jupiter.nls.Thesaurus;

import java.util.Optional;

/**
 * Models a transition from one {@link State} to another
 * and the {@link StateTransitionEventType} that will trigger this transition.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (11:55)
 */
public interface StateTransition {

    public long getId();

    public State getFrom();

    public State getTo();

    /**
     * Gets the name of this StateTransitionEventType.
     * If it does not have a name, you could rely on the
     * symbolic representation of the {@link StateTransitionEventType}.
     *
     * @return The name of this StateTransition
     * @see StateTransitionEventType#getSymbol()
     */
    public Optional<String> getName();

    /**
     * Gets the name of this StateTransition as it was specified
     * by the user or runs the symbolic representation of the
     * {@link StateTransitionEventType} through the Thesaurus.
     *
     * @param thesaurus The Thesaurus which is ignored if this StateTransition has a proper name
     * @return The name of this StateTransition
     */
    public String getName(Thesaurus thesaurus);

    public StateTransitionEventType getEventType();

}