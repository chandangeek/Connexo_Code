/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.time.TimeDuration;

import aQute.bnd.annotation.ProviderType;

import java.util.Set;

@ProviderType
public interface DeviceLifeCycleBuilder {

    /**
     * Sets the maximum time shift in the future
     * that can be used for the effective timestamp
     * of a device life cycle transition.
     *
     * @param timeDuration The TimeDuration
     * @return The DeviceLifeCycleBuilder to support method chaining
     * @see DeviceLifeCycle#getMaximumFutureEffectiveTimeShift()
     */
    DeviceLifeCycleBuilder maximumFutureEffectiveTimeShift(TimeDuration timeDuration);

    /**
     * Sets the maximum time shift in the past
     * that can be used for the effective timestamp
     * of a device life cycle transition.
     *
     * @param timeDuration The TimeDuration
     * @return The DeviceLifeCycleBuilder to support method chaining
     * @see DeviceLifeCycle#getMaximumPastEffectiveTimeShift()
     */
    DeviceLifeCycleBuilder maximumPastEffectiveTimeShift(TimeDuration timeDuration);

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
    @ProviderType
    interface AuthorizedActionBuilder<T extends AuthorizedAction> {

        AuthorizedActionBuilder<T> addLevel(AuthorizedAction.Level level, AuthorizedAction.Level... otherLevels);

        AuthorizedActionBuilder<T> addAllLevels(Set<AuthorizedAction.Level> levels);

        T complete();
    }

    @ProviderType
    interface AuthorizedTransitionActionBuilder extends AuthorizedActionBuilder<AuthorizedTransitionAction> {

        AuthorizedTransitionActionBuilder setChecks(Set<String> checks);

        AuthorizedTransitionActionBuilder setChecks(String... checks);

        AuthorizedTransitionActionBuilder addActions(Set<MicroAction> actions);

        AuthorizedTransitionActionBuilder addActions(MicroAction... actions);
    }
}
