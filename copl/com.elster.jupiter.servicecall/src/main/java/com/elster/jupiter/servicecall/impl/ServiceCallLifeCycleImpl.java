package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

/**
 * Created by bvn on 2/4/16.
 */
public class ServiceCallLifeCycleImpl implements IServiceCallLifeCycle {

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

    private long id;
    private String name;
    private Reference<FiniteStateMachine> finiteStateMachine = Reference.empty();

    private final DataModel dataModel;
    private final Clock clock;

    @Inject
    public ServiceCallLifeCycleImpl(DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
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

    public void triggerTransition(ServiceCall serviceCall, DefaultState to) {

        DefaultState from = serviceCall.getState();
//        finiteStateMachine.get().getTransitions().get(0).getEventType()

        CustomStateTransitionEventType eventType = decorate(finiteStateMachine.get().getTransitions()
                .stream())
                .filter(stateTransition -> to.matches(stateTransition.getTo()))
                .filter(stateTransition -> from.matches(stateTransition.getFrom()))
                .map(StateTransition::getEventType)
                .filterSubType(CustomStateTransitionEventType.class)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);

        eventType.newInstance(
                finiteStateMachine.get(),
                String.valueOf(serviceCall.getId()),
                from.getKey(),
                clock.instant(),
                ImmutableMap.of(ServiceCall.class.getName(), serviceCall))
                .publish();

    }
}
