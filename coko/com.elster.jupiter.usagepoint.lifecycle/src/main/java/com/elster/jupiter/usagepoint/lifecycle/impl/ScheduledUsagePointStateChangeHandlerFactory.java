package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.tasks.TaskService;

import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "ScheduledUsagePointStateChangeHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + ServerUsagePointLifeCycleService.QUEUE_SUBSCRIBER,
                "destination=" + EventService.JUPITER_EVENTS},
        immediate = true)
public class ScheduledUsagePointStateChangeHandlerFactory implements MessageHandlerFactory {
    private TaskService taskService;
    private ServerUsagePointLifeCycleService lifeCycleService;

    public ScheduledUsagePointStateChangeHandlerFactory() {
    }

    @Inject
    public ScheduledUsagePointStateChangeHandlerFactory(TaskService taskService,
                                                        ServerUsagePointLifeCycleService lifeCycleService) {
        setTaskService(taskService);
        setLifeCycleService(lifeCycleService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return this.taskService.createMessageHandler(new ScheduledUsagePointStateChangeHandler(this.lifeCycleService, this.taskService));
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