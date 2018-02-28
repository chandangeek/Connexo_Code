/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.keyrenewal;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.device.data.KeyRenewalService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.pki.PropertyValueRequiredException;

import com.google.inject.Inject;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.Optional;

@Component(name = "com.energyict.mdc.device.data.impl.pki.tasks.keyrenewal.KeyRenewalHandlerFactory",
        service = {KeyRenewalService.class, MessageHandlerFactory.class},
        property = {"subscriber=" + KeyRenewalHandlerFactory.KEY_RENEWAL_TASK_SUBSCRIBER,
                "destination=" + KeyRenewalHandlerFactory.KEY_RENEWAL_TASK_DESTINATION_NAME},
        immediate = true)
public class KeyRenewalHandlerFactory implements KeyRenewalService, MessageHandlerFactory {
    private volatile TaskService taskService;
    private volatile OrmService ormService;
    private volatile BpmService bpmService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;

    // felix config properties
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
                                    OrmService ormService,
                                    BpmService bpmService,
                                    Clock clock,
                                    NlsService nlsService) {
        this();
        setTaskService(taskService);
        setOrmService(ormService);
        setBpmService(bpmService);
        setClock(clock);
        setNlsService(nlsService);
        activate(bundleContext);
    }

    public String getKeyRenewalBpmProcessDefinitionId() {
        return keyRenewalBpmProcessDefinitionId;
    }

    public Integer getKeyRenewalExpitationDays() {
        return keyRenewalExpitationDays;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        // depends on DDC data model
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
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
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
        return taskService.createMessageHandler(new KeyRenewalTaskExecutor(ormService, bpmService, clock, keyRenewalBpmProcessDefinitionId,
                keyRenewalExpitationDays));
    }

    @Override
    public RecurrentTask getTask() {
        return taskService.getRecurrentTask(KEY_RENEWAL_TASK_NAME).get();
    }

    @Override
    public TaskOccurrence runNow() {
        return getTask().runNow(new KeyRenewalTaskExecutor(ormService, bpmService, clock, keyRenewalBpmProcessDefinitionId,
                keyRenewalExpitationDays));
    }

    private void getTaskProperties(BundleContext bundleContext) {
        keyRenewalBpmProcessDefinitionId = getTaskProperty(bundleContext, KEY_RENEWAL_PROCESS_DEFINITION_PROPERTY)
                .orElseThrow(() -> new PropertyValueRequiredException(thesaurus, MessageSeeds.PROPERTY_VALUE_REQUIRED, KEY_RENEWAL_PROCESS_DEFINITION_PROPERTY));
        String expirationDays = getTaskProperty(bundleContext, KEY_DAYS_TILL_EXPIRATION_PROPERTY)
                .orElseThrow(() -> new PropertyValueRequiredException(thesaurus, MessageSeeds.PROPERTY_VALUE_REQUIRED, KEY_DAYS_TILL_EXPIRATION_PROPERTY));
        keyRenewalExpitationDays = Integer.parseInt(expirationDays);
    }

    private Optional<String> getTaskProperty(BundleContext context, String property) {
        return Optional.ofNullable(context.getProperty(property));
    }

}
