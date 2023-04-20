/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.Command;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;

import static com.elster.jupiter.messaging.MessageService.PRIORITIZED_RAW_QUEUE_TABLE;

public class UpgraderV10_7 implements Upgrader {

    static final String OLD_SERVICE_CALLS_DESTINATION_NAME = "SerivceCalls";

    private final DataModel dataModel;
    private final Installer installerV10_7;
    private final MessageService messageService;
    private final AppService appService;
    private final Logger logger;

    @Inject
    UpgraderV10_7(DataModel dataModel, Installer installerV10_7, MessageService messageService, AppService appService) {
        this.dataModel = dataModel;
        this.installerV10_7 = installerV10_7;
        this.messageService = messageService;
        this.appService = appService;
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7));
        QueueTableSpec defaultQueueTableSpec = installerV10_7.createDefaultQueueTableSpecIfNotExist(PRIORITIZED_RAW_QUEUE_TABLE);
        installerV10_7.createMessageHandler(defaultQueueTableSpec, ServiceCallService.SERVICE_CALLS_SUBSCRIBER_NAME,
                ServiceCallService.SERVICE_CALLS_DESTINATION_NAME, TranslationKeys.SERVICE_CALL_SUBSCRIBER, ServiceCallService.COMPONENT_NAME, logger);
        installerV10_7.createMessageHandler(defaultQueueTableSpec, ServiceCallService.SERVICE_CALLS_ISSUE_SUBSCRIBER_NAME,
                ServiceCallService.SERVICE_CALLS_ISSUE_DESTINATION_NAME, TranslationKeys.SERVICE_CALL_ISSUE_SUBSCRIBER, ServiceCallService.COMPONENT_NAME, logger);
        replaceSubscriber();
        deleteOldDestination();
    }

    private Optional<SubscriberSpec> getSubscriber4(String destinationName) {
        return messageService.getSubscribers().stream()
                .filter((SubscriberSpec subscriberSpec) -> subscriberSpec.getDestination().getName().equals(destinationName))
                .findFirst();
    }

    private void replaceSubscriber() {
        SubscriberSpec newSubscriber = getSubscriber4(ServiceCallService.SERVICE_CALLS_DESTINATION_NAME).orElseThrow(() -> new IllegalStateException("Subscriber '" + ServiceCallService.SERVICE_CALLS_DESTINATION_NAME + "' was not created."));
        Optional<SubscriberSpec> oldSubscriberSpec = getSubscriber4(OLD_SERVICE_CALLS_DESTINATION_NAME);
        oldSubscriberSpec.ifPresent(oldSubscriber -> {
            appService.findAppServers().stream()
                    .map(AppServer::getSubscriberExecutionSpecs)
                    .flatMap(Collection::stream)
                    .filter(spec -> spec.getSubscriberSpec().getName().equals(oldSubscriber.getName()))
                    .filter(spec -> spec.getSubscriberSpec().getDestination().getName().equals(oldSubscriber.getDestination().getName()))
                    .forEach(subscriberSpec -> {
                        AppServer appServer = subscriberSpec.getAppServer();
                        appServer.removeSubscriberExecutionSpec(subscriberSpec);
                        appServer.createSubscriberExecutionSpec(newSubscriber, subscriberSpec.getThreadCount());
                        appServer.sendCommand(new AppServerCommand(Command.CONFIG_CHANGED));
                    });
        });
    }

    private void deleteOldDestination() {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(OLD_SERVICE_CALLS_DESTINATION_NAME);
        destinationSpec.ifPresent(destination -> {
            destination.unSubscribe(OLD_SERVICE_CALLS_DESTINATION_NAME);
            destination.delete();
        });
    }

}
