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

    /**
     * Starts the update process for the {@link State} with the specified name.
     *
     * @param name The name of the State that needs updating
     * @return The StateUpdater
     */
    public StateUpdater state(String name);

    /**
     * Assists in updating {@link State}s that already exist
     * in the main builder's update target.
     */
    public interface StateUpdater {

        public StateUpdater setName(String newName);

        /**
         * Adds the external process definition to the list of
         * processes that need to execute when the {@link State}
         * that is currently being built is entered.
         *
         * @param deploymentId The deployment id of the external process
         * @param processId The process id of the external process
         * @return The StateBuilder
         */
        public StateUpdater onEntry(String deploymentId, String processId);

        /**
         * Adds the external process definition to the list of
         * processes that need to execute when the {@link State}
         * that is currently being built is exited.
         *
         * @param deploymentId The deployment id of the external process
         * @param processId The process id of the external process
         * @return The StateBuilder
         */
        public StateUpdater onExit(String deploymentId, String processId);

        /**
         * Assists in building a {@link StateTransition} from the {@link State}
         * that is being built here to another State when the specified
         * {@link StateTransitionEventType} occurs.
         *
         * @param eventType The StateTransitionEventType
         * @return The builder on which you will specify the target State
         */
        public TransitionBuilder on(StateTransitionEventType eventType);

        /**
         * Prohibits the {@link StateTransitionEventType} to occur
         * and as such is the inverse operation of {@link StateBuilder#on(StateTransitionEventType)}.
         * Note that this may throw an {@link UnsupportedStateTransitionException}
         * if the transition was not defined before.
         *
         * @param eventType The StateTransitionEventType
         * @return This StateUpdater to support method chaining
         */
        public StateUpdater prohibit(StateTransitionEventType eventType);

        public State complete();

    }

    public interface TransitionBuilder {
        public StateUpdater transitionTo(State state);
        public StateUpdater transitionTo(StateBuilder stateBuilder);
        public StateUpdater transitionTo(String stateName);
    }

}