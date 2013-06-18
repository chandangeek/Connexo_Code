package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.time.Clock;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.concurrent.TimeUnit;

@Component(name = "com.elster.jupiter.parties", service = {TaskService.class, InstallService.class}, property = "name=" + Bus.COMPONENTNAME)
public class TaskServiceImpl implements TaskService, ServiceLocator {


    private volatile Clock clock;
    private volatile MessageService messageService;


    @Override
    public Clock getClock() {
        return clock;
    }

    @Override
    public RecurrentTaskBuilder newBuilder() {
        return new DefaultRecurrentTaskBuilder(new DefaultCronExpressionParser());
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public MessageService getMessageService() {
        return messageService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void activate() {
        Bus.setServiceLocator(this);
        new TaskScheduler(new TaskOccurrenceLauncher(new DueTaskFetcher()), 1, TimeUnit.MINUTES);
    }

    public void deactivate() {
        Bus.setServiceLocator(null);
    }
}
