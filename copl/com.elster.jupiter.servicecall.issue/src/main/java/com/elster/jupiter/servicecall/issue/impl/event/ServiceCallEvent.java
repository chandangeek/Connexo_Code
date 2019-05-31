/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.issue.IssueServiceCallService;
import com.elster.jupiter.servicecall.issue.ServiceCallIssueFilter;

import com.google.inject.Inject;

import java.util.Map;
import java.util.Optional;

public abstract class ServiceCallEvent implements IssueEvent {

    protected Long channelId;
    protected String readingType;
    private final Thesaurus thesaurus;

    private final IssueServiceCallService issueServiceCallService;
    private final IssueService issueService;

    @Inject
    public ServiceCallEvent(Thesaurus thesaurus, IssueServiceCallService issueServiceCallService, IssueService issueService) {
        this.thesaurus = thesaurus;
        this.issueServiceCallService = issueServiceCallService;
        this.issueService = issueService;
    }

    abstract void init(Map<?, ?> jsonPayload);

    @Override
    public String getEventType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<com.elster.jupiter.metering.EndDevice> getEndDevice() {
        return Optional.empty();
    }

    @Override
    public void apply(Issue issue) {

    }

    @Override
    public Optional<? extends OpenIssue> findExistingIssue() {
        ServiceCallIssueFilter filter = new ServiceCallIssueFilter();
//        getEndDevice().ifPresent(filter::setDevice);
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        filter.addStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
       // Optional<? extends IssueServiceCall> foundIssue = IssueServiceCallService.findAllDataValidationIssues(filter).find().stream().findFirst();//It is going to be only zero or one open issue per device
//        if (foundIssue.isPresent()) {
//            return Optional.of((OpenIssue)foundIssue.get());
//        }
        return Optional.empty();
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }
}
