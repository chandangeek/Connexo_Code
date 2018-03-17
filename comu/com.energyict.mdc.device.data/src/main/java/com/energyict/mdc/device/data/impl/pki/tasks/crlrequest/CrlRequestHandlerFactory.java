/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.crlrequest;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.device.data.CrlRequestService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskService;

import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

@Component(name = "com.energyict.mdc.device.data.impl.pki.tasks.crlrequest.CrlRequestHandlerFactory",
        service = {CrlRequestService.class, MessageHandlerFactory.class},
        property = {"subscriber=" + CrlRequestHandlerFactory.CRL_REQUEST_TASK_SUBSCRIBER,
                "destination=" + CrlRequestHandlerFactory.CRL_REQUEST_TASK_DESTINATION_NAME},
        immediate = true)
public class CrlRequestHandlerFactory implements MessageHandlerFactory, CrlRequestService {
    private volatile TaskService taskService;
    private volatile CaService caService;
    private volatile CrlRequestTaskService crlRequestTaskService;
    private volatile SecurityManagementService securityManagementService;
    private volatile DeviceService deviceService;

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
                                    CrlRequestTaskService crlRequestTaskService,
                                    SecurityManagementService securityManagementService,
                                    DeviceService deviceService) {
        this();
        setTaskService(taskService);
        setCaService(caService);
        setCrlRequestTaskService(crlRequestTaskService);
        setSecurityManagementService(securityManagementService);
        setDeviceService(deviceService);
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

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public Optional<RecurrentTask> getTask() {
        return taskService.getRecurrentTask(CRL_REQUEST_TASK_NAME);
    }

    @Override
    public TaskOccurrence runNow() {
        if (getTask().isPresent()) {
            return getTask().get().runNow(new CrlRequestTaskExecutor(caService, crlRequestTaskService, securityManagementService, deviceService));
        }
        return null;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new CrlRequestTaskExecutor(caService, crlRequestTaskService, securityManagementService, deviceService));
    }
}
