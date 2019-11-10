/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.impl.EngineConfigurationServiceImpl;
import com.energyict.mdc.engine.impl.status.ComServerAliveHandlerFactory;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;

public class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;
    private final TaskService taskService;
    private final MessageService messageService;
    private final BundleContext bundleContext;

    private static final String TASK_NAME = "ComServerAliveTask";
    private static final String TASK_SCHEDULE = "0 0/1 * 1/1 * ? *";
    private static final int TASK_RETRY_DELAY = 60;

    private static final TranslationKey TASK_DEFAULT_NAME_TRANSLATION = new TranslationKey() {
        @Override
        public String getKey() {
            return ComServerAliveHandlerFactory.COM_SERVER_ALIVE_TIMEOUT_TASK_SUBSCRIBER;
        }

        @Override
        public String getDefaultFormat() {
            return "Com Server Alive";
        }
    };

    @Inject
    UpgraderV10_7(DataModel dataModel, TaskService taskService, MessageService messageService, BundleContext bundleContext) {
        this.dataModel = dataModel;
        this.taskService = taskService;
        this.messageService = messageService;
        this.bundleContext = bundleContext;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        createComServerAliveStatusTask();
    }

    private void createComServerAliveStatusTask() {
        String property = bundleContext.getProperty(EngineConfigurationServiceImpl.COM_SERVER_STATUS_ALIVE_FREQ_PROP);
        DestinationSpec destination = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get()
                .createDestinationSpec(ComServerAliveHandlerFactory.COM_SERVER_ALIVE_TASK_DESTINATION, TASK_RETRY_DELAY);
        destination.activate();
        destination.subscribe(TASK_DEFAULT_NAME_TRANSLATION, EngineConfigurationService.COMPONENT_NAME, Layer.DOMAIN);
        taskService.newBuilder()
                .setApplication("Admin")
                .setName(TASK_NAME)
                .setScheduleExpressionString(property == null ? TASK_SCHEDULE : "0 0/" + property + " * 1/1 * ? *")
                .setDestination(destination)
                .setPayLoad("payload")
                .scheduleImmediately(true)
                .build();
    }
}
