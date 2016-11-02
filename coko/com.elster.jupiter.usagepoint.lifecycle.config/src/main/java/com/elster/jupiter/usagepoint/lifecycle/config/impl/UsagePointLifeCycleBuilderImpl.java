package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UsagePointLifeCycleBuilderImpl {
    private static final String FSM_NAME_PREFIX = UsagePointLifeCycleConfigurationService.COMPONENT_NAME + "_";

    private final DataModel dataModel;
    private final FiniteStateMachineService stateMachineService;
    private final Thesaurus thesaurus;

    @Inject
    public UsagePointLifeCycleBuilderImpl(DataModel dataModel, FiniteStateMachineService stateMachineService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.stateMachineService = stateMachineService;
        this.thesaurus = thesaurus;
    }

    public UsagePointLifeCycle getDefaultLifeCycleWithName(String name) {
        return getLifeCycle(name, () -> getDefaultFiniteStateMachine(name), this::addDefaultTransitions);
    }

    public UsagePointLifeCycle cloneUsagePointLifeCycle(String name, UsagePointLifeCycle source) {
        UsagePointLifeCycleImpl sourceImpl = (UsagePointLifeCycleImpl) source;
        return getLifeCycle(name, () -> this.stateMachineService.cloneFiniteStateMachine(sourceImpl.getStateMachine(), name),
                target -> cloneTransitions(sourceImpl, target));
    }

    private UsagePointLifeCycle getLifeCycle(String name, Provider<FiniteStateMachine> stateMachineBuilder, Consumer<UsagePointLifeCycleImpl> transitionBuilder) {
        UsagePointLifeCycleImpl lifeCycle = this.dataModel.getInstance(UsagePointLifeCycleImpl.class);
        lifeCycle.setName(name);
        lifeCycle.setStateMachine(stateMachineBuilder.get());
        lifeCycle.save();
        transitionBuilder.accept(lifeCycle);
        return lifeCycle;
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

    private void addDefaultTransitions(UsagePointLifeCycleImpl lifeCycle) {
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

    private void cloneTransitions(UsagePointLifeCycleImpl source, UsagePointLifeCycleImpl target) {
        // clean-up cloned fsm transitions
        FiniteStateMachineUpdater stateMachineUpdater = target.getStateMachine().startUpdate();
        target.getStateMachine().getStates().stream()
                .map(State::getOutgoingStateTransitions)
                .flatMap(Collection::stream)
                .forEach(transition -> stateMachineUpdater.state(transition.getFrom().getId()).prohibit(transition.getEventType()).complete());
        stateMachineUpdater.complete();

        Map<String, UsagePointState> statesMap = target.getStates().stream()
                .collect(Collectors.toMap(state -> ((UsagePointStateImpl) state).getState().getName(), Function.identity()));
        source.getTransitions().forEach(sourceTransition -> target.newTransition(sourceTransition.getName(),
                statesMap.get(((UsagePointStateImpl) sourceTransition.getFrom()).getState().getName()),
                statesMap.get(((UsagePointStateImpl) sourceTransition.getTo()).getState().getName()))
                .withLevels(sourceTransition.getLevels())
                .withChecks(sourceTransition.getChecks().stream().map(MicroCheck::getKey).collect(Collectors.toSet()))
                .withActions(sourceTransition.getActions().stream().map(MicroAction::getKey).collect(Collectors.toSet()))
                .triggeredBy(sourceTransition.getTriggeredBy().orElse(null))
                .complete());
    }
}
