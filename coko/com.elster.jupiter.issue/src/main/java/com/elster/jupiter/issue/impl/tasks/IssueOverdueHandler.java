package com.elster.jupiter.issue.impl.tasks;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.actions.IssueActionExecutor;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Condition;

import java.util.List;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;


public class IssueOverdueHandler implements TaskExecutor{
    private static final Logger LOG = Logger.getLogger(IssueOverdueHandler.class.getName());

    private final IssueService issueService;
    private final Thesaurus thesaurus;

    public IssueOverdueHandler(IssueService issueService, Thesaurus thesaurus) {
        this.issueService = issueService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        Condition overdueCondition = where("overdue").isEqualTo(false)
                .and(where("dueDate").isLessThan(System.currentTimeMillis()));
        Query<Issue> overdueIssuesQuery = issueService.query(Issue.class);
        List<Issue> overdueIssues = overdueIssuesQuery.select(overdueCondition);
        for (Issue issue : overdueIssues) {
            markOverdue(issue);
            doOverdueActions(issue);
            MessageSeeds.ISSUE_OVERDUE_NOTIFICATION.log(LOG, thesaurus, issue.getTitle());
        }
    }

    private void markOverdue(Issue issue){
        if (issue != null) {
            issue.setOverdue(true);
            issue.update();
        }
    }

    private void doOverdueActions(Issue issue) {
        new IssueActionExecutor(issue, CreationRuleActionPhase.OVERDUE, thesaurus).run();
    }
}
