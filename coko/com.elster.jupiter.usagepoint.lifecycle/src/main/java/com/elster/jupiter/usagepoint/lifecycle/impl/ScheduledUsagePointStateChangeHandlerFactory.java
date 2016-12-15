package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.tasks.TaskService;

import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.usagepoint.lifecycle.ScheduledUsagePointStateChangeHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + ServerUsagePointLifeCycleService.QUEUE_SUBSCRIBER,
                "destination=" + ServerUsagePointLifeCycleService.DESTINATION_NAME},
        immediate = true)
public class ScheduledUsagePointStateChangeHandlerFactory implements MessageHandlerFactory {
    private TaskService taskService;
    private ServerUsagePointLifeCycleService lifeCycleService;

    public ScheduledUsagePointStateChangeHandlerFactory() {
    }

    @Inject
    public ScheduledUsagePointStateChangeHandlerFactory(TaskService taskService, ServerUsagePointLifeCycleService lifeCycleService) {
        this();
        setTaskService(taskService);
        setLifeCycleService(lifeCycleService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return this.taskService.createMessageHandler(new ScheduledUsagePointStateChangeHandler(this.lifeCycleService));
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setLifeCycleService(ServerUsagePointLifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }
}