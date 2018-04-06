/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.crlrequest;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.CrlRequestService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskPropertiesService;

import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.Optional;

@Component(name = "com.energyict.mdc.device.data.impl.pki.tasks.crlrequest.CrlRequestHandlerFactory",
        service = {CrlRequestService.class, MessageHandlerFactory.class},
        property = {"subscriber=" + CrlRequestHandlerFactory.CRL_REQUEST_TASK_SUBSCRIBER,
                "destination=" + CrlRequestHandlerFactory.CRL_REQUEST_TASK_DESTINATION_NAME},
        immediate = true)
public class CrlRequestHandlerFactory implements MessageHandlerFactory, CrlRequestService {
    private volatile TaskService taskService;
    private volatile CaService caService;
    private volatile CrlRequestTaskPropertiesService crlRequestTaskPropertiesService;
    private volatile SecurityManagementService securityManagementService;
    private volatile DeviceService deviceService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile TransactionService transactionService;

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
                                    CrlRequestTaskPropertiesService crlRequestTaskPropertiesService,
                                    SecurityManagementService securityManagementService,
                                    DeviceService deviceService,
                                    Clock clock,
                                    NlsService nlsService,
                                    TransactionService transactionService) {
        this();
        setTaskService(taskService);
        setCaService(caService);
        setCrlRequestTaskPropertiesService(crlRequestTaskPropertiesService);
        setSecurityManagementService(securityManagementService);
        setDeviceService(deviceService);
        setClock(clock);
        setNlsService(nlsService);
        setTransactionService(transactionService);
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
    public void setCrlRequestTaskPropertiesService(CrlRequestTaskPropertiesService crlRequestTaskPropertiesService) {
        this.crlRequestTaskPropertiesService = crlRequestTaskPropertiesService;
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Optional<RecurrentTask> getTask() {
        return taskService.getRecurrentTask(CRL_REQUEST_TASK_NAME);
    }

    @Override
    public TaskOccurrence runNow() {
        if (getTask().isPresent()) {
            return getTask().get().runNow(new CrlRequestTaskExecutor(caService, crlRequestTaskPropertiesService, securityManagementService, deviceService, clock, thesaurus, transactionService));
        }
        return null;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new CrlRequestTaskExecutor(caService, crlRequestTaskPropertiesService, securityManagementService, deviceService, clock, thesaurus, transactionService));
    }
}
