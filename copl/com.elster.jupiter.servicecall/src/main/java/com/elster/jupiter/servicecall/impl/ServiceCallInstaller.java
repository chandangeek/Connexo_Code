package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceCallInstaller {
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final Logger logger = Logger.getLogger(ServiceCallInstaller.class.getName());

    private final FiniteStateMachineService finiteStateMachineService;
    private final IServiceCallService serviceCallService;
    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
    ServiceCallInstaller(FiniteStateMachineService finiteStateMachineService, IServiceCallService serviceCallService, DataModel dataModel, MessageService messageService) {
        this.finiteStateMachineService = finiteStateMachineService;
        this.serviceCallService = serviceCallService;
        this.dataModel = dataModel;
        this.messageService = messageService;
    }

    public void install() {
        try {
            this.dataModel.install(true, true);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        installDefaultLifeCycle();
        createServiceCallQueue();
    }

    private void createServiceCallQueue() {
        try {
            QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(ServiceCallServiceImpl.SERIVCE_CALLS_DESTINATION_NAME, DEFAULT_RETRY_DELAY_IN_SECONDS);
            destinationSpec.activate();
            destinationSpec.subscribe(ServiceCallServiceImpl.SERIVCE_CALLS_SUBSCRIBER_NAME);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void installDefaultLifeCycle() {
        Map<String, CustomStateTransitionEventType> eventTypes = this.findOrCreateStateTransitionEventTypes();
        this.createDefaultLifeCycle(
                TranslationKeys.DEFAULT_SERVICE_CALL_LIFE_CYCLE_NAME.getKey()
        );
    }

    private void createDefaultLifeCycle(String name) {
        serviceCallService.getDefaultServiceCallLifeCycle()
                .orElseGet(() -> serviceCallService.createServiceCallLifeCycle(name).create());
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

}
