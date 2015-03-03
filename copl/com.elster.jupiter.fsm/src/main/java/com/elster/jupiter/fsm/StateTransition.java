package com.elster.jupiter.fsm;

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

    public StateTransitionEventType getEventType();

}