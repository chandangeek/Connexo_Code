/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleUpdater;

/**
 * Provides an implementation for the {@link DeviceLifeCycleUpdater} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-27 (10:30)
 */
public class DeviceLifeCycleUpdaterImpl extends DeviceLifeCycleBuilderImpl implements DeviceLifeCycleUpdater {

    public DeviceLifeCycleUpdaterImpl(DataModel dataModel, DeviceLifeCycleImpl underConstruction) {
        super(dataModel, underConstruction);
    }

    @Override
    public DeviceLifeCycleUpdater setName(String name) {
        this.getUnderConstruction().setName(name);
        return this;
    }

    @Override
    public DeviceLifeCycleUpdater removeTransitionAction(StateTransition transition) {
        this.getUnderConstruction().removeTransitionAction(transition);
        return this;
    }

    @Override
    public AuthorizedTransitionActionUpdater transitionAction(StateTransition transition) {
        return new AuthorizedTransitionActionUpdaterImpl(this.getUnderConstruction().findActionFor(transition));
    }

    private class AuthorizedTransitionActionUpdaterImpl extends AuthorizedTransitionActionBuilderImpl implements AuthorizedTransitionActionUpdater {

        protected AuthorizedTransitionActionUpdaterImpl(AuthorizedTransitionActionImpl underConstruction) {
            super(underConstruction);
        }

        @Override
        public AuthorizedTransitionActionUpdater clearLevels() {
            this.getUnderConstruction().clearLevels();
            return this;
        }

        @Override
        public AuthorizedTransitionActionUpdater clearChecks() {
            this.getUnderConstruction().clearChecks();
            return this;
        }

        @Override
        public AuthorizedTransitionActionUpdater clearActions() {
            this.getUnderConstruction().clearActions();
            return this;
        }

        @Override
        public AuthorizedTransitionAction complete() {
            AuthorizedTransitionActionImpl action = this.getUnderConstruction();
            action.notifyUpdated();
            return action;
        }

    }

}