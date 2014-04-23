package com.elster.jupiter.issue.impl.records.assignee.types;

import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.records.BaseIssueImpl;
import com.elster.jupiter.issue.impl.records.IssueAssigneeImpl;
import com.elster.jupiter.issue.impl.records.assignee.AssigneeRoleImpl;
import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;

public class RoleType extends AssigneeType{
    @Override
    public IssueAssigneeImpl getAssignee(BaseIssueImpl issueImpl) {
        checkIssue(issueImpl);
        return issueImpl.getRole();
    }

    @Override
    public void applyAssigneeToIssue(BaseIssueImpl issue, IssueAssigneeImpl issueAssignee) {
        checkIssue(issue);
        issue.setRole(AssigneeRoleImpl.class.cast(issueAssignee));
    }

    @Override
    public IssueAssigneeImpl getAssignee(IssueService issueService, UserService userService, long id) {
        Optional<AssigneeRole> assigneeRef = issueService.findAssigneeRole(id);
        if (assigneeRef.isPresent()){
            return AssigneeRoleImpl.class.cast(assigneeRef.get());
        }
        return null;
    }

    @Override
    public String getType() {
        return IssueAssignee.Types.ROLE;
    }

    @Override
    public String getColumnName() {
        return DatabaseConst.ISSUE_COLUMN_ROLE_ID;
    }
}
