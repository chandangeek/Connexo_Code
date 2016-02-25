package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.json.JsonService;

public class ServiceCallMessageHandler implements MessageHandler {

    private final JsonService jsonService;
    private final IServiceCallService serviceCallService;

    ServiceCallMessageHandler(JsonService jsonService, IServiceCallService serviceCallService) {
        this.jsonService = jsonService;
        this.serviceCallService = serviceCallService;
    }

    @Override
    public void process(Message message) {
        TransitionRequest transitionRequest = jsonService.deserialize(message.getPayload(), TransitionRequest.class);
        ServiceCall serviceCall = serviceCallService.getServiceCall(transitionRequest.getServiceCallId())
                .orElseThrow(IllegalStateException::new);

    }

}
