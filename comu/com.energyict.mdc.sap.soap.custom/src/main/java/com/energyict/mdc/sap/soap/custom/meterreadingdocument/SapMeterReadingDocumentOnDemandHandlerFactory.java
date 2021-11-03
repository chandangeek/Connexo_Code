/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.meterreadingdocument;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingComTaskExecutionHelper;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;

@Component(name = "SapMeterReadingDocumentOnDemandHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + com.energyict.mdc.sap.soap.custom.meterreadingdocument.SapMeterReadingDocumentOnDemandHandlerFactory.SUBSCRIBER_NAME, "destination=" + EventService.JUPITER_EVENTS},
        immediate = true)
public class SapMeterReadingDocumentOnDemandHandlerFactory implements MessageHandlerFactory {
    public static final String SUBSCRIBER_NAME = "SapCustomMeterReadingHandler";
    public static final String SUBSCRIBER_DISPLAYNAME = "Handle events for SAP MeterReading web services";
    public static final String COMPONENT_NAME = "SCM";

    private volatile JsonService jsonService;
    private volatile ServiceCallService serviceCallService;
    private volatile SAPMeterReadingComTaskExecutionHelper sapMeterReadingComTaskExecutionHelper;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile UpgradeService upgradeService;
    private volatile MessageService messageService;

    // For OSGi purposes
    public SapMeterReadingDocumentOnDemandHandlerFactory(){
        super();
    }

    @Inject
    public void SapMeterReadingDocumentHandlerFactory(JsonService jsonService, ServiceCallService serviceCallService, SAPMeterReadingComTaskExecutionHelper sapMeterReadingComTaskExecutionHelper, CommunicationTaskService communicationTaskService, UpgradeService upgradeService, MessageService messageService) {
        setJsonService(jsonService);
        setServiceCallService(serviceCallService);
        setSapMeterReadingComTaskExecutionHelper(sapMeterReadingComTaskExecutionHelper);
        setCommunicationTaskService(communicationTaskService);
        setUpgradeService(upgradeService);
        setMessageService(messageService);

        activate();
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setSapMeterReadingComTaskExecutionHelper(SAPMeterReadingComTaskExecutionHelper sapMeterReadingComTaskExecutionHelper) {
        this.sapMeterReadingComTaskExecutionHelper = sapMeterReadingComTaskExecutionHelper;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new SAPMeterReadingDocumentOnDemandHandler(this.serviceCallService, this.sapMeterReadingComTaskExecutionHelper, this.jsonService, this.communicationTaskService);
    }

    @Activate
    public final void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).toInstance(messageService);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("MultiSense", COMPONENT_NAME),
                dataModel, Installer.class,
                Collections.emptyMap());
    }
}
