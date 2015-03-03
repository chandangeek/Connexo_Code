package com.elster.jupiter.fsm;

/**
 * Assists in building {@link FinateStateMachine}s.
 * Most of the methods return the same builder to support method chaining.
 * The building process is completed by returning the FinateStateMachine
 * but it is your responsibility to save the FinateStateMachine.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (09:54)
 */
public interface FinateStateMachineBuilder {

    /**
     * Starts the building process of a new {@link State}.
     *
     * @param name The name of the new State
     * @return The StateBuilder
     */
    public StateBuilder newState(String name);

    public TransitionSourceBuilder on(StateTransitionEventType eventType);

    /**
     * Completes the building process, returning the {@link FinateStateMachine}
     * that was built from the instructions.
     *
     * @return The FinateStateMachine
     */
    public FinateStateMachine complete();

    /**
     * Assists in building {@link State}s that will be added
     * to the main builder when completed.
     * Note that you need to complete every StateBuilder
     * before starting a new one.
     */
    public interface StateBuilder {

        /**
         * Adds the external process definition to the list of
         * processes that need to execute when the {@link State}
         * that is currently being built is entered.
         *
         * @param deploymentId The deployment id of the external process
         * @param processId The process id of the external process
         * @return The StateBuilder
         */
        public StateBuilder onEntry(String deploymentId, String processId);

        /**
         * Adds the external process definition to the list of
         * processes that need to execute when the {@link State}
         * that is currently being built is exited.
         *
         * @param deploymentId The deployment id of the external process
         * @param processId The process id of the external process
         * @return The StateBuilder
         */
        public StateBuilder onExit(String deploymentId, String processId);

        public State complete();

    }

    public interface TransitionSourceBuilder {
        public TransitionTargetBuilder transitionFrom(State state);
    }

    public interface TransitionTargetBuilder {
        public StateTransition to(State state);
    }

}