/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.issue.ServiceCallIssue;

import com.google.inject.Inject;

import java.util.Map;
import java.util.Optional;

public class ServiceCallStateChangedEvent implements IssueEvent {

    private final ServiceCall serviceCall;
    private final DefaultState newState;

    @Inject
    public ServiceCallStateChangedEvent(ServiceCall serviceCall, DefaultState newState) {
        this.serviceCall = serviceCall;
        this.newState = newState;
    }

    public void init(Map<?, ?> map){
        map.entrySet();
    }

    @Override
    public String getEventType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional getEndDevice() {
        return Optional.empty();
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof ServiceCallIssue) {
            ServiceCallIssue issueServiceCall = (ServiceCallIssue) issue;
            issueServiceCall.setNewState(newState);
            issueServiceCall.setServiceCall(serviceCall);
        }
    }

    public ServiceCall getServiceCall() {
        return serviceCall;
    }

    public DefaultState getNewState() {
        return newState;
    }

    @Override
    public Optional<? extends OpenIssue> findExistingIssue() {
        return Optional.empty();
    }

}
