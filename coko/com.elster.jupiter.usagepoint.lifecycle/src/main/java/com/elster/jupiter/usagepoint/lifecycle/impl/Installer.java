package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.util.time.Never;

import javax.inject.Inject;
import java.util.logging.Logger;

public class Installer implements FullInstaller {
    private final DataModel dataModel;
    private final MessageService messageService;
    private final TaskService taskService;
    private final Thesaurus thesaurus;
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;

    @Inject
    public Installer(DataModel dataModel,
                     MessageService messageService,
                     TaskService taskService, UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.taskService = taskService;
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(this.dataModel, Version.latest());
        doTry(
                "Create recurrent task",
                this::createChangeRequestTask,
                logger
        );
        doTry(
                "Create default usage point lifecycle",
                this::createLifeCycle,
                logger
        );
    }

    private void createChangeRequestTask() {
        this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS).ifPresent(destinationSpec -> {
            destinationSpec.subscribe(TranslationKeys.QUEUE_SUBSCRIBER, UsagePointLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
            this.taskService.newBuilder().setApplication("Pulse")
                    .setName(ServerUsagePointLifeCycleService.EXECUTOR_TASK)
                    .setScheduleExpression(Never.NEVER)
                    .setDestination(destinationSpec)
                    .setPayLoad(TranslationKeys.QUEUE_SUBSCRIBER.getDefaultFormat())
                    .build();
        });
    }

    private void createLifeCycle() {
        this.usagePointLifeCycleConfigurationService.newUsagePointLifeCycle(this.thesaurus.getFormat(TranslationKeys.LIFE_CYCLE_NAME).format())
                .markAsDefault();
    }
}
