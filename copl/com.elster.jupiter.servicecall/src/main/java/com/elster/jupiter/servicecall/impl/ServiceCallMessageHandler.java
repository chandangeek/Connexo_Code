package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.NoSuchServiceCallException;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.json.JsonService;

public class ServiceCallMessageHandler implements MessageHandler {

    private volatile JsonService jsonService;
    private volatile IServiceCallService serviceCallService;
    private volatile Thesaurus thesaurus;

    ServiceCallMessageHandler(JsonService jsonService, IServiceCallService serviceCallService, Thesaurus thesaurus) {
        this.jsonService = jsonService;
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void process(Message message) {
        TransitionNotification transitionNotification = jsonService.deserialize(message.getPayload(), TransitionNotification.class);
        ServiceCall serviceCall = serviceCallService.getServiceCall(transitionNotification.getServiceCallId())
                .orElseThrow(()->new NoSuchServiceCallException(thesaurus, MessageSeeds.NO_SUCH_SERVICE_CALL, transitionNotification.getServiceCallId()));
        serviceCall.getType().getServiceCallHandler()
                .onStateChange(serviceCall, transitionNotification.getOldState(), transitionNotification.getNewState());

        serviceCall.getParent()
                .ifPresent(parent -> parent.getType().getServiceCallHandler()
                        .onChildStateChange(serviceCall, transitionNotification.getOldState(), transitionNotification.getNewState()));
    }

}
