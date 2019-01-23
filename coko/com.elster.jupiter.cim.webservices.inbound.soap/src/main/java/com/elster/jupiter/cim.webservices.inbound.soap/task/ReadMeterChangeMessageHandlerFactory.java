/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.task;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.cim.webservices.inbound.soap.ReadMeterChangeMessageHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + ReadMeterChangeMessageHandlerFactory.TASK_SUBSCRIBER,
                "destination=" + ReadMeterChangeMessageHandlerFactory.DESTINATION},
        immediate = true)
public class ReadMeterChangeMessageHandlerFactory implements MessageHandlerFactory {

    public static final String DESTINATION = "ReadMeterStatusChgTopic";
    public static final String TASK_SUBSCRIBER = "ReadMeterStatusChgSubscriber";
    public static final String TASK_SUBSCRIBER_DISPLAYNAME = "Handle read meters response";
    public static final String QUEUE_TABLE_SPEC_NAME = "MSG_RAWTOPICTABLE";

    private volatile JsonService jsonService;
    private volatile ServiceCallService serviceCallService;
    private volatile TransactionService transactionService;

    public ReadMeterChangeMessageHandlerFactory() {
        // for OSGI purposes
    }

    @Inject
    public ReadMeterChangeMessageHandlerFactory(JsonService jsonService, ServiceCallService serviceCallService,
                                                TransactionService transactionService) {
        setJsonService(jsonService);
        setServiceCallService(serviceCallService);
        setTransactionService(transactionService);
    }

    @Reference
    public final void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
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
        return new ReadMeterChangeHandler(jsonService, serviceCallService, transactionService);
    }
}