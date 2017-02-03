/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;

public class UsagePointTransitionCreatorImpl implements UsagePointTransition.UsagePointTransitionCreator<UsagePointTransitionCreatorImpl> {
    private final DataModel dataModel;
    private final FiniteStateMachineService stateMachineService;

    private UsagePointLifeCycleImpl lifeCycle;
    private String name;
    private UsagePointState fromState;
    private UsagePointState toState;
    private StandardStateTransitionEventType eventType;
    private Set<UsagePointTransition.Level> levels;
    private Set<String> microCheckKeys;
    private Set<String> microActionKeys;

    @Inject
    public UsagePointTransitionCreatorImpl(DataModel dataModel, FiniteStateMachineService stateMachineService) {
        this.dataModel = dataModel;
        this.stateMachineService = stateMachineService;
    }

    UsagePointTransitionCreatorImpl init(UsagePointLifeCycleImpl lifeCycle, String name, UsagePointState from, UsagePointState to) {
        this.lifeCycle = lifeCycle;
        this.name = name != null ? name.trim() : null;
        this.fromState = from;
        this.toState = to;
        return this;
    }

    @Override
    public UsagePointTransitionCreatorImpl triggeredBy(StandardStateTransitionEventType eventType) {
        this.eventType = eventType;
        return this;
    }

    @Override
    public UsagePointTransitionCreatorImpl withActions(Set<String> microActionKeys) {
        this.microActionKeys = microActionKeys != null ? Collections.unmodifiableSet(microActionKeys) : Collections.emptySet();
        return this;
    }

    @Override
    public UsagePointTransitionCreatorImpl withChecks(Set<String> microCheckKeys) {
        this.microCheckKeys = microCheckKeys != null ? Collections.unmodifiableSet(microCheckKeys) : Collections.emptySet();
        return this;
    }

    @Override
    public UsagePointTransitionCreatorImpl withLevels(Set<UsagePointTransition.Level> levels) {
        this.levels = levels != null ? Collections.unmodifiableSet(levels) : Collections.emptySet();
        return this;
    }

    @Override
    public UsagePointTransition complete() {
        UsagePointTransitionImpl transition = this.dataModel.getInstance(UsagePointTransitionImpl.class)
                .init(this.lifeCycle, this.name, this.fromState, this.toState);
        transition.setLevels(this.levels);
        transition.setMicroChecks(this.microCheckKeys);
        transition.setMicroActions(this.microActionKeys);
        this.lifeCycle.addTransition(transition);
        StateTransitionEventType eventType = this.eventType;
        String eventTypeSymbol = this.eventType == null
                ? "upl" + this.lifeCycle.getId() + "_state" + this.fromState.getId() + "_tr" + transition.getId()
                : eventType.getSymbol();
        if (eventType == null) {
            eventType = this.stateMachineService.newCustomStateTransitionEventType(eventTypeSymbol, UsagePointLifeCycleConfigurationService.COMPONENT_NAME);
        }
        StateTransition fsmTransition = this.lifeCycle.getStateMachine().startUpdate()
                .state(this.fromState.getId())
                .on(eventType)
                .setName(this.name)
                .transitionTo(this.toState.getId())
                .complete()
                .getOutgoingStateTransitions().stream().filter(t -> t.getEventType().getSymbol().equals(eventTypeSymbol)).findFirst()
                .get();
        transition.setFsmTransition(fsmTransition);
        this.dataModel.update(transition);
        this.lifeCycle.touch();
        return transition;
    }
}
