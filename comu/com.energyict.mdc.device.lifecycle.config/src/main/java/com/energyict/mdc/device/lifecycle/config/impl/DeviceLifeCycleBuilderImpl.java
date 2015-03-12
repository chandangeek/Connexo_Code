package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleBuilder;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.orm.DataModel;

import java.util.Set;

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

    @Override
    public AuthorizedActionBuilder newCustomAction(State state, String deploymentId, String processId) {
        return new AuthorizedActionBuilderImpl<>(this.dataModel.getInstance(AuthorizedBusinessProcessActionImpl.class).initialize(state, deploymentId, processId));
    }

    @Override
    public DeviceLifeCycle complete() {
        return this.underConstruction;
    }

    private class AuthorizedActionBuilderImpl<T extends AuthorizedActionImpl> implements AuthorizedActionBuilder<T> {
        private final T underConstruction;

        private AuthorizedActionBuilderImpl(T underConstruction) {
            super();
            this.underConstruction = underConstruction;
        }

        @Override
        public AuthorizedActionBuilder<T> add(AuthorizedAction.Level level, AuthorizedAction.Level... otherLevels) {
            this.underConstruction.add(level);
            for (AuthorizedAction.Level otherLevel : otherLevels) {
                this.underConstruction.add(otherLevel);
            }
            return this;
        }

        @Override
        public AuthorizedActionBuilder<T> addAll(Set<AuthorizedAction.Level> levels) {
            for (AuthorizedAction.Level level : levels) {
                this.add(level);
            }
            return this;
        }

        @Override
        public T complete() {
            DeviceLifeCycleBuilderImpl.this.underConstruction.add(this.underConstruction);
            return this.underConstruction;
        }

    }

}