package com.elster.jupiter.fsm;

import com.elster.jupiter.nls.Thesaurus;

import java.util.Optional;

/**
 * Models a transition from one {@link State} to another
 * and the {@link StateTransitionEventType} that will trigger this transition.
 * <p>
 * By default, the name of a StateTransition is the symbolic representation
 * of the StateTransitionEventType. This can be overruled with a specific String
 * or with a translation key.
 * </p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (11:55)
 */
public interface StateTransition {

    public long getId();

    public State getFrom();

    public State getTo();

    /**
     * Gets the name of this StateTransition.
     * If it does not have a name, you could rely on the
     * symbolic representation of the {@link StateTransitionEventType}.
     *
     * @return The name of this StateTransition
     * @see StateTransitionEventType#getSymbol()
     */
    public Optional<String> getName();

    /**
     * Gets the translation key that will serve to produce
     * a name for this StateTransition.
     *
     * @return The translation key of this StateTransition
     */
    public Optional<String> getTranslationKey();

    /**
     * Gets the name of this StateTransition.
     * If neither a name or a translation key was specified
     * then the symbolic representation of the related
     * {@link StateTransitionEventType} is passed on to the Thesaurus
     * to produce a name.
     * If a name was specified then that name is returned.
     * If a translation key was specified then that translation key
     * is passed on to the Thesaurus to produce a name
     *
     * @param thesaurus The Thesaurus which is ignored if this StateTransition has a proper name
     * @return The name of this StateTransition
     */
    public String getName(Thesaurus thesaurus);

    public StateTransitionEventType getEventType();

}