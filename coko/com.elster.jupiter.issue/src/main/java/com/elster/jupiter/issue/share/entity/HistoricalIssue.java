package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class HistoricalIssue extends Issue {

    @Inject
    public HistoricalIssue(DataModel dataModel, UserService userService, IssueService issueService, IssueAssignmentService issueAssignmentService) {
        super(dataModel, userService, issueService, issueAssignmentService);
    }

    HistoricalIssue copy(Issue issue) {
        super.copy(issue);
        return this;
    }
}
