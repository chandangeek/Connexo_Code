/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl.eventhandler;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;

import java.time.Clock;

public class ComTaskExecutionEventHandlerFactory implements MessageHandlerFactory {
    public static final String SUBSCRIBER_NAME = "ComTaskExecutionEventHandler";
    public static final String SUBSCRIBER_DISPLAYNAME = "Handle events for cim web services";

    private volatile JsonService jsonService;
    private volatile ServiceCallService serviceCallService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile Clock clock;


    public ComTaskExecutionEventHandlerFactory(JsonService jsonService, ServiceCallService serviceCallService, CommunicationTaskService communicationTaskService, Clock clock) {
        this.jsonService = jsonService;
        this.clock = clock;
        this.serviceCallService = serviceCallService;
        this.communicationTaskService = communicationTaskService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new ComTaskExecutionEventHandler(this.clock, this.serviceCallService, this.jsonService, this.communicationTaskService);
    }

}
