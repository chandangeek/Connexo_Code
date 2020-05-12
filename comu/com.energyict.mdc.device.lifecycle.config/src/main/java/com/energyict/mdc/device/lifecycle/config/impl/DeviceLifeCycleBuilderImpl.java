/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycleBuilder;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;
import com.energyict.mdc.common.device.lifecycle.config.TransitionBusinessProcess;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link DeviceLifeCycleBuilder} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (16:38)
 */
public class DeviceLifeCycleBuilderImpl implements DeviceLifeCycleBuilder {

    private final DataModel dataModel;
    private final DeviceLifeCycleImpl underConstruction;

    public DeviceLifeCycleBuilderImpl(DataModel dataModel, DeviceLifeCycleImpl underConstruction) {
        super();
        this.dataModel = dataModel;
        this.underConstruction = underConstruction;
    }

    protected DataModel getDataModel() {
        return dataModel;
    }

    protected DeviceLifeCycleImpl getUnderConstruction() {
        return underConstruction;
    }

    @Override
    public AuthorizedActionBuilder<AuthorizedBusinessProcessAction> newCustomAction(State state, String name, TransitionBusinessProcess process) {
        AuthorizedBusinessProcessActionImpl businessProcessAction = this.dataModel
                .getInstance(AuthorizedBusinessProcessActionImpl.class)
                .initialize(this.underConstruction, state, name, process);
        return new AuthorizedActionBuilderImpl<>(businessProcessAction);
    }

    @Override
    public AuthorizedTransitionActionBuilder newTransitionAction(StateTransition stateTransition) {
        AuthorizedTransitionActionImpl transitionAction;
        transitionAction = this.dataModel.getInstance(AuthorizedTransitionActionImpl.class).initialize(this.underConstruction, stateTransition);
        return new AuthorizedTransitionActionBuilderImpl(transitionAction);
    }

    @Override
    public DeviceLifeCycle complete() {
        return this.underConstruction;
    }

    private class AuthorizedActionBuilderImpl<AT extends AuthorizedAction, T extends AuthorizedActionImpl> implements AuthorizedActionBuilder<AT> {
        private final T underConstruction;

        protected AuthorizedActionBuilderImpl(T underConstruction) {
            super();
            this.underConstruction = underConstruction;
        }

        protected T getUnderConstruction() {
            return underConstruction;
        }

        @Override
        public AuthorizedActionBuilder<AT> addLevel(AuthorizedAction.Level level, AuthorizedAction.Level... otherLevels) {
            this.underConstruction.add(level);
            for (AuthorizedAction.Level otherLevel : otherLevels) {
                this.underConstruction.add(otherLevel);
            }
            return this;
        }

        @Override
        public AuthorizedActionBuilder<AT> addAllLevels(Set<AuthorizedAction.Level> levels) {
            for (AuthorizedAction.Level level : levels) {
                this.addLevel(level);
            }
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public AT complete() {
            DeviceLifeCycleBuilderImpl.this.underConstruction.add(this.underConstruction);
            return (AT) this.underConstruction;
        }

    }

    protected class AuthorizedTransitionActionBuilderImpl
            extends AuthorizedActionBuilderImpl<AuthorizedTransitionAction, AuthorizedTransitionActionImpl>
            implements AuthorizedTransitionActionBuilder {

        protected AuthorizedTransitionActionBuilderImpl(AuthorizedTransitionActionImpl underConstruction) {
            super(underConstruction);
        }

        @Override
        public AuthorizedTransitionActionBuilder setChecks(Set<String> checks) {
            this.getUnderConstruction().setChecks(checks);
            return this;
        }

        @Override
        public AuthorizedTransitionActionBuilder setChecks(String... checks) {
            this.getUnderConstruction().setChecks(Arrays.stream(checks).collect(Collectors.toSet()));
            return this;
        }

        @Override
        public AuthorizedTransitionActionBuilder addActions(MicroAction... actions) {
            for (MicroAction action : actions) {
                this.getUnderConstruction().add(action);
            }
            return this;
        }

        @Override
        public AuthorizedTransitionActionBuilder addActions(Set<MicroAction> actions) {
            for (MicroAction action : actions) {
                this.getUnderConstruction().add(action);
            }
            return this;
        }
    }
}
