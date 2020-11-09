/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
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
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static com.elster.jupiter.servicecall.impl.ServiceCallServiceImpl.SERVICECALLS_RAW_QUEUE_TABLE;
import static com.elster.jupiter.servicecall.impl.ServiceCallServiceImpl.SERVICE_CALLS_DESTINATION_NAME;
import static com.elster.jupiter.servicecall.impl.ServiceCallServiceImpl.SERVICE_CALLS_SUBSCRIBER_NAME;

public class UpgraderV10_8_7 implements Upgrader {

    private final Installer installer;
    private final MessageService messageService;
    private final AppService appService;
    private final Logger logger;

    @Inject
    UpgraderV10_8_7(Installer installer, MessageService messageService, AppService appService) {
        this.installer = installer;
        this.messageService = messageService;
        this.appService = appService;
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        Map<AppServer, Integer> appServersAndThreadCounts = new HashMap<>();
        QueueTableSpec defaultQueueTableSpec = installer.createDefaultQueueTableSpecIfNotExist(SERVICECALLS_RAW_QUEUE_TABLE);
        getSubscriber(SERVICE_CALLS_SUBSCRIBER_NAME).ifPresent(subscriber -> {
            removeSubscriber(subscriber, appServersAndThreadCounts);
            deleteDestination(subscriber.getDestination());
        });
        DestinationSpec destinationSpec = installer.createMessageHandler(defaultQueueTableSpec, SERVICE_CALLS_SUBSCRIBER_NAME,
                SERVICE_CALLS_DESTINATION_NAME, TranslationKeys.SERVICE_CALL_SUBSCRIBER, ServiceCallService.COMPONENT_NAME, logger);
        addSubscriber(appServersAndThreadCounts, destinationSpec);
    }

    private Optional<SubscriberSpec> getSubscriber(String subscriberName) {
        return messageService.getSubscribers().stream()
                .filter((SubscriberSpec subscriberSpec) -> subscriberSpec.getName().equals(subscriberName))
                .findFirst();
    }

    private void addSubscriber(Map<AppServer, Integer> appServersAndThreadCounts, DestinationSpec destinationSpec) {
        SubscriberSpec newSubscriber = destinationSpec
                .getSubscribers()
                .stream()
                .filter(spec -> spec.getName().equals(SERVICE_CALLS_SUBSCRIBER_NAME)).findFirst().orElseThrow(() -> new IllegalStateException("Subscriber '" + SERVICE_CALLS_SUBSCRIBER_NAME + "' was not created."));
        for (Map.Entry<AppServer, Integer> appServerAndThreadCount : appServersAndThreadCounts.entrySet()) {
            appServerAndThreadCount.getKey().createSubscriberExecutionSpec(newSubscriber, appServerAndThreadCount.getValue());
            appServerAndThreadCount.getKey().sendCommand(new AppServerCommand(Command.CONFIG_CHANGED));
        }
    }

    private void removeSubscriber(SubscriberSpec subscriber, Map<AppServer, Integer> appServersAndThreadCounts) {
        appService.findAppServers().stream()
                .map(AppServer::getSubscriberExecutionSpecs)
                .flatMap(Collection::stream)
                .filter(spec -> spec.getSubscriberSpec().getName().equals(subscriber.getName()))
                .filter(spec -> spec.getSubscriberSpec().getDestination().getName().equals(subscriber.getDestination().getName()))
                .forEach(subscriberSpec -> {
                    AppServer appServer = subscriberSpec.getAppServer();
                    appServersAndThreadCounts.put(appServer, subscriberSpec.getThreadCount());
                    appServer.removeSubscriberExecutionSpec(subscriberSpec);
                });
    }

    private void deleteDestination(DestinationSpec destination) {
        destination.unSubscribe(SERVICE_CALLS_SUBSCRIBER_NAME);
        destination.delete();
    }
}
