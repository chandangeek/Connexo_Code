package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleBuilder;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.util.Optional;

public class UsagePointLifeCycleBuilderImpl implements UsagePointLifeCycleBuilder {
    private final FiniteStateMachineService stateMachineService;
    private final DataModel dataModel;

    private UsagePointLifeCycleImpl lifeCycle;

    @Inject
    public UsagePointLifeCycleBuilderImpl(FiniteStateMachineService stateMachineService, DataModel dataModel) {
        this.stateMachineService = stateMachineService;
        this.dataModel = dataModel;
        this.lifeCycle = this.dataModel.getInstance(UsagePointLifeCycleImpl.class);
    }

    UsagePointLifeCycleBuilder init(String name) {
        this.lifeCycle.setName(name);
        return this;
    }

    @Override
    public UsagePointLifeCycle complete() {
        Optional<FiniteStateMachine> stateMachineRef = this.lifeCycle.getStateMachine();
        if (!stateMachineRef.isPresent()) {
            FiniteStateMachineBuilder stateMachineBuilder = this.stateMachineService.newFiniteStateMachine("upl_" + this.lifeCycle.getName());
            State initialState = stateMachineBuilder.newStandardState("#initial").complete();
            FiniteStateMachine stateMachine = stateMachineBuilder.complete(initialState);
            this.lifeCycle.setStateMachine(stateMachine);
        }
        this.lifeCycle.save();
        return this.lifeCycle;
    }
}
