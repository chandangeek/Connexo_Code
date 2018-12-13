/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.demo.customtask.impl;

import com.elster.jupiter.customtask.CustomTaskService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;

@Component(name = "com.elster.jupiter.mdm.demo.customtask.impl.demo",
        property = {"subscriber=" + DemoCustomTaskFactory.SUBSCRIBER_NAME, "destination="+DemoCustomTaskFactory.DESTINATION_NAME},
        service = MessageHandlerFactory.class,
        immediate = true)
public class DemoCustomTaskMessageHandlerFactory implements MessageHandlerFactory {

    private volatile CustomTaskService customTaskService;
    private volatile TaskService taskService;
    private volatile TransactionService transactionService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
   //private volatile ThreadPrincipalService threadPrincipalService;

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new DemoCustomTaskExecutor(customTaskService, transactionService, thesaurus, meteringGroupsService, clock/*, threadPrincipalService*/));
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN);
    }
    @Reference
    public void setCustomTaskService(CustomTaskService customTaskService) {
        this.customTaskService = (CustomTaskService) customTaskService;
    }

   /* @Reference
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

    /*@Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }*/

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }
}
