package com.elster.jupiter.fsm;

/**
 * Models the behavior of a component that allows to update an existing {@link FinateStateMachine}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-09 (09:45)
 */
public interface FinateStateMachineUpdater extends FinateStateMachineBuilder {

    /**
     * Sets a new name for the {@link FinateStateMachine}
     * that is the target of this FinateStateMachineUpdater.
     * Note that the name uniqueness constraint is obviously
     * respected and than any attempt to set the name
     * of the existing FinateStateMachine to one that already
     * exists will result in a validation error.
     *
     * @param newName The new name
     * @return This FinateStateMachineUpdater to support method chaining
     */
    public FinateStateMachineUpdater setName(String newName);

    public FinateStateMachineUpdater setTopic(String newTopic);

    /**
     * Removes the {@link State} with the specified name as well
     * as all incoming and outgoing transitions for that State.
     * Note that this may throw an {@link UnknownStateException}
     * if no such State exists.
     *
     * @param obsoleteStateName The name of the obsolete State
     * @return This FinateStateMachineUpdater to support method chaining
     */
    public FinateStateMachineUpdater removeState(String obsoleteStateName);

    /**
     * Removes the specified {@link State} as well
     * as all incoming and outgoing transitions for that State.
     * Note that this may throw an {@link UnknownStateException}
     * if the State is not actually part of the FinateStateMachine.
     *
     * @param obsoleteState The obsolete State
     * @return This FinateStateMachineUpdater to support method chaining
     */
    public FinateStateMachineUpdater removeState(State obsoleteState);

}