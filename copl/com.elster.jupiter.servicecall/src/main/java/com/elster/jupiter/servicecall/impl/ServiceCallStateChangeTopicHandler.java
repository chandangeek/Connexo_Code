package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.servicecall.topichandler", service = TopicHandler.class)
public class ServiceCallStateChangeTopicHandler implements TopicHandler {

    private volatile FiniteStateMachineService finiteStateMachineService;

    @Override
    public void handle(LocalEvent localEvent) {
        StateTransitionChangeEvent event = (StateTransitionChangeEvent) localEvent.getSource();
        ServiceCallImpl serviceCall = (ServiceCallImpl) event.getProperties().get(ServiceCall.class.getName());
        if (serviceCall != null) {
            handle(serviceCall, DefaultState.from(event.getOldState()).get(), DefaultState.from(event.getNewState())
                    .get());
        }
    }

    private void handle(ServiceCallImpl serviceCall, DefaultState oldState, DefaultState newState) {
        ServiceCallHandler serviceCallHandler = serviceCall.getType().getServiceCallHandler();

        if (serviceCallHandler.allowStateChange(serviceCall, oldState, newState)) {
            serviceCallHandler.onStateChange(serviceCall, oldState, newState);
            serviceCall.setState(newState);
            serviceCall.save();
        }
    }

    @Override
    public String getTopicMatcher() {
        return finiteStateMachineService.stateTransitionChangeEventTopic();
    }

    @Reference
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }
}
