/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.issue.IssueServiceCall;
import com.elster.jupiter.servicecall.issue.ServiceCallIssueFilter;

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
        if (issue instanceof IssueServiceCall) {
            IssueServiceCall issueServiceCall = (IssueServiceCall) issue;
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
        ServiceCallIssueFilter filter = new ServiceCallIssueFilter();
//        getEndDevice().ifPresent(filter::setDevice);
//        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
//        filter.addStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
       // Optional<? extends IssueServiceCall> foundIssue = IssueServiceCallService.findAllDataValidationIssues(filter).find().stream().findFirst();//It is going to be only zero or one open issue per device
//        if (foundIssue.isPresent()) {
//            return Optional.of((OpenIssue)foundIssue.get());
//        }
        return Optional.empty();
    }
}
