package com.elster.jupiter.servicecalls.impl;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.servicecalls.DefaultState;
import com.elster.jupiter.servicecalls.ServiceCallLifeCycle;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by bvn on 2/4/16.
 */
public class ServiceCallInstaller {
    private final Logger logger = Logger.getLogger(ServiceCallInstaller.class.getName());

    private final FiniteStateMachineService finiteStateMachineService;
    private final DataModel dataModel;

    public ServiceCallInstaller(FiniteStateMachineService finiteStateMachineService, DataModel dataModel) {
        this.finiteStateMachineService = finiteStateMachineService;
        this.dataModel = dataModel;
    }

    public void install() {
        try {
            this.dataModel.install(true, true);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        installDefaultLifeCycle();
    }

    public ServiceCallLifeCycle installDefaultLifeCycle() {
        Map<String, CustomStateTransitionEventType> eventTypes = this.findOrCreateStateTransitionEventTypes();
        return this.createDefaultLifeCycle(
                TranslationKeys.DEFAULT_SERVICE_CALL_LIFE_CYCLE_NAME.getKey(),
                eventTypes);
    }

    private ServiceCallLifeCycle createDefaultLifeCycle(String name, Map<String, CustomStateTransitionEventType> eventTypes) {
//        Optional<FiniteStateMachine> stateMachine = finiteStateMachineService.findFiniteStateMachineByName(TranslationKeys.DEFAULT_SERVICE_CALL_LIFE_CYCLE_NAME.getKey());

        FiniteStateMachine defaultStateMachine = this.createDefaultFiniteStateMachine(name, eventTypes);
        // create and store service call life cycle
        ServiceCallLifeCycleImpl serviceCallLifeCycle = new ServiceCallLifeCycleImpl(dataModel);
        serviceCallLifeCycle.init(name, defaultStateMachine);
        serviceCallLifeCycle.save();
        return serviceCallLifeCycle;
    }

    private Map<String, CustomStateTransitionEventType> findOrCreateStateTransitionEventTypes() {
        // Create default StateTransitionEventTypes
        this.logger.fine(() -> "Finding (or creating) default finite state machine transitions...");
        Map<String, CustomStateTransitionEventType> eventTypes = Stream
                .of(DefaultCustomStateTransitionEventType.values())
                .map(each -> each.findOrCreate(this.finiteStateMachineService))
                .collect(Collectors.toMap(
                        StateTransitionEventType::getSymbol,
                        Function.identity()));
        this.logger.fine(() -> "Found (or created) default finite state machine transitions");
        return eventTypes;
    }

    private FiniteStateMachine createDefaultFiniteStateMachine(String name, Map<String, CustomStateTransitionEventType> eventTypes) {
        StateTransitionEventType scheduledEventType = eventTypes.get(DefaultCustomStateTransitionEventType.SCHEDULED.getSymbol());
        StateTransitionEventType pendingEventType = eventTypes.get(DefaultCustomStateTransitionEventType.PENDING.getSymbol());
        StateTransitionEventType ongoingEventType = eventTypes.get(DefaultCustomStateTransitionEventType.ONGOING.getSymbol());
        StateTransitionEventType cancelledEventType = eventTypes.get(DefaultCustomStateTransitionEventType.CANCELLED.getSymbol());
        StateTransitionEventType pausedEventType = eventTypes.get(DefaultCustomStateTransitionEventType.PAUSED.getSymbol());
        StateTransitionEventType waitingEventType = eventTypes.get(DefaultCustomStateTransitionEventType.WAITING.getSymbol());
        StateTransitionEventType successEventType = eventTypes.get(DefaultCustomStateTransitionEventType.SUCCESSFUL.getSymbol());
        StateTransitionEventType partialSuccessEventType = eventTypes.get(DefaultCustomStateTransitionEventType.PARTIAL_SUCCESS.getSymbol());
        StateTransitionEventType failedEventType = eventTypes.get(DefaultCustomStateTransitionEventType.FAILED.getSymbol());
        StateTransitionEventType rejectedEventType = eventTypes.get(DefaultCustomStateTransitionEventType.REJECTED.getSymbol());

        FiniteStateMachineBuilder builder = this.finiteStateMachineService.newFiniteStateMachine(name);
        // Create default States
        FiniteStateMachineBuilder.StateBuilder scheduledBuilder = builder.newStandardState(DefaultState.SCHEDULED.getKey());

        State canceled = builder
                .newStandardState(DefaultState.CANCELLED.getKey())
                .on(scheduledEventType).transitionTo(scheduledBuilder, TranslationKeys.TRANSITION_FROM_CANCELLED_TO_SCHEDULED)
                .complete();
        State rejected = builder.newStandardState(DefaultState.REJECTED.getKey()).complete();
        State failed = builder
                .newStandardState(DefaultState.FAILED.getKey())
                .on(scheduledEventType).transitionTo(scheduledBuilder, TranslationKeys.TRANSITION_FROM_FAILED_TO_SCHEDULED)
                .complete();
        State successful = builder.newStandardState(DefaultState.SUCCESSFUL.getKey()).complete();
        State partialSuccess = builder
                .newStandardState(DefaultState.PARTIAL_SUCCESS.getKey())
                .on(scheduledEventType).transitionTo(scheduledBuilder, TranslationKeys.TRANSITION_FROM_PARTIAL_SUCCESS_TO_SCHEDULED)
                .complete();

        FiniteStateMachineBuilder.StateBuilder ongoingBuilder = builder.newStandardState(DefaultState.ONGOING.getKey());
        State paused = builder
                .newStandardState(DefaultState.PAUSED.getKey())
                .on(ongoingEventType).transitionTo(ongoingBuilder, TranslationKeys.TRANSITION_FROM_PAUSED_TO_ONGOING)
                .on(cancelledEventType).transitionTo(canceled, TranslationKeys.TRANSITION_FROM_PAUSED_TO_CANCELLED)
                .complete();
        State waiting = builder
                .newStandardState(DefaultState.WAITING.getKey())
                .on(ongoingEventType).transitionTo(ongoingBuilder, TranslationKeys.TRANSITION_FROM_WAITING_TO_ONGOING)
                .on(cancelledEventType).transitionTo(canceled, TranslationKeys.TRANSITION_FROM_WAITING_TO_CANCELLED)
                .complete();
        ongoingBuilder
                .on(cancelledEventType).transitionTo(canceled, TranslationKeys.TRANSITION_FROM_WAITING_TO_CANCELLED)
                .on(waitingEventType).transitionTo(waiting, TranslationKeys.TRANSITION_FROM_ONGOING_TO_WAITING)
                .on(pausedEventType).transitionTo(paused, TranslationKeys.TRANSITION_FROM_ONGOING_TO_PAUSED)
                .on(successEventType).transitionTo(successful, TranslationKeys.TRANSITION_FROM_ONGOING_TO_SUCCESS)
                .on(partialSuccessEventType).transitionTo(partialSuccess, TranslationKeys.TRANSITION_FROM_ONGOING_TO_PARTIAL_SUCCESS)
                .on(failedEventType).transitionTo(failed, TranslationKeys.TRANSITION_FROM_ONGOING_TO_FAILED)
                .complete();
        State pending = builder
                .newStandardState(DefaultState.PENDING.getKey())
                .on(ongoingEventType).transitionTo(ongoingBuilder, TranslationKeys.TRANSITION_FROM_PENDING_TO_ONGOING)
                .on(cancelledEventType).transitionTo(canceled, TranslationKeys.TRANSITION_FROM_PENDING_TO_CANCELLED)
                .complete();
        State scheduled = scheduledBuilder
                .on(pendingEventType).transitionTo(pending, TranslationKeys.TRANSITION_FROM_SCHEDULED_TO_PENDING)
                .on(cancelledEventType).transitionTo(canceled, TranslationKeys.TRANSITION_FROM_SCHEDULED_TO_CANCELLED)
                .complete();
        State created = builder
                .newStandardState(DefaultState.CREATED.getKey())
                .on(pendingEventType).transitionTo(pending, TranslationKeys.TRANSITION_FROM_CREATED_TO_PENDING)
                .on(scheduledEventType).transitionTo(scheduled, TranslationKeys.TRANSITION_FROM_CREATED_TO_SCHEDULED)
                .on(rejectedEventType).transitionTo(rejected, TranslationKeys.TRANSITION_FROM_CREATED_TO_REJECTED)
                .complete();

        this.logger.fine(() -> "Creating default finite state machine...");
        FiniteStateMachine stateMachine = builder.complete(created);
        this.logger.fine(() -> "Created default finite state machine");
        return stateMachine;
    }


}
