/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.elster.jupiter.issue.impl.ManualIssueProvider",
        service = {IssueProvider.class},
        property = "name=" + ManualIssueProvider.NAME,
        immediate = true)
public class ManualIssueProvider implements IssueProvider {
    static final String NAME = "ManualIssueProvider";

    private volatile IssueService issueService;

    public ManualIssueProvider() {
        // For OSGI purpose
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    public Optional<? extends OpenIssue> getOpenIssue(OpenIssue issue) {
        if (!issue.getStatus().isHistorical()) {
            return (issue.getType() != null && IssueService.MANUAL_ISSUE_TYPE.equals(issue.getType().getKey())) ? Optional.of(issue) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue) {
        if (issue.getStatus().isHistorical()) {
            return (issue.getType() != null && IssueService.MANUAL_ISSUE_TYPE.equals(issue.getType().getKey())) ? Optional.of(issue) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<? extends Issue> findIssue(long id) {
        Optional<? extends Issue> issue = issueService.findIssue(id);
        return (issue.isPresent() && issue.get().getType() != null && IssueService.MANUAL_ISSUE_TYPE.equals(issue.get().getType().getKey())) ? issue : Optional.empty();
    }

    @Override
    public Set<String> getIssueTypeIdentifiers() {
        return Collections.singleton(IssueService.MANUAL_ISSUE_TYPE);
    }
}
