/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.keyrenewal;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.pki.SymmetricKeyWrapperDAO;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.device.data.KeyRenewalService;
import com.energyict.mdc.device.data.SecurityAccessorDAO;
import com.energyict.mdc.device.data.impl.pki.tasks.command.CommandExecutorFactory;

import com.google.inject.Inject;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.Optional;
import java.util.logging.Logger;

@Component(name = "com.energyict.mdc.device.data.impl.pki.tasks.keyrenewal.KeyRenewalHandlerFactory",
        service = {KeyRenewalService.class, MessageHandlerFactory.class},
        property = {"subscriber=" + KeyRenewalHandlerFactory.KEY_RENEWAL_TASK_SUBSCRIBER,
                "destination=" + KeyRenewalHandlerFactory.KEY_RENEWAL_TASK_DESTINATION_NAME},
        immediate = true)
public class KeyRenewalHandlerFactory implements KeyRenewalService, MessageHandlerFactory {

    private volatile EventService eventService;
    private volatile TaskService taskService;
    private volatile BpmService bpmService;
    private volatile Clock clock;
    private volatile SymmetricKeyWrapperDAO symemtricKeyWrapperDAO;
    private volatile SecurityAccessorDAO securityAccessorDAO;


    private static final String KEY_RENEWAL_PROCESS_DEFINITION_PROPERTY = "com.energyict.mdc.device.data.pki.keyrenewal.bpmprocess";
    private static final String KEY_DAYS_TILL_EXPIRATION_PROPERTY = "com.energyict.mdc.device.data.pki.keyrenewal.expirationdays";
    public static final String KEY_RENEWAL_TASK_SUBSCRIBER = "KeyRenewalSubscriber";

    public static final String KEY_RENEWAL_TASK_NAME = "Key Renewal Task";
    public static final String KEY_RENEWAL_TASK_CRON_STRING = "0 0 11 1/1 * ? *"; // every day 11AM
    public static final String KEY_RENEWAL_TASK_DESTINATION_NAME = "KeyRenewal";
    public static final String KEY_RENEWAL_DISPLAY_NAME = "Handle expired keys";
    private String keyRenewalBpmProcessDefinitionId;

    private Integer keyRenewalExpitationDays;

    public KeyRenewalHandlerFactory() {
    }

    @Inject
    public KeyRenewalHandlerFactory(BundleContext bundleContext,
                                    TaskService taskService,
                                    BpmService bpmService) {
        this();
        setTaskService(taskService);
        setBpmService(bpmService);
        activate(bundleContext);
    }


    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setSymemtricKeyWrapperDAO(SymmetricKeyWrapperDAO symemtricKeyWrapperDAO) {
        this.symemtricKeyWrapperDAO = symemtricKeyWrapperDAO;
    }

    @Reference
    public void setSecurityAccessorDAO(SecurityAccessorDAO securityAccessorDAO) {
        this.securityAccessorDAO = securityAccessorDAO;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }


    @Activate
    public void activate(BundleContext bundleContext) {
        getTaskProperties(bundleContext);
    }

    @Deactivate
    public void deactivate() {
        keyRenewalBpmProcessDefinitionId = null;
        keyRenewalExpitationDays = null;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new KeyRenewalTaskExecutor(eventService, bpmService, clock, symemtricKeyWrapperDAO, securityAccessorDAO, new CommandExecutorFactory(), keyRenewalBpmProcessDefinitionId, keyRenewalExpitationDays, Logger
                .getAnonymousLogger()));
    }

    @Override
    public RecurrentTask getTask() {
        return taskService.getRecurrentTask(KEY_RENEWAL_TASK_NAME).get();
    }

    @Override
    public TaskOccurrence runNow() {
        return getTask().runNow(new KeyRenewalTaskExecutor(eventService, bpmService, clock, symemtricKeyWrapperDAO, securityAccessorDAO, new CommandExecutorFactory(), keyRenewalBpmProcessDefinitionId, keyRenewalExpitationDays, Logger
                .getAnonymousLogger()));
    }

    private void getTaskProperties(BundleContext bundleContext) {
        getTaskProperty(bundleContext, KEY_RENEWAL_PROCESS_DEFINITION_PROPERTY)
                .ifPresent(p -> keyRenewalBpmProcessDefinitionId = p);
        getTaskProperty(bundleContext, KEY_DAYS_TILL_EXPIRATION_PROPERTY)
                .map(p -> Integer.parseInt(p))
                .ifPresent(expirationDays -> keyRenewalExpitationDays = expirationDays);
    }

    private Optional<String> getTaskProperty(BundleContext context, String property) {
        return Optional.ofNullable(context.getProperty(property)).filter(p -> p.trim().length() > 0);
    }

}
