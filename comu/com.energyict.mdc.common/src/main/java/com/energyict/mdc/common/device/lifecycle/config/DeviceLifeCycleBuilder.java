/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.common.device.lifecycle.config;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;

import aQute.bnd.annotation.ConsumerType;

import java.util.Set;

@ConsumerType
public interface DeviceLifeCycleBuilder {

    /**
     * Starts the building process to authorize the initiation of the {@link TransitionBusinessProcess}
     * when the related device is in the specified {@link State}.
     *
     * @param state   The State
     * @param name    The name for the new custom action
     * @param process The TransitionBusinessProcess
     * @return The AuthorizedActionBuilder
     */
    AuthorizedActionBuilder<AuthorizedBusinessProcessAction> newCustomAction(State state, String name, TransitionBusinessProcess process);

    /**
     * Starts the building process to authorize the initiation of the specified {@link StateTransition}
     * when the related device is in the "from" {@link State} of the transition.
     *
     * @param stateTransition The StateTransition
     * @return The AuthorizedTransitionActionBuilder
     * @see StateTransition#getFrom()
     */
    AuthorizedTransitionActionBuilder newTransitionAction(StateTransition stateTransition);

    /**
     * Gets the {@link DeviceLifeCycle} that was being constructed with this builder.
     * Note that it is your responsibility to save the DeviceLifeCycle.
     *
     * @return The DeviceLifeCycle
     * @see DeviceLifeCycle#save()
     */
    DeviceLifeCycle complete();

    /**
     * Supports the building process of an {@link AuthorizedAction}.
     * It allows to specify the privilege levels that a user needs
     * to be able to initiate the action.
     * It is sufficient that a user has at least one of the
     * privilege levels to be able to initiate the new action.
     *
     * @param <T> The type of {@link AuthorizedAction} that is being built
     */
    @ConsumerType
    interface AuthorizedActionBuilder<T extends AuthorizedAction> {

        AuthorizedActionBuilder<T> addLevel(AuthorizedAction.Level level, AuthorizedAction.Level... otherLevels);

        AuthorizedActionBuilder<T> addAllLevels(Set<AuthorizedAction.Level> levels);

        T complete();
    }

    @ConsumerType
    interface AuthorizedTransitionActionBuilder extends AuthorizedActionBuilder<AuthorizedTransitionAction> {

        AuthorizedTransitionActionBuilder setChecks(Set<String> checks);

        AuthorizedTransitionActionBuilder setChecks(String... checks);

        AuthorizedTransitionActionBuilder addActions(Set<MicroAction> actions);

        AuthorizedTransitionActionBuilder addActions(MicroAction... actions);
    }
}
