/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.tasks;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger;


public class IssueSnoozeHandler implements TaskExecutor {
    private static final Logger LOG = Logger.getLogger(IssueSnoozeHandler.class.getName());

    private final IssueService issueService;
    private final Thesaurus thesaurus;
    private final IssueActionService issueActionService;
    private volatile Clock clock;

    public IssueSnoozeHandler(IssueService issueService, Thesaurus thesaurus, IssueActionService issueActionService, Clock clock) {
        this.issueService = issueService;
        this.thesaurus = thesaurus;
        this.issueActionService = issueActionService;
        this.clock = clock;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        toggleSnoozed();
    }

    private void toggleSnoozed() {
        Optional<IssueStatus> snoozedIssueStaus = issueService.findStatus(IssueStatus.SNOOZED);
        IssueFilter issueFilter = issueService.newIssueFilter();
        if (snoozedIssueStaus.isPresent()) {
            issueFilter.addStatus(snoozedIssueStaus.get());
            issueService.findIssues(issueFilter).find()
                    .forEach(this::handleExpired);
            issueService.findAlarms(issueFilter).find()
                    .forEach(this::handleExpired);
        }
    }

    @Override
    public void postFailEvent(EventService eventService, TaskOccurrence occurrence, String cause){
        throw new UnsupportedOperationException("Unsupported operation");
    }

    private void handleExpired(Issue issue) {
        if (issue.getSnoozeDateTime().isPresent() &&
                issue.getSnoozeDateTime().get().isBefore(Instant.now(clock))) {
            issue.clearSnooze();
            issue.update();
            MessageSeeds.ISSUE_SNOOZE_PERIOD_EXPIRED.log(LOG, thesaurus, issue.getTitle());
        }
    }
}
