/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl.event;

import com.elster.jupiter.issue.servicecall.MessageSeeds;
import com.elster.jupiter.issue.servicecall.ServiceCallIssue;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.google.inject.Inject;

import java.util.Map;
import java.util.Optional;

public class ServiceCallStateChangedEvent implements IssueEvent {

    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;
    private ServiceCall serviceCall;
    private DefaultState newState;

    @Inject
    public ServiceCallStateChangedEvent(ServiceCallService serviceCallService, Thesaurus thesaurus) {
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
    }

    public void init(Map<?, ?> map){
        Object id  = map.get("serviceCallId");
        serviceCall = Optional.ofNullable(id).map(Number.class::cast).map(Number::longValue).flatMap(serviceCallService::getServiceCall)
                .orElseThrow(() -> new UnableToCreateEventException(thesaurus, MessageSeeds.UNABLE_TO_CREATE_EVENT, map.toString()));
        newState = Optional.ofNullable(map.get("newState")).map(String.class::cast).map(DefaultState::valueOf)
                .orElseThrow(() -> new UnableToCreateEventException(thesaurus, MessageSeeds.UNABLE_TO_CREATE_EVENT, map.toString()));
    }

    @Override
    public String getEventType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<EndDevice> getEndDevice() {
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

    public long getServiceCallTypeId() {
        return serviceCall.getType().getId();
    }

    public long getStateId() {
        return newState.ordinal();
    }

    public DefaultState getNewState() {
        return newState;
    }

    @Override
    public Optional<? extends OpenIssue> findExistingIssue() {
        return Optional.empty();
    }

}
