/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.servicecall.topichandler", service = TopicHandler.class)
public class ServiceCallStateChangeTopicHandler implements TopicHandler {

    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile IServiceCallService serviceCallService;
    private volatile JsonService jsonService;

    public ServiceCallStateChangeTopicHandler() {
    }

    @Inject
    ServiceCallStateChangeTopicHandler(FiniteStateMachineService finiteStateMachineService, IServiceCallService serviceCallService, JsonService jsonService) {
        this.finiteStateMachineService = finiteStateMachineService;
        this.serviceCallService = serviceCallService;
        this.jsonService = jsonService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        StateTransitionChangeEvent event = (StateTransitionChangeEvent) localEvent.getSource();
        ServiceCallImpl serviceCall = (ServiceCallImpl) event.getProperties().get(ServiceCall.class.getName());
        if (serviceCall != null) {
            handle(
                serviceCall,
                DefaultState.from(event.getOldState()).get(),
                DefaultState.from(event.getNewState()).get());
        }
    }

    private void handle(ServiceCallImpl serviceCall, DefaultState oldState, DefaultState newState) {
        ServiceCallHandler serviceCallHandler = serviceCall.getType().getServiceCallHandler();

        if (serviceCallHandler.allowStateChange(serviceCall, oldState, newState)) {
            doStateChange(serviceCall, oldState, newState);
        } else {
            serviceCall.log(LogLevel.WARNING, "Handler rejected the transition from " + oldState.getDefaultFormat() + " to " + newState.getDefaultFormat());
        }
    }

    private void doStateChange(ServiceCallImpl serviceCall, DefaultState currentState, DefaultState newState) {
        serviceCall.setState(newState);
        serviceCall.save();

        TransitionNotification transitionNotification = new TransitionNotification(serviceCall, currentState, newState);

        DestinationSpec serviceCallQueue = serviceCallService.getServiceCallQueue();

        if (DefaultState.CANCELLED.equals(newState)) {
            serviceCallQueue.purgeCorrelationId(serviceCall.getNumber());
            serviceCall.findChildren().stream().filter(sc -> sc.canTransitionTo(DefaultState.CANCELLED)).forEach(ServiceCall::cancel);
        }

        serviceCallQueue.message(jsonService.serialize(transitionNotification))
                .withCorrelationId(serviceCall.getNumber())
                .send();
    }

    @Override
    public String getTopicMatcher() {
        return finiteStateMachineService.stateTransitionChangeEventTopic();
    }

    @Reference
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = (IServiceCallService) serviceCallService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }
}
