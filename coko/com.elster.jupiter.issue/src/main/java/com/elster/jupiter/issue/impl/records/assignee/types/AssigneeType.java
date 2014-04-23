package com.elster.jupiter.issue.impl.records.assignee.types;

import com.elster.jupiter.issue.impl.records.BaseIssueImpl;
import com.elster.jupiter.issue.impl.records.IssueAssigneeImpl;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.users.UserService;

public abstract class AssigneeType {
    public abstract String getType();
    public abstract String getColumnName();
    public abstract IssueAssigneeImpl getAssignee(BaseIssueImpl issueImpl);
    public abstract void applyAssigneeToIssue(BaseIssueImpl issue, IssueAssigneeImpl issueAssignee);
    public abstract IssueAssigneeImpl getAssignee(IssueService issueService, UserService userService, long id);

    protected  void checkIssue(BaseIssueImpl issueImpl){
        if (issueImpl == null) {
            throw new IllegalArgumentException("Issue can't be null");
        }
    }

}
