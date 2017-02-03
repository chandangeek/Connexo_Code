/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;

import javax.inject.Inject;

public class UsagePointStateCreatorImpl implements UsagePointState.UsagePointStateCreator<UsagePointStateCreatorImpl> {
    private final DataModel dataModel;

    private UsagePointStage.Key stage;
    private UsagePointLifeCycleImpl lifeCycle;
    private FiniteStateMachineUpdater stateMachineUpdater;
    private FiniteStateMachineBuilder.StateBuilder stateBuilder;
    private boolean isInitial = false;

    @Inject
    public UsagePointStateCreatorImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    UsagePointStateCreatorImpl init(UsagePointLifeCycleImpl lifeCycle, String name) {
        this.lifeCycle = lifeCycle;
        this.stateMachineUpdater = lifeCycle.getStateMachine().startUpdate();
        this.stateBuilder = this.stateMachineUpdater.newCustomState(name);
        return this;
    }

    @Override
    public UsagePointStateCreatorImpl onEntry(StateChangeBusinessProcess process) {
        this.stateBuilder.onEntry(process);
        return this;
    }

    @Override
    public UsagePointStateCreatorImpl onExit(StateChangeBusinessProcess process) {
        this.stateBuilder.onExit(process);
        return this;
    }

    @Override
    public UsagePointStateCreatorImpl setInitial() {
        this.isInitial = true;
        return this;
    }

    @Override
    public UsagePointStateCreatorImpl setStage(UsagePointStage.Key stage) {
        this.stage = stage;
        return this;
    }

    @Override
    public UsagePointState complete() {
        State state = this.stateBuilder.complete();
        FiniteStateMachine stateMachine = this.stateMachineUpdater.complete();
        if (this.isInitial) {
            stateMachine.startUpdate().complete(state); // Bug in fsm updater, can't set new state as initial
        }
        return this.dataModel.getInstance(UsagePointStateImpl.class).init(this.lifeCycle, state, this.stage);
    }
}
