/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.issue.servicecall.ServiceCallIssueService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.servicecall.issue.topichandler", service = TopicHandler.class)
public class ServiceCallMessageHandler implements TopicHandler {

    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile ServiceCallIssueService serviceCallIssueService;

    public ServiceCallMessageHandler() {
    }

    @Inject
    ServiceCallMessageHandler(FiniteStateMachineService finiteStateMachineService, ServiceCallIssueService serviceCallIssueService) {
        this.finiteStateMachineService = finiteStateMachineService;
        this.serviceCallIssueService = serviceCallIssueService;
    }

    @Reference
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Reference
    public void setServiceCallIssueService(ServiceCallIssueService serviceCallIssueService) {
        this.serviceCallIssueService = serviceCallIssueService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        StateTransitionChangeEvent event = (StateTransitionChangeEvent) localEvent.getSource();
        ServiceCall serviceCall = (ServiceCall) event.getProperties().get(ServiceCall.class.getName());
        if (serviceCall != null) {
            serviceCallIssueService.createIssue(serviceCall, DefaultState.from(event.getNewState()).get());
        }
    }


    @Override
    public String getTopicMatcher() {
        return finiteStateMachineService.stateTransitionChangeEventTopic();
    }
}
