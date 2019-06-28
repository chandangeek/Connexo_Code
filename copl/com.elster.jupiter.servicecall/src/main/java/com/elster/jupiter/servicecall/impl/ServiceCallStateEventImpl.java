/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;

import java.util.Optional;

class ServiceCallStateEventImpl implements IssueEvent {

    private ServiceCall serviceCall;
    private DefaultState newState;

    ServiceCallStateEventImpl(ServiceCall serviceCall, DefaultState newState) {
        this.serviceCall = serviceCall;
        this.newState = newState;
    }

    public ServiceCall getServiceCall() {
        return serviceCall;
    }

    public DefaultState getNewState() {
        return newState;
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
    public Optional<? extends OpenIssue> findExistingIssue() {
        return Optional.empty();
    }

    @Override
    public void apply(Issue issue) {
    }
}