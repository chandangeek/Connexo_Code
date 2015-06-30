package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleBuilder;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess;
import com.energyict.mdc.device.lifecycle.config.TransitionType;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.TimeDuration;

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

    protected DataModel getDataModel() {
        return dataModel;
    }

    protected DeviceLifeCycleImpl getUnderConstruction() {
        return underConstruction;
    }

    @Override
    public DeviceLifeCycleBuilder maximumFutureEffectiveTimeShift(TimeDuration timeDuration) {
        this.underConstruction.setMaximumFutureEffectiveTimeShift(timeDuration);
        return this;
    }

    @Override
    public DeviceLifeCycleBuilder maximumPastEffectiveTimeShift(TimeDuration timeDuration) {
        this.underConstruction.setMaximumPastEffectiveTimeShift(timeDuration);
        return this;
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
        if (this.isStandard(stateTransition)) {
            transitionAction = this.dataModel.getInstance(AuthorizedStandardTransitionActionImpl.class).initialize(this.underConstruction, stateTransition);
        }
        else {
            transitionAction = this.dataModel.getInstance(AuthorizedCustomTransitionActionImpl.class).initialize(this.underConstruction, stateTransition);
        }
        return new AuthorizedTransitionActionBuilderImpl(transitionAction);
    }

    private boolean isStandard(StateTransition stateTransition) {
        return TransitionType.from(stateTransition).isPresent();
    }

    @Override
    public DeviceLifeCycle complete() {
        return this.underConstruction;
    }

    private class AuthorizedActionBuilderImpl<AT extends AuthorizedAction, T extends AuthorizedActionImpl> implements AuthorizedActionBuilder<AT> {
        protected final T underConstruction;

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
        public AuthorizedTransitionActionBuilder addCheck(MicroCheck check, MicroCheck... otherChecks) {
            this.underConstruction.add(check);
            for (MicroCheck otherCheck : otherChecks) {
                this.underConstruction.add(otherCheck);
            }
            return this;
        }

        @Override
        public AuthorizedTransitionActionBuilder addAllChecks(Set<MicroCheck> checks) {
            for (MicroCheck check : checks) {
                this.underConstruction.add(check);
            }
            return this;
        }

        @Override
        public AuthorizedTransitionActionBuilder addAction(MicroAction action, MicroAction... otherActions) {
            this.underConstruction.add(action);
            for (MicroAction otherAction : otherActions) {
                this.underConstruction.add(otherAction);
            }
            return this;
        }

        @Override
        public AuthorizedTransitionActionBuilder addAllActions(Set<MicroAction> actions) {
            for (MicroAction action : actions) {
                this.underConstruction.add(action);
            }
            return this;
        }

    }
}