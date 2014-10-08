package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class OpenIssueImpl extends IssueImpl implements OpenIssue{

    @Inject
    public OpenIssueImpl(DataModel dataModel, UserService userService, IssueService issueService, IssueAssignmentService issueAssignmentService) {
        super(dataModel, userService, issueService, issueAssignmentService);
    }
}
