/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LifeCycleIsStillInUseException;
import com.elster.jupiter.servicecall.NoTransitionException;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.util.conditions.Where;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;
import static com.elster.jupiter.util.streams.Predicates.on;

/**
 * Created by bvn on 2/4/16.
 */
class ServiceCallLifeCycleImpl implements IServiceCallLifeCycle {

    private final Thesaurus thesaurus;

    public enum Fields {
        name("name"),
        finiteStateMachine("finiteStateMachine");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }

    }

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    private String name;
    private Reference<FiniteStateMachine> finiteStateMachine = Reference.empty();

    private final DataModel dataModel;
    private final Clock clock;

    @Inject
    ServiceCallLifeCycleImpl(DataModel dataModel, Thesaurus thesaurus, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.thesaurus = thesaurus;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    void init(String name, FiniteStateMachine finiteStateMachine) {
        this.name = name;
        this.finiteStateMachine.set(finiteStateMachine);
    }

    public void save() {
        if (this.getId() > 0) {
            Save.UPDATE.save(this.dataModel, this, Save.Update.class);
        } else {
            Save.CREATE.save(this.dataModel, this, Save.Create.class);
        }
    }

    public FiniteStateMachine getFiniteStateMachine() {
        return finiteStateMachine.orElseThrow(IllegalStateException::new);
    }

    @Override
    public Optional<State> getState(DefaultState defaultState) {
        return finiteStateMachine.get()
                .getState(defaultState.getKey());
    }

    @Override
    public void delete() {
        this.validateDelete();
        dataModel.remove(this);
        this.finiteStateMachine.get().delete();
    }

    private void validateDelete() {
        // find all service call types linked to this life cycle
        if (!dataModel.query(ServiceCallType.class)
                .select(Where.where(ServiceCallTypeImpl.Fields.serviceCallLifeCycle.fieldName()).isEqualTo(this))
                .isEmpty()) {
            throw new LifeCycleIsStillInUseException(thesaurus, MessageSeeds.LIFE_CYCLE_STILL_IN_USE, this);
        }
    }
    @Override
    public void triggerTransition(ServiceCall serviceCall, DefaultState to) {
        DefaultState from = serviceCall.getState();
        CustomStateTransitionEventType eventType = decorate(finiteStateMachine.get().getTransitions()
                .stream())
                .filter(stateTransition -> to.matches(stateTransition.getTo()))
                .filter(stateTransition -> from.matches(stateTransition.getFrom()))
                .map(StateTransition::getEventType)
                .filterSubType(CustomStateTransitionEventType.class)
                .findAny()
                .orElseThrow(() -> new NoTransitionException(thesaurus, MessageSeeds.NO_TRANSITION, from.getKey(), to.getKey()));

        eventType
            .newInstance(
                finiteStateMachine.get(),
                String.valueOf(serviceCall.getId()),
                ServiceCall.class.getName(),
                from.getKey(),
                clock.instant(),
                ImmutableMap.of(ServiceCall.class.getName(), serviceCall))
            .publish();
    }

    @Override
    public boolean canTransition(DefaultState currentState, DefaultState targetState) {
        return getFiniteStateMachine().getTransitions()
                .stream()
                .filter(on(StateTransition::getFrom).test(currentState::matches))
                .anyMatch(on(StateTransition::getTo).test(targetState::matches));
    }
}
