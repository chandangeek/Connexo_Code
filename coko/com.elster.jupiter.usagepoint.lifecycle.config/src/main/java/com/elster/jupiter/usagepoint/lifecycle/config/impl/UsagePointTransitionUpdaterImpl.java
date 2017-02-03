/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class UsagePointTransitionUpdaterImpl implements UsagePointTransition.UsagePointTransitionUpdater {
    private final DataModel dataModel;
    private final FiniteStateMachineService stateMachineService;

    private UsagePointLifeCycleImpl lifeCycle;
    private UsagePointTransitionImpl transition;

    private String name;
    private UsagePointState fromState;
    private UsagePointState toState;
    private StandardStateTransitionEventType eventType;
    private Set<UsagePointTransition.Level> levels;
    private Set<String> microCheckKeys;
    private Set<String> microActionKeys;

    @Inject
    public UsagePointTransitionUpdaterImpl(DataModel dataModel, FiniteStateMachineService stateMachineService) {
        this.dataModel = dataModel;
        this.stateMachineService = stateMachineService;
    }

    UsagePointTransitionUpdaterImpl init(UsagePointLifeCycleImpl lifeCycle, UsagePointTransitionImpl transition) {
        this.lifeCycle = lifeCycle;
        this.transition = transition;
        this.fromState = this.transition.getFrom();
        this.toState = this.transition.getTo();
        this.eventType = this.transition.getTriggeredBy().orElse(null);
        this.name = transition.getName();
        return this;
    }

    @Override
    public UsagePointTransition.UsagePointTransitionUpdater triggeredBy(StandardStateTransitionEventType eventType) {
        this.eventType = eventType;
        return this;
    }

    @Override
    public UsagePointTransition.UsagePointTransitionUpdater withActions(Set<String> microActionKeys) {
        this.microActionKeys = microActionKeys != null ? Collections.unmodifiableSet(microActionKeys) : Collections.emptySet();
        return this;
    }

    @Override
    public UsagePointTransition.UsagePointTransitionUpdater withChecks(Set<String> microCheckKeys) {
        this.microCheckKeys = microCheckKeys != null ? Collections.unmodifiableSet(microCheckKeys) : Collections.emptySet();
        return this;
    }

    @Override
    public UsagePointTransition.UsagePointTransitionUpdater withLevels(Set<UsagePointTransition.Level> levels) {
        this.levels = levels != null ? Collections.unmodifiableSet(levels) : Collections.emptySet();
        return this;
    }

    @Override
    public UsagePointTransition.UsagePointTransitionUpdater withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public UsagePointTransition.UsagePointTransitionUpdater from(UsagePointState state) {
        this.fromState = Objects.requireNonNull(state, "Source state can't be null");
        return this;
    }

    @Override
    public UsagePointTransition.UsagePointTransitionUpdater to(UsagePointState state) {
        this.toState = Objects.requireNonNull(state, "Target state can't be null");
        return this;
    }

    @Override
    public UsagePointTransition complete() {
        StateTransitionEventType eventType = this.transition.getFsmTransition().getEventType();
        boolean needNewStateTransition = cleanUpOldFsmTransitionIfNecessary(eventType);
        if (cleanUpOldFsmEventIfNecessary(eventType)) {
            String eventTypeSymbol = "upl" + this.lifeCycle.getId() + "_state" + this.fromState.getId() + "_tr" + this.transition.getId();
            eventType = this.stateMachineService.newCustomStateTransitionEventType(eventTypeSymbol, UsagePointLifeCycleConfigurationService.COMPONENT_NAME);
        }
        this.transition.init(this.lifeCycle, this.name, this.fromState, this.toState);
        if (needNewStateTransition) {
            StateTransitionEventType eventTypeEffectiveFinal = this.eventType != null ? this.eventType : eventType;
            StateTransition fsmTransition = this.lifeCycle.getStateMachine().startUpdate()
                    .state(this.fromState.getId())
                    .on(eventTypeEffectiveFinal)
                    .setName(this.name)
                    .transitionTo(this.toState.getId())
                    .complete()
                    .getOutgoingStateTransitions().stream().filter(t -> t.getEventType().getSymbol().equals(eventTypeEffectiveFinal.getSymbol())).findFirst()
                    .get();
            this.transition.setFsmTransition(fsmTransition);
        }
        if (this.levels != null) {
            this.transition.setLevels(this.levels);
        }
        if (this.microCheckKeys != null) {
            this.transition.setMicroChecks(this.microCheckKeys);
        }
        if (this.microActionKeys != null) {
            this.transition.setMicroActions(this.microActionKeys);
        }
        this.dataModel.update(this.transition);
        this.lifeCycle.touch();
        return this.transition;
    }

    private boolean toStateWasChanged() {
        return this.toState != null && this.toState.getId() != this.transition.getTo().getId();
    }

    private boolean fromStateWasChanged() {
        return this.fromState != null && this.fromState.getId() != this.transition.getFrom().getId();
    }

    private boolean triggerByWasChanged(StateTransitionEventType eventType) {
        return this.eventType != null && eventType == null
                || this.eventType == null && eventType != null && eventType instanceof StandardStateTransitionEventType
                || this.eventType != null && !this.eventType.equals(eventType);
    }

    private boolean cleanUpOldFsmTransitionIfNecessary(StateTransitionEventType eventType) {
        if (fromStateWasChanged() || toStateWasChanged() || triggerByWasChanged(eventType)) {
            State fsmFromState = this.transition.getFsmTransition().getFrom();
            this.transition.setFsmTransition(null);
            this.dataModel.update(this.transition);
            FiniteStateMachineUpdater stateMachineUpdater = fsmFromState.getFiniteStateMachine().startUpdate();
            stateMachineUpdater.state(fsmFromState.getId()).prohibit(eventType).complete();
            stateMachineUpdater.complete();
            return true;
        }
        return false;
    }

    private boolean cleanUpOldFsmEventIfNecessary(StateTransitionEventType eventType) {
        if (fromStateWasChanged() || triggerByWasChanged(eventType)) {
            if (eventType instanceof CustomStateTransitionEventType) {
                eventType.delete();
            }
            return this.eventType == null;
        }
        return false;
    }
}
