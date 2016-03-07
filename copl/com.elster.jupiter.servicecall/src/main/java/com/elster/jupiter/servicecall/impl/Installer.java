package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.servicecall.ServiceCallService;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by bvn on 3/7/16.
 */
public class Installer {
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());

    private final MessageService messageService;
    private final ServiceCallService serviceCallService;
    private final FiniteStateMachineService finiteStateMachineService;
    private final DataModel dataModel;

    @Inject
    public Installer(MessageService messageService, ServiceCallService serviceCallService, FiniteStateMachineService finiteStateMachineService, DataModel dataModel) {
        this.messageService = messageService;
        this.serviceCallService = serviceCallService;
        this.finiteStateMachineService = finiteStateMachineService;
        this.dataModel = dataModel;
    }

    public void install() {
        try {
            this.dataModel.install(true, true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        createMessageHandler(defaultQueueTableSpec, ServiceCallServiceImpl.DESTINATION_NAME, ServiceCallServiceImpl.SUBSCRIBER_NAME);
        installDefaultLifeCycle();
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, String subscriberName) {
        try {
            Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
            if (!destinationSpecOptional.isPresent()) {
                DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
                queue.activate();
                queue.subscribe(subscriberName);
            } else {
                boolean notSubscribedYet = !destinationSpecOptional.get().getSubscribers().stream().anyMatch(spec -> spec.getName().equals(subscriberName));
                if (notSubscribedYet) {
                    destinationSpecOptional.get().activate();
                    destinationSpecOptional.get().subscribe(subscriberName);
                }
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
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
        LOGGER.fine(() -> "Finding (or creating) default finite state machine transitions...");
        Map<String, CustomStateTransitionEventType> eventTypes = Stream
                .of(DefaultCustomStateTransitionEventType.values())
                .map(each -> each.findOrCreate(this.finiteStateMachineService))
                .collect(Collectors.toMap(
                        StateTransitionEventType::getSymbol,
                        Function.identity()));
        LOGGER.fine(() -> "Found (or created) default finite state machine transitions");
        return eventTypes;
    }

}
