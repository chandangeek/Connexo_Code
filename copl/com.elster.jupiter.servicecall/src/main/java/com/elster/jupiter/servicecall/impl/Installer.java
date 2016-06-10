package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by bvn on 3/7/16.
 */
public class Installer implements FullInstaller {
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

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        createMessageHandler(defaultQueueTableSpec, ServiceCallServiceImpl.SERIVCE_CALLS_DESTINATION_NAME, ServiceCallServiceImpl.SERIVCE_CALLS_SUBSCRIBER_NAME, logger);
        doTry(
                "Install default Service Call Life Cycle.",
                this::installDefaultLifeCycle,
                logger
        );
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, String subscriberName, Logger logger) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = doTry(
                    "Create Queue : " + ServiceCallServiceImpl.SERIVCE_CALLS_DESTINATION_NAME,
                    () -> {
                        DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
                        destinationSpec.activate();
                        return destinationSpec;
                    },
                    logger
            );
            doTry(
                    "Create subsriber " + ServiceCallServiceImpl.SERIVCE_CALLS_SUBSCRIBER_NAME + " on " + ServiceCallServiceImpl.SERIVCE_CALLS_DESTINATION_NAME,
                    () -> queue.subscribe(subscriberName),
                    logger
            );
        } else {
            DestinationSpec queue = destinationSpecOptional.get();
            boolean notSubscribedYet = queue
                    .getSubscribers()
                    .stream()
                    .noneMatch(spec -> spec.getName().equals(subscriberName));
            if (notSubscribedYet) {
                doTry(
                        "Create subsriber " + ServiceCallServiceImpl.SERIVCE_CALLS_SUBSCRIBER_NAME + " on " + ServiceCallServiceImpl.SERIVCE_CALLS_DESTINATION_NAME,
                        () -> {
                            queue.activate();
                            queue.subscribe(subscriberName);
                        },
                        logger
                );
            }
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
