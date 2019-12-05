/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.status;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.status.StatusService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name = "com.energyict.mdc.engine.impl.status.ComServerAliveStatusHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + ComServerAliveStatusHandlerFactory.COM_SERVER_ALIVE_TIMEOUT_TASK_SUBSCRIBER,
                "destination=" + ComServerAliveStatusHandlerFactory.COM_SERVER_ALIVE_TASK_DESTINATION},
        immediate = true)
public class ComServerAliveStatusHandlerFactory implements MessageHandlerFactory {
    public static final String COM_SERVER_ALIVE_TASK_DESTINATION = "ComServerStatusTopic";
    public static final String COM_SERVER_ALIVE_TIMEOUT_TASK_SUBSCRIBER = "ComServerStatusSubs";

    private volatile TaskService taskService;
    private volatile Clock clock;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile StatusService statusService;

    public ComServerAliveStatusHandlerFactory() {
    }

    @Inject
    public ComServerAliveStatusHandlerFactory(TaskService taskService) {
        setTaskService(taskService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new ComServerAliveStatusHandler(clock, engineConfigurationService, statusService));
    }

    @Reference
    public final void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public final void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }
}
