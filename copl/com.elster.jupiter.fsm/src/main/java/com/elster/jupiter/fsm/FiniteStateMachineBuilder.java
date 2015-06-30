package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Assists in building {@link FiniteStateMachine}s.
 * Most of the methods return the same builder to support method chaining.
 * The building process is completed by returning the FiniteStateMachine
 * but it is your responsibility to save the FiniteStateMachine.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (09:54)
 */
@ProviderType
public interface FiniteStateMachineBuilder {

    /**
     * Starts the building process of a new custom {@link State}.
     *
     * @param name The name of the new custom State
     * @return The StateBuilder
     * @see State#isCustom()
     */
    public StateBuilder newCustomState(String name);

    /**
     * Starts the building process of a new standard {@link State}.
     *
     * @param symbolicName The symbolic name of the new State
     * @return The StateBuilder
     * @see State#isCustom()
     */
    public StateBuilder newStandardState(String symbolicName);

    /**
     * Completes the building process, returning the {@link FiniteStateMachine}
     * that was built from the instructions.
     * Note that you are responsible for saving the FiniteStateMachine.
     *
     * @return The FiniteStateMachine
     */
    public FiniteStateMachine complete();

    /**
     * Completes the building process, marking the specified {@link State}
     * as the initial State and returning the {@link FiniteStateMachine}
     * that was built from the instructions.
     *
     * @return The FiniteStateMachine
     */
    public FiniteStateMachine complete(State initial);

    /**
     * Assists in building {@link State}s that will be added
     * to the main builder when completed.
     */
    public interface StateBuilder {

        /**
         * Adds the {@link StateChangeBusinessProcess} to the list of
         * processes that need to execute when the {@link State}
         * that is currently being built is entered.
         *
         * @param process The StateChangeBusinessProcess
         * @return The StateBuilder
         */
        public StateBuilder onEntry(StateChangeBusinessProcess process);

        /**
         * Adds the {@link StateChangeBusinessProcess} to the list of
         * processes that need to execute when the {@link State}
         * that is currently being built is exited.
         *
         * @param process The StateChangeBusinessProcess
         * @return The StateBuilder
         */
        public StateBuilder onExit(StateChangeBusinessProcess process);

        /**
         * Assists in building a {@link StateTransition} from the {@link State}
         * that is being built here to another State when the specified
         * {@link StateTransitionEventType} occurs.
         *
         * @param eventType The StateTransitionEventType
         * @return The builder on which you will specify the target State
         */
        public TransitionBuilder on(StateTransitionEventType eventType);

        public State complete();

    }

    public interface TransitionBuilder {
        public StateBuilder transitionTo(State state);
        public StateBuilder transitionTo(State state, String name);
        public StateBuilder transitionTo(State state, TranslationKey translationKey);
        public StateBuilder transitionTo(StateBuilder state);
        public StateBuilder transitionTo(StateBuilder stateBuilder, String name);
        public StateBuilder transitionTo(StateBuilder stateBuilder, TranslationKey translationKey);
    }

}