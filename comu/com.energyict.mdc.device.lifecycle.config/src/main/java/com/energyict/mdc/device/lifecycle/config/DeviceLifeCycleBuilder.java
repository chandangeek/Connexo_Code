package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.time.TimeDuration;

import java.util.Set;

/**
 * Provides building services for {@link DeviceLifeCycle}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (16:31)
 */
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
    public DeviceLifeCycleBuilder maximumFutureEffectiveTimeShift(TimeDuration timeDuration);

    /**
     * Sets the maximum time shift in the past
     * that can be used for the effective timestamp
     * of a device life cycle transition.
     *
     * @param timeDuration The TimeDuration
     * @return The DeviceLifeCycleBuilder to support method chaining
     * @see DeviceLifeCycle#getMaximumPastEffectiveTimeShift()
     */
    public DeviceLifeCycleBuilder maximumPastEffectiveTimeShift(TimeDuration timeDuration);

    /**
     * Starts the building process to authorize the initiation of the {@link TransitionBusinessProcess}
     * when the related device is in the specified {@link State}.
     *
     * @param state The State
     * @param name The name for the new custom action
     * @param process The TransitionBusinessProcess
     * @return The AuthorizedActionBuilder
     */
    public AuthorizedActionBuilder<AuthorizedBusinessProcessAction> newCustomAction(State state, String name, TransitionBusinessProcess process);

    /**
     * Starts the building process to authorize the initiation of the specified {@link StateTransition}
     * when the related device is in the "from" {@link State} of the transition.
     *
     * @param stateTransition The StateTransition
     * @return The AuthorizedTransitionActionBuilder
     * @see StateTransition#getFrom()
     */
    public AuthorizedTransitionActionBuilder newTransitionAction(StateTransition stateTransition);

    /**
     * Gets the {@link DeviceLifeCycle} that was being constructed with this builder.
     * Note that it is your responsibility to save the DeviceLifeCycle.
     *
     * @return The DeviceLifeCycle
     * @see DeviceLifeCycle#save()
     */
    public DeviceLifeCycle complete();

    /**
     * Supports the building process of an {@link AuthorizedAction}.
     * It allows to specify the privilege levels that a user needs
     * to be able to initiate the action.
     * It is sufficient that a user has at least one of the
     * privilege levels to be able to initiate the new action.
     *
     * @param <T> The type of {@link AuthorizedAction} that is being built
     */
    public interface AuthorizedActionBuilder<T extends AuthorizedAction> {

        public AuthorizedActionBuilder<T> addLevel(AuthorizedAction.Level level, AuthorizedAction.Level... otherLevels);

        public AuthorizedActionBuilder<T> addAllLevels(Set<AuthorizedAction.Level> levels);

        public T complete();

    }

    public interface AuthorizedTransitionActionBuilder extends AuthorizedActionBuilder<AuthorizedTransitionAction> {

        public AuthorizedTransitionActionBuilder addCheck(MicroCheck check, MicroCheck... otherChecks);

        public AuthorizedTransitionActionBuilder addAllChecks(Set<MicroCheck> checks);

        public AuthorizedTransitionActionBuilder addAction(MicroAction action, MicroAction... otherActions);

        public AuthorizedTransitionActionBuilder addAllActions(Set<MicroAction> actions);

    }

}