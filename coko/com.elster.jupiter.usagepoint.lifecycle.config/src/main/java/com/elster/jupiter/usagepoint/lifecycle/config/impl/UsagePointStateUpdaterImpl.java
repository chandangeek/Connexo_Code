package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;

public class UsagePointStateUpdaterImpl implements UsagePointState.UsagePointStateUpdater {
    private final UsagePointStateImpl state;
    private final FiniteStateMachineUpdater stateMachineUpdater;
    private final FiniteStateMachineUpdater.StateUpdater stateUpdater;
    private boolean isInitial;
    private UsagePointStage.Stage stage;

    public UsagePointStateUpdaterImpl(UsagePointStateImpl state) {
        this.state = state;
        this.stateMachineUpdater = this.state.getState().getFiniteStateMachine().startUpdate();
        this.stateUpdater = this.stateMachineUpdater.state(state.getId());
    }

    @Override
    public UsagePointState.UsagePointStateUpdater setName(String newName) {
        this.stateUpdater.setName(newName);
        return this;
    }

    @Override
    public UsagePointState.UsagePointStateUpdater removeOnEntry(StateChangeBusinessProcess process) {
        this.stateUpdater.removeOnEntry(process);
        return this;
    }

    @Override
    public UsagePointState.UsagePointStateUpdater removeOnExit(StateChangeBusinessProcess process) {
        this.stateUpdater.removeOnExit(process);
        return this;
    }

    @Override
    public UsagePointState.UsagePointStateUpdater onEntry(StateChangeBusinessProcess process) {
        this.stateUpdater.onEntry(process);
        return this;
    }

    @Override
    public UsagePointState.UsagePointStateUpdater onExit(StateChangeBusinessProcess process) {
        this.stateUpdater.onExit(process);
        return this;
    }

    @Override
    public UsagePointState.UsagePointStateUpdater setInitial() {
        this.isInitial = true;
        return this;
    }

    @Override
    public UsagePointState.UsagePointStateUpdater setStage(UsagePointStage.Stage stage) {
        this.stage = stage;
        return this;
    }

    @Override
    public UsagePointState complete() {
        State updatedState = this.stateUpdater.complete();
        if (this.isInitial) {
            this.stateMachineUpdater.complete(updatedState);
        } else {
            this.stateMachineUpdater.complete();
        }
        if (this.state != null) {
            this.state.setStage(this.stage);
        }
        return this.state;
    }
}
