/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.autoreschedule.impl;

import com.elster.jupiter.customtask.CustomTaskService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;

import com.energyict.mdc.device.data.tasks.CommunicationTaskService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;

@Component(name = "com.energyict.mdc.autoreschedule.impl.AutoRescheduleTaskMessageHandlerFactory",
        property = {"subscriber=" + AutoRescheduleTaskFactory.SUBSCRIBER_NAME, "destination=" + AutoRescheduleTaskFactory.DESTINATION_NAME},
        service = MessageHandlerFactory.class,
        immediate = true)
public class AutoRescheduleTaskMessageHandlerFactory implements MessageHandlerFactory {

    private volatile CustomTaskService customTaskService;
    private volatile TaskService taskService;
    private volatile TransactionService transactionService;
    private volatile EventService eventService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new AutoRescheduleTaskExecutor(customTaskService, eventService, transactionService, thesaurus, meteringGroupsService, communicationTaskService, clock));
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setCustomTaskService(CustomTaskService customTaskService) {
        this.customTaskService = (CustomTaskService) customTaskService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
/*@Reference
    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }*/

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }
}
