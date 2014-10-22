package com.elster.jupiter.issue.impl.records.assignee.types;

import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.records.IssueAssigneeImpl;
import com.elster.jupiter.issue.impl.records.IssueImpl;
import com.elster.jupiter.issue.impl.records.assignee.AssigneeRoleImpl;
import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.users.UserService;
import java.util.Optional;

public class RoleType extends AssigneeTypeImpl {
    @Override
    public IssueAssigneeImpl getAssignee(IssueImpl issueImpl) {
        checkIssue(issueImpl);
        return issueImpl.getRole();
    }

    @Override
    public void applyAssigneeToIssue(IssueImpl issue, IssueAssigneeImpl issueAssignee) {
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
