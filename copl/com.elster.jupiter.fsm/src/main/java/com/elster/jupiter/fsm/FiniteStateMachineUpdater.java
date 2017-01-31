/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the behavior of a component that allows to update an existing {@link FiniteStateMachine}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-09 (09:45)
 */
public interface FiniteStateMachineUpdater extends FiniteStateMachineBuilder {

    /**
     * Sets a new name for the {@link FiniteStateMachine}
     * that is the target of this FiniteStateMachineUpdater.
     * Note that the name uniqueness constraint is obviously
     * respected and than any attempt to set the name
     * of the existing FiniteStateMachine to one that already
     * exists will result in a validation error.
     *
     * @param newName The new name
     * @return This FiniteStateMachineUpdater to support method chaining
     */
    FiniteStateMachineUpdater setName(String newName);

    /**
     * Removes the {@link State} with the specified name as well
     * as all incoming and outgoing transitions for that State.
     * Note that this may throw an {@link UnknownStateException}
     * if no such State exists.
     *
     * @param obsoleteStateName The name of the obsolete State
     * @return This FiniteStateMachineUpdater to support method chaining
     */
    FiniteStateMachineUpdater removeState(String obsoleteStateName);

    /**
     * Removes the specified {@link State} as well
     * as all incoming and outgoing transitions for that State.
     * Note that this may throw an {@link UnknownStateException}
     * if the State is not actually part of the FiniteStateMachine.
     *
     * @param obsoleteState The obsolete State
     * @return This FiniteStateMachineUpdater to support method chaining
     */
    FiniteStateMachineUpdater removeState(State obsoleteState);

    /**
     * Starts the update process for the {@link State} with the specified name.
     *
     * @param name The name of the State that needs updating
     * @return The StateUpdater
     */
    StateUpdater state(String name);

    /**
     * Starts the update process for the {@link State} with the unique identifier.
     *
     * @param id The unique identifier of the State that needs updating
     * @return The StateUpdater
     */
    StateUpdater state(long id);

    /**
     * Completes the building process, returning the {@link FiniteStateMachine}
     * that was built from the instructions.
     * Note that you are responsible for saving the FiniteStateMachine.
     *
     * @return The FiniteStateMachine
     */
    FiniteStateMachine complete();

    /**
     * Assists in updating {@link State}s that already exist
     * in the main builder's update target.
     */
    @ProviderType
    interface StateUpdater {

        StateUpdater setName(String newName);

        /**
         * Adds the {@link StateChangeBusinessProcess} to the list of
         * processes that need to execute when the {@link State}
         * that is currently being built is entered.
         *
         * @param process The StateChangeBusinessProcess
         * @return The StateBuilder
         */
        StateUpdater onEntry(StateChangeBusinessProcess process);

        /**
         * Removes the {@link StateChangeBusinessProcess} from the list of
         * processes that need to execute when the {@link State}
         * that is currently being built is entered.
         *
         * @param process The StateChangeBusinessProcess
         * @return The StateBuilder
         */
        StateUpdater removeOnEntry(StateChangeBusinessProcess process);

        /**
         * Adds the {@link StateChangeBusinessProcess} to the list of
         * processes that need to execute when the {@link State}
         * that is currently being built is exited.
         *
         * @param process The StateChangeBusinessProcess
         * @return The StateBuilder
         */
        StateUpdater onExit(StateChangeBusinessProcess process);

        /**
         * Removes the {@link StateChangeBusinessProcess} from the list of
         * processes that need to execute when the {@link State}
         * that is currently being built is exited.
         *
         * @param process The StateChangeBusinessProcess
         * @return The StateBuilder
         */
        StateUpdater removeOnExit(StateChangeBusinessProcess process);

        /**
         * Assists in building a {@link StateTransition} from the {@link State}
         * that is being built here to another State when the specified
         * {@link StateTransitionEventType} occurs.
         *
         * @param eventType The StateTransitionEventType
         * @return The builder on which you will specify the target State
         */
        TransitionBuilder on(StateTransitionEventType eventType);

        /**
         * Prohibits the {@link StateTransitionEventType} to occur
         * and as such is the inverse operation of {@link StateBuilder#on(StateTransitionEventType)}.
         * Note that this may throw an {@link UnsupportedStateTransitionException}
         * if the transition was not defined before.
         *
         * @param eventType The StateTransitionEventType
         * @return This StateUpdater to support method chaining
         */
        StateUpdater prohibit(StateTransitionEventType eventType);

        State complete();

    }

    interface TransitionBuilder {
        TransitionBuilder setName(String transitionName);
        StateUpdater transitionTo(State state);
        StateUpdater transitionTo(StateBuilder stateBuilder);
        StateUpdater transitionTo(String stateName);
        StateUpdater transitionTo(long stateId);
    }

}