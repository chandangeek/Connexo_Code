/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.task;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.energyict.mdc.sap.ConnectionStatusChangeMessageHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + ConnectionStatusChangeMessageHandlerFactory.TASK_SUBSCRIBER,
                "destination=" + ConnectionStatusChangeMessageHandlerFactory.DESTINATION},
        immediate = true)
public class ConnectionStatusChangeMessageHandlerFactory implements MessageHandlerFactory {

    public static final String DESTINATION = "SapConStatusChgTopic";
    public static final String TASK_SUBSCRIBER = "SapConStatusChgSubscriber";
    public static final String TASK_SUBSCRIBER_DISPLAYNAME = "Handle device disconnect/reconnect response";
    public static final String QUEUE_TABLE_SPEC_NAME = "MSG_RAWTOPICTABLE";

    private volatile JsonService jsonService;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile ServiceCallService serviceCallService;
    private volatile TransactionService transactionService;

    public ConnectionStatusChangeMessageHandlerFactory() {
        // for OSGI purposes
    }

    @Inject
    public ConnectionStatusChangeMessageHandlerFactory(JsonService jsonService,
                                                       SAPCustomPropertySets sapCustomPropertySets,
                                                       ServiceCallService serviceCallService,
                                                       TransactionService transactionService) {
        setJsonService(jsonService);
        setSAPCustomPropertySets(sapCustomPropertySets);
        setServiceCallService(serviceCallService);
        setTransactionService(transactionService);
    }

    @Reference
    public final void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new ConnectionStatusChangeHandler(jsonService, sapCustomPropertySets, serviceCallService, transactionService);
    }
}