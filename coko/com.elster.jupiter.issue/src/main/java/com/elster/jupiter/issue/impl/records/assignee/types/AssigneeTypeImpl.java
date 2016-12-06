package com.elster.jupiter.issue.impl.records.assignee.types;

import com.elster.jupiter.issue.impl.records.IssueAssigneeImpl;
import com.elster.jupiter.issue.impl.records.IssueImpl;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.users.UserService;

import java.util.Optional;

public abstract class AssigneeTypeImpl {

    public abstract String getType();

    public abstract String getColumnName();

    public abstract IssueAssigneeImpl getAssignee(IssueImpl issueImpl);

    public abstract void applyAssigneeToIssue(IssueImpl issue, IssueAssigneeImpl issueAssignee);

    public abstract Optional<IssueAssignee> getAssignee(IssueService issueService, UserService userService, long id);

    protected void checkIssue(IssueImpl issueImpl) {
        if (issueImpl == null) {
            throw new IllegalArgumentException("Issue can't be null");
        }
    }
}
