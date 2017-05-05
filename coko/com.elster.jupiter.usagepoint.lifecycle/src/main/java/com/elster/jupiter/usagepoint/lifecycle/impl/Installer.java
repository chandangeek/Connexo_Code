/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Never;

import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

public class Installer implements FullInstaller {

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final DataModel dataModel;
    private final MeteringService meteringService;
    private final MessageService messageService;
    private final TaskService taskService;
    private final Thesaurus thesaurus;
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private final ServerUsagePointLifeCycleService usagePointLifeCycleService;

    @Inject
    public Installer(DataModel dataModel, MeteringService meteringService, MessageService messageService, TaskService taskService,
                     UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService, Thesaurus thesaurus,
                     UsagePointLifeCycleService usagePointLifeCycleService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
        this.messageService = messageService;
        this.taskService = taskService;
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
        this.thesaurus = thesaurus;
        this.usagePointLifeCycleService = (ServerUsagePointLifeCycleService) usagePointLifeCycleService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(this.dataModel, Version.latest());
        doTry(
                "Create message handlers",
                this::createMessageHandlers,
                logger
        );
        doTry(
                "Create recurrent task",
                this::createChangeRequestRecurrentTask,
                logger
        );
        doTry(
                "Create default usage point lifecycle",
                this::createLifeCycle,
                logger
        );
        doTry(
                "Set default life cycle to all usage points",
                this::setInitialStateForInstalledUsagePoints,
                logger
        );
    }

    private void createMessageHandlers() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        this.createMessageHandler(defaultQueueTableSpec, ServerUsagePointLifeCycleService.DESTINATION_NAME, TranslationKeys.QUEUE_SUBSCRIBER);
    }

    private void createChangeRequestRecurrentTask() {
        this.messageService.getDestinationSpec(ServerUsagePointLifeCycleService.DESTINATION_NAME)
                .ifPresent(destinationSpec -> this.taskService.newBuilder()
                        .setApplication("Pulse")
                        .setName(ServerUsagePointLifeCycleService.EXECUTOR_TASK)
                        .setScheduleExpression(Never.NEVER)
                        .setDestination(destinationSpec)
                        .setPayLoad(TranslationKeys.QUEUE_SUBSCRIBER.getDefaultFormat())
                        .build());
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberName) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
            queue.activate();
            queue.subscribe(subscriberName, UsagePointLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
        } else {
            boolean notSubscribedYet = destinationSpecOptional.get().getSubscribers().stream().noneMatch(spec -> spec.getName().equals(subscriberName.getKey()));
            if (notSubscribedYet) {
                destinationSpecOptional.get().activate();
                destinationSpecOptional.get().subscribe(subscriberName, UsagePointLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
            }
        }
    }

    private void setInitialStateForInstalledUsagePoints() {
        meteringService.getUsagePointQuery()
                .select(Condition.TRUE)
                .forEach(UsagePoint::setInitialState);
    }



    private void createLifeCycle() {
        this.usagePointLifeCycleConfigurationService.newUsagePointLifeCycle(UsagePointLifeCycleConfigurationService.LIFE_CYCLE_KEY)
                .markAsDefault();
    }
}
