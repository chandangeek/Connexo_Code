package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.DefaultState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.function.Consumer;

public class UsagePointLifeCycleBuilderImpl {
    private static final String FSM_NAME_PREFIX = UsagePointLifeCycleService.COMPONENT_NAME + "_";

    private final DataModel dataModel;
    private final FiniteStateMachineService stateMachineService;
    private final Thesaurus thesaurus;

    @Inject
    public UsagePointLifeCycleBuilderImpl(DataModel dataModel, FiniteStateMachineService stateMachineService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.stateMachineService = stateMachineService;
        this.thesaurus = thesaurus;
    }

    private UsagePointLifeCycle getLifeCycle(String name, Consumer<UsagePointLifeCycle> transitionBuilder, Provider<FiniteStateMachine> stateMachineBuilder) {
        UsagePointLifeCycleImpl lifeCycle = this.dataModel.getInstance(UsagePointLifeCycleImpl.class);
        lifeCycle.setName(name);
        lifeCycle.setStateMachine(stateMachineBuilder.get());
        lifeCycle.save();
        transitionBuilder.accept(lifeCycle);
        return lifeCycle;
    }

    public UsagePointLifeCycle getDefaultLifeCycleWithName(String name) {
        return getLifeCycle(name, this::addDefaultTransitions, () -> getDefaultFiniteStateMachine(name));
    }

    private FiniteStateMachine getDefaultFiniteStateMachine(String name) {
        FiniteStateMachineBuilder stateMachineBuilder = this.stateMachineService.newFiniteStateMachine(FSM_NAME_PREFIX + name);
        stateMachineBuilder.newStandardState(DefaultState.CONNECTED.getKey()).complete();
        stateMachineBuilder.newStandardState(DefaultState.PHYSICALLY_DISCONNECTED.getKey()).complete();
        stateMachineBuilder.newStandardState(DefaultState.DEMOLISHED.getKey()).complete();
        return stateMachineBuilder.complete(stateMachineBuilder.newStandardState(DefaultState.UNDER_CONSTRUCTION.getKey()).complete());
    }

    private UsagePointState getState(UsagePointLifeCycle lifeCycle, DefaultState state) {
        return lifeCycle.getStates()
                .stream()
                .filter(candidate -> candidate.isDefault(state))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Can't find state with key:  " + state.getKey()));
    }

    private void addDefaultTransitions(UsagePointLifeCycle lifeCycle) {
        lifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_INSTALL).format(),
                getState(lifeCycle, DefaultState.UNDER_CONSTRUCTION), getState(lifeCycle, DefaultState.CONNECTED)).complete();
        lifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_SEAL).format(),
                getState(lifeCycle, DefaultState.CONNECTED), getState(lifeCycle, DefaultState.PHYSICALLY_DISCONNECTED)).complete();
        lifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_OPEN).format(),
                getState(lifeCycle, DefaultState.PHYSICALLY_DISCONNECTED), getState(lifeCycle, DefaultState.CONNECTED)).complete();
        lifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_DEMOLISH_FROM_CONNECTED).format(),
                getState(lifeCycle, DefaultState.CONNECTED), getState(lifeCycle, DefaultState.DEMOLISHED)).complete();
        lifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_DEMOLISH_FROM_PHYSICALLY_DISCONNECTED).format(),
                getState(lifeCycle, DefaultState.PHYSICALLY_DISCONNECTED), getState(lifeCycle, DefaultState.DEMOLISHED)).complete();
    }
}
