/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.crlrequest;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskService;

import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.device.data.impl.pki.tasks.crlrequest.CrlRequestHandlerFactory",
        service = {MessageHandlerFactory.class},
        property = {"subscriber=" + CrlRequestHandlerFactory.CRL_REQUEST_TASK_SUBSCRIBER,
                "destination=" + CrlRequestHandlerFactory.CRL_REQUEST_TASK_DESTINATION_NAME},
        immediate = true)
public class CrlRequestHandlerFactory implements MessageHandlerFactory {
    private volatile TaskService taskService;
    private volatile CaService caService;
    private volatile CrlRequestTaskService crlRequestTaskService;

    public static final String CRL_REQUEST_TASK_SUBSCRIBER = "CrlRequestSubscriber";
    public static final String CRL_REQUEST_TASK_NAME = "Crl Request Task";
    public static final String CRL_REQUEST_TASK_DESTINATION_NAME = "CrlRequest";
    public static final String CRL_REQUEST_TASK_DISPLAY_NAME = "Handle CRLs";

    public static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    public CrlRequestHandlerFactory() {
    }

    @Inject
    public CrlRequestHandlerFactory(TaskService taskService,
                                    CaService caService,
                                    CrlRequestTaskService crlRequestTaskService) {
        this();
        setTaskService(taskService);
        setCaService(caService);
        setCrlRequestTaskService(crlRequestTaskService);
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setCaService(CaService caService) {
        this.caService = caService;
    }

    @Reference
    public void setCrlRequestTaskService(CrlRequestTaskService crlRequestTaskService) {
        this.crlRequestTaskService = crlRequestTaskService;
    }


    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new CrlRequestTaskExecutor(caService, crlRequestTaskService));
    }
}
