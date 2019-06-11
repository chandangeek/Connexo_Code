/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.certrenewal;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.device.data.CertificateRenewalService;
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

@Component(name = "com.energyict.mdc.device.data.impl.pki.tasks.certrenewal.CertificateRenewalHandlerFactory",
        service = {CertificateRenewalService.class, MessageHandlerFactory.class},
        property = {"subscriber=" + CertificateRenewalHandlerFactory.CERTIFICATE_RENEWAL_TASK_SUBSCRIBER,
                "destination=" + CertificateRenewalHandlerFactory.CERTIFICATE_RENEWAL_TASK_DESTINATION_NAME},
        immediate = true)
public class CertificateRenewalHandlerFactory implements CertificateRenewalService, MessageHandlerFactory {
    private volatile TaskService taskService;
    private volatile OrmService ormService;
    private volatile BpmService bpmService;
    private volatile NlsService nlsService;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;

    // felix config properties
    private static final String CERTIFICATE_RENEWAL_PROCESS_DEFINITION_PROPERTY = "com.energyict.mdc.device.data.pki.certrenewal.bpmprocess";
    private static final String CERTIFICATE_DAYS_TILL_EXPIRATION_PROPERTY = "com.energyict.mdc.device.data.pki.certrenewal.expirationdays";

    public static final String CERTIFICATE_RENEWAL_TASK_SUBSCRIBER = "CertificateRenewalSubscriber";
    public static final String CERTIFICATE_RENEWAL_TASK_NAME = "Certificate Renewal Task";
    public static final String CERTIFICATE_RENEWAL_TASK_CRON_STRING = "0 0 8 1/1 * ? *"; // every day 8AM
    public static final String CERTIFICATE_RENEWAL_TASK_DESTINATION_NAME = "CertificateRenewal";
    public static final String CERTIFICATE_RENEWAL_DISPLAY_NAME = "Handle expired certificates";

    private String certRenewalBpmProcessDefinitionId;
    private Integer certRenewalExpitationDays;

    public CertificateRenewalHandlerFactory() {
    }

    @Inject
    public CertificateRenewalHandlerFactory(BundleContext bundleContext,
                                            TaskService taskService,
                                            OrmService ormService,
                                            BpmService bpmService,
                                            Clock clock,
                                            NlsService nlsService, EventService eventService) {
        this();
        setTaskService(taskService);
        setOrmService(ormService);
        setBpmService(bpmService);
        setEventService(eventService);
        setClock(clock);
        setNlsService(nlsService);
        activate(bundleContext);
    }

    public String getCertRenewalBpmProcessDefinitionId() {
        return certRenewalBpmProcessDefinitionId;
    }

    public Integer getCertRenewalExpitationDays() {
        return certRenewalExpitationDays;
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
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
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
        certRenewalBpmProcessDefinitionId = null;
        certRenewalExpitationDays = null;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new CertificateRenewalTaskExecutor(ormService, bpmService, eventService, clock, certRenewalBpmProcessDefinitionId,
                certRenewalExpitationDays));
    }

    @Override
    public RecurrentTask getTask() {
        return taskService.getRecurrentTask(CERTIFICATE_RENEWAL_TASK_NAME).get();
    }

    @Override
    public TaskOccurrence runNow() {
        return getTask().runNow(new CertificateRenewalTaskExecutor(ormService, bpmService, eventService, clock, certRenewalBpmProcessDefinitionId,
                certRenewalExpitationDays));
    }

    private void getTaskProperties(BundleContext bundleContext) {
        certRenewalBpmProcessDefinitionId = getTaskProperty(bundleContext, CERTIFICATE_RENEWAL_PROCESS_DEFINITION_PROPERTY)
                .orElseThrow(() -> new PropertyValueRequiredException(thesaurus, MessageSeeds.PROPERTY_VALUE_REQUIRED, CERTIFICATE_RENEWAL_PROCESS_DEFINITION_PROPERTY));
        String expirationDays = getTaskProperty(bundleContext, CERTIFICATE_DAYS_TILL_EXPIRATION_PROPERTY)
                .orElseThrow(() -> new PropertyValueRequiredException(thesaurus, MessageSeeds.PROPERTY_VALUE_REQUIRED, CERTIFICATE_DAYS_TILL_EXPIRATION_PROPERTY));
        certRenewalExpitationDays = Integer.parseInt(expirationDays);
    }

    private Optional<String> getTaskProperty(BundleContext context, String property) {
        return Optional.ofNullable(context.getProperty(property));
    }
}
