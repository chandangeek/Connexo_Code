package com.elster.jupiter.issue.impl.records.assignee.types;

import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.records.BaseIssueImpl;
import com.elster.jupiter.issue.impl.records.IssueAssigneeImpl;
import com.elster.jupiter.issue.impl.records.assignee.AssigneeTeamImpl;
import com.elster.jupiter.issue.share.entity.AssigneeTeam;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;

public class GroupType extends AssigneeType{
    @Override
    public IssueAssigneeImpl getAssignee(BaseIssueImpl issueImpl) {
        checkIssue(issueImpl);
        return issueImpl.getGroup();
    }

    @Override
    public void applyAssigneeToIssue(BaseIssueImpl issue, IssueAssigneeImpl issueAssignee) {
        checkIssue(issue);
        issue.setGroup(AssigneeTeamImpl.class.cast(issueAssignee));
    }

    @Override
    public IssueAssigneeImpl getAssignee(IssueService issueService, UserService userService, long id) {
        Optional<AssigneeTeam> assigneeRef = issueService.findAssigneeTeam(id);
        if (assigneeRef.isPresent()){
            return AssigneeTeamImpl.class.cast(assigneeRef.get());
        }
        return null;
    }

    @Override
    public String getType() {
        return IssueAssignee.Types.GROUP;
    }

    @Override
    public String getColumnName() {
        return DatabaseConst.ISSUE_COLUMN_TEAM_ID;
    }
}
