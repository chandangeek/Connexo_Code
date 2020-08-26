/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl.event;

import com.elster.jupiter.issue.servicecall.OpenServiceCallIssue;
import com.elster.jupiter.issue.servicecall.ServiceCallIssueFilter;
import com.elster.jupiter.issue.servicecall.ServiceCallIssueService;
import com.elster.jupiter.issue.servicecall.impl.MessageSeeds;
import com.elster.jupiter.issue.servicecall.impl.entity.ServiceCallIssueImpl;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateIssueException;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.google.inject.Inject;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ServiceCallStateChangedEvent implements IssueEvent {
    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;
    private final IssueService issueService;
    private final ServiceCallIssueService serviceCallIssueService;

    private ServiceCall serviceCall;
    private DefaultState newState;
    private int ruleId;

    @Inject
    public ServiceCallStateChangedEvent(ServiceCallService serviceCallService,
                                        Thesaurus thesaurus,
                                        IssueService issueService,
                                        ServiceCallIssueService serviceCallIssueService) {
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
        this.issueService = issueService;
        this.serviceCallIssueService = serviceCallIssueService;
    }

    public void init(Map<?, ?> map){
        Object id  = map.get("serviceCallId");
        serviceCall = Optional.ofNullable(id).map(Number.class::cast).map(Number::longValue).flatMap(serviceCallService::getServiceCall)
                .orElseThrow(() -> new UnableToCreateIssueException(thesaurus, MessageSeeds.UNABLE_TO_CREATE_EVENT, map.toString()));
        newState = Optional.ofNullable(map.get("newState")).map(String.class::cast).map(DefaultState::valueOf)
                .orElseThrow(() -> new UnableToCreateIssueException(thesaurus, MessageSeeds.UNABLE_TO_CREATE_EVENT, map.toString()));
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
        if (issue instanceof ServiceCallIssueImpl) {
            ServiceCallIssueImpl issueServiceCall = (ServiceCallIssueImpl) issue;
            issueServiceCall.setNewState(newState);
            issueServiceCall.setServiceCall(serviceCall);
        }
    }

    public ServiceCall getServiceCall() {
        return serviceCall;
    }

    /**
     * used by issue creation rule
     */
    public long getServiceCallTypeId() {
        return serviceCall.getType().getId();
    }

    /**
     * used by issue creation rule
     */
    public long getStateId() {
        return newState.ordinal();
    }

    /**
     * used by issue creation rule
     */
    public void setCreationRule(int ruleId) {
        this.ruleId = ruleId;
    }

    public DefaultState getNewState() {
        return newState;
    }

    @Override
    public Optional<OpenServiceCallIssue> findExistingIssue() {
        ServiceCallIssueFilter filter = new ServiceCallIssueFilter();
        Optional<CreationRule> rule = issueService.getIssueCreationService().findCreationRuleById(ruleId);
        if (rule.isPresent()){
            filter.addRule(rule.get());
            Stream.of(IssueStatus.OPEN, IssueStatus.IN_PROGRESS, IssueStatus.SNOOZED)
                    .map(issueService::findStatus)
                    .map(Optional::get)
                    .forEach(filter::addStatus);
            filter.addServiceCall(serviceCall);
            return serviceCallIssueService.findIssues(filter).paged(0, 0).stream()
                    .findAny()
                    .map(OpenServiceCallIssue.class::cast);
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "ServiceCallStateChangedEvent{" +
                "serviceCall=" + serviceCall +
                ", newState=" + newState +
                ", ruleId=" + ruleId +
                '}';
    }
}
