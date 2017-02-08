/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records.assignee.types;

import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.records.IssueAssigneeImpl;
import com.elster.jupiter.issue.impl.records.IssueImpl;
import com.elster.jupiter.issue.impl.records.assignee.AssigneeUserImpl;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import java.util.Optional;

public class UserType extends AssigneeTypeImpl {

    @Override
    public IssueAssigneeImpl getAssignee(IssueImpl issueImpl) {
        checkIssue(issueImpl);
        return new AssigneeUserImpl(issueImpl.getUser());
    }

    @Override
    public void applyAssigneeToIssue(IssueImpl issue, IssueAssigneeImpl issueAssignee) {
        checkIssue(issue);
        AssigneeUserImpl assigneeImpl = AssigneeUserImpl.class.cast(issueAssignee);
        issue.setUser(assigneeImpl.getUser());
    }

    @Override
    public Optional<IssueAssignee> getAssignee(IssueService issueService, UserService userService, long id) {
        Optional<User> assigneeRef = userService.getUser(id);
        return assigneeRef.map(user -> Optional.<IssueAssignee> of(new AssigneeUserImpl(assigneeRef.get()))).orElse(Optional.empty());
    }

    @Override
    public String getType() {
        return IssueAssignee.Types.USER;
    }

    @Override
    public String getColumnName() {
        return DatabaseConst.ISSUE_COLUMN_USER_ID;
    }
}
