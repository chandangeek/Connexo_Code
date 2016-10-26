package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.orm.DataModel;

public class UsagePointStateCreatorImpl implements UsagePointState.UsagePointStateCreator<UsagePointStateCreatorImpl> {
    private final DataModel dataModel;
    private final FiniteStateMachineUpdater stateMachineUpdater;
    private final FiniteStateMachineBuilder.StateBuilder stateBuilder;
    private boolean isInitial = false;

    public UsagePointStateCreatorImpl(DataModel dataModel, UsagePointLifeCycleImpl lifeCycle, String name) {
        this.dataModel = dataModel;
        this.stateMachineUpdater = lifeCycle.getStateMachine().get().startUpdate();
        this.stateBuilder = this.stateMachineUpdater.newCustomState(name);
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
    public UsagePointState complete() {
        State state = this.stateBuilder.complete();
        FiniteStateMachine stateMachine = this.stateMachineUpdater.complete();
        if (this.isInitial) {
            stateMachine.startUpdate().complete(state); // Bug in fsm updater, can't set new state as initial
        }
        return this.dataModel.getInstance(UsagePointStateImpl.class).init(state);
    }
}
