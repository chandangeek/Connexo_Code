/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.certrenewal;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.CertificateDAO;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.device.data.CertificateRenewalService;
import com.energyict.mdc.device.data.SecurityAccessorDAO;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
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

@Component(name = "com.energyict.mdc.device.data.impl.pki.tasks.certrenewal.CertificateRenewalHandlerFactory",
        service = {CertificateRenewalService.class, MessageHandlerFactory.class},
        property = {"subscriber=" + CertificateRenewalHandlerFactory.CERTIFICATE_RENEWAL_TASK_SUBSCRIBER,
                "destination=" + CertificateRenewalHandlerFactory.CERTIFICATE_RENEWAL_TASK_DESTINATION_NAME},
        immediate = true)
public class CertificateRenewalHandlerFactory implements CertificateRenewalService, MessageHandlerFactory {
    private volatile EventService eventService;
    private volatile TaskService taskService;
    private volatile OrmService ormService;
    private volatile CertificateDAO certificateDAO;
    private volatile SecurityAccessorDAO securityAccessorDAO;
    private volatile BpmService bpmService;
    private volatile Clock clock;

    // felix config properties
    private static final String CERTIFICATE_RENEWAL_PROCESS_DEFINITION_PROPERTY = "com.energyict.mdc.device.data.pki.certrenewal.bpmprocess";
    private static final String CERTIFICATE_DAYS_TILL_EXPIRATION_PROPERTY = "com.energyict.mdc.device.data.pki.certrenewal.expirationdays";

    public static final String CERTIFICATE_RENEWAL_PROCESS_NAME = "Certificate renewal";
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
                                            BpmService bpmService) {
        this();
        setTaskService(taskService);
        setOrmService(ormService);
        setBpmService(bpmService);
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
    public void setCertificateDAO(CertificateDAO certificateDAO) {
        this.certificateDAO = certificateDAO;
    }

    @Reference
    public void setSecurityAccessorDAO(SecurityAccessorDAO securityAccessorDAO) {
        this.securityAccessorDAO = securityAccessorDAO;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }


    @Activate
    public void activate(BundleContext bundleContext) {
        getTaskProperties(bundleContext);
        bpmService.addSingletonInstanceProcess(Optional.ofNullable(certRenewalBpmProcessDefinitionId).orElse(CERTIFICATE_RENEWAL_PROCESS_NAME), "deviceId");
    }

    @Deactivate
    public void deactivate() {
        certRenewalBpmProcessDefinitionId = null;
        certRenewalExpitationDays = null;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new CertificateRenewalTaskExecutor(certificateDAO, securityAccessorDAO, new CommandExecutorFactory(), clock, eventService, bpmService, certRenewalBpmProcessDefinitionId,
                certRenewalExpitationDays, Logger.getAnonymousLogger()));
    }

    @Override
    public RecurrentTask getTask() {
        return taskService.getRecurrentTask(CERTIFICATE_RENEWAL_TASK_NAME).get();
    }

    @Override
    public TaskOccurrence runNow() {
        return getTask().runNow(new CertificateRenewalTaskExecutor(certificateDAO, securityAccessorDAO, new CommandExecutorFactory(), clock, eventService, bpmService, certRenewalBpmProcessDefinitionId,
                certRenewalExpitationDays, Logger.getAnonymousLogger()));
    }

    private void getTaskProperties(BundleContext bundleContext) {
        getTaskProperty(bundleContext, CERTIFICATE_RENEWAL_PROCESS_DEFINITION_PROPERTY)
                .ifPresent(p -> certRenewalBpmProcessDefinitionId = p);
        getTaskProperty(bundleContext, CERTIFICATE_DAYS_TILL_EXPIRATION_PROPERTY)
                .map(p -> Integer.parseInt(p))
                .ifPresent(expirationDays -> certRenewalExpitationDays = expirationDays);
    }

    private Optional<String> getTaskProperty(BundleContext context, String property) {
        return Optional.ofNullable(context.getProperty(property)).filter(p -> p.trim().length() > 0);
    }
}
