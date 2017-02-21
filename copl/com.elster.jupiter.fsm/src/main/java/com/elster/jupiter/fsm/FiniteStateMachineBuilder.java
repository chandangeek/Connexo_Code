/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm;

import com.elster.jupiter.nls.TranslationKey;

import aQute.bnd.annotation.ProviderType;

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
     * @throws IllegalStateException When the {@link FiniteStateMachineBuilder} has a {@link StageSet}
     */
    StateBuilder newCustomState(String name);

    /**
     * Starts the building process of a new standard {@link State}.
     *
     * @param symbolicName The symbolic name of the new State
     * @return The StateBuilder
     * @see State#isCustom()
     * @throws IllegalStateException When the {@link FiniteStateMachineBuilder} has a {@link StageSet}
     */
    StateBuilder newStandardState(String symbolicName);

    /**
     * Starts the building process of a new custom {@link State}.
     *
     * @param name The name of the new custom State
     * @param stage The {@link Stage} of the new custom Stage
     * @return The StateBuilder
     * @see State#isCustom()
     * @throws IllegalStateException When the {@link FiniteStateMachineBuilder} has no {@link StageSet} or the given Stage is not in the StageSet
     */
    StateBuilder newCustomState(String name, Stage stage);

    /**
     * Starts the building process of a new standard {@link State}.
     *
     * @param symbolicName The symbolic name of the new State
     * @param stage The {@link Stage} of the new State
     * @return The StateBuilder
     * @see State#isCustom()
     * @throws IllegalStateException When the {@link FiniteStateMachineBuilder} has no {@link StageSet} or the given Stage is not in the StageSet
     */
    StateBuilder newStandardState(String symbolicName, Stage stage);

    /**
     * Completes the building process, marking the specified {@link State}
     * as the initial State and returning the {@link FiniteStateMachine}
     * that was built from the instructions.
     *
     * @return The FiniteStateMachine
     */
    FiniteStateMachine complete(State initial);

    /**
     * Assists in building {@link State}s that will be added
     * to the main builder when completed.
     */
    interface StateBuilder {

        /**
         * Adds the {@link StateChangeBusinessProcess} to the list of
         * processes that need to execute when the {@link State}
         * that is currently being built is entered.
         *
         * @param process The StateChangeBusinessProcess
         * @return The StateBuilder
         */
        StateBuilder onEntry(StateChangeBusinessProcess process);

        /**
         * Adds the {@link StateChangeBusinessProcess} to the list of
         * processes that need to execute when the {@link State}
         * that is currently being built is exited.
         *
         * @param process The StateChangeBusinessProcess
         * @return The StateBuilder
         */
        StateBuilder onExit(StateChangeBusinessProcess process);

        /**
         * Assists in building a {@link StateTransition} from the {@link State}
         * that is being built here to another State when the specified
         * {@link StateTransitionEventType} occurs.
         *
         * @param eventType The StateTransitionEventType
         * @return The builder on which you will specify the target State
         */
        TransitionBuilder on(StateTransitionEventType eventType);

        State complete();

    }

    interface TransitionBuilder {
        StateBuilder transitionTo(State state);
        StateBuilder transitionTo(State state, String name);
        StateBuilder transitionTo(State state, TranslationKey translationKey);
        StateBuilder transitionTo(StateBuilder state);
        StateBuilder transitionTo(StateBuilder stateBuilder, String name);
        StateBuilder transitionTo(StateBuilder stateBuilder, TranslationKey translationKey);
    }

}