/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.tasks;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Condition;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;


public class IssueOverdueHandler implements TaskExecutor{
    private static final Logger LOG = Logger.getLogger(IssueOverdueHandler.class.getName());

    private final IssueService issueService;
    private final Thesaurus thesaurus;
    private final IssueActionService issueActionService;

    public IssueOverdueHandler(IssueService issueService, Thesaurus thesaurus, IssueActionService issueActionService) {
        this.issueService = issueService;
        this.thesaurus = thesaurus;
        this.issueActionService = issueActionService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        Condition overdueCondition = where("overdue").isEqualTo(false).and(where("dueDate").isLessThan(System.currentTimeMillis()));
        Query<OpenIssue> overdueIssuesQuery = issueService.query(OpenIssue.class);
        List<OpenIssue> overdueIssues = overdueIssuesQuery.select(overdueCondition).stream().map(this::mapBaseIssue).collect(Collectors.toList());
        for (OpenIssue issue : overdueIssues) {
            markOverdue(issue);
            doOverdueActions(issue);
            MessageSeeds.ISSUE_OVERDUE_NOTIFICATION.log(LOG, thesaurus, issue.getTitle());
        }
    }
    
    private OpenIssue mapBaseIssue(OpenIssue baseIssue) {
        for (IssueProvider provider : issueService.getIssueProviders()) {
            Optional<? extends OpenIssue> openIssue = provider.getOpenIssue(baseIssue);
            if (openIssue.isPresent()) {
                return openIssue.get();
            }
        }
        return baseIssue;
    }

    private void markOverdue(OpenIssue issue){
        if (issue != null) {
            issue.setOverdue(true);
            issue.update();
        }
    }

    private void doOverdueActions(OpenIssue issue) {
        new IssueActionExecutor(issue, CreationRuleActionPhase.OVERDUE, thesaurus, issueActionService).run();
    }
}
