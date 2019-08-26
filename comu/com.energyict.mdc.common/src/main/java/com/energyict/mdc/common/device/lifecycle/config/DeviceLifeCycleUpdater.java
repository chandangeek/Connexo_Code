/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.lifecycle.config;

import com.elster.jupiter.fsm.StateTransition;

import aQute.bnd.annotation.ConsumerType;

/**
 * Extends the building services of {@link DeviceLifeCycleBuilder}
 * to support updates to existing {@link DeviceLifeCycle}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-27 (13:25)
 */
@ConsumerType
public interface DeviceLifeCycleUpdater extends DeviceLifeCycleBuilder {

    /**
     * Sets a new name for the {@link DeviceLifeCycle}.
     *
     * @param name The new name that needs to be unique
     * @return The DeviceLifeCycleUpdater to support method chaining
     */
    public DeviceLifeCycleUpdater setName(String name);

    /**
     * Removes the specified {@link StateTransition} as an {@link AuthorizedTransitionAction}.
     * The transition will still be part of the DeviceLifeCycle's finite state machine
     * but will no longer be available as an action for the user.
     *
     * @param transition The StateTransition
     * @return The DeviceLifeCycleUpdater to support method chaining
     */
    public DeviceLifeCycleUpdater removeTransitionAction(StateTransition transition);

    public AuthorizedTransitionActionUpdater transitionAction(StateTransition transition);

    public interface AuthorizedTransitionActionUpdater extends AuthorizedTransitionActionBuilder {

        public AuthorizedTransitionActionUpdater clearLevels();

        public AuthorizedTransitionActionUpdater clearChecks();

        public AuthorizedTransitionActionUpdater clearActions();

    }

}