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
        TransitionNotification transitionNotification = jsonService.deserialize(message.getPayload(), TransitionNotification.class);
        ServiceCall serviceCall = serviceCallService.getServiceCall(transitionNotification.getServiceCallId())
                .orElseThrow(IllegalStateException::new);
        serviceCall.getType().getServiceCallHandler()
                .onStateChange(serviceCall, transitionNotification.getOldState(), transitionNotification.getNewState());
    }

}
