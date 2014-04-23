package com.elster.jupiter.issue.impl.records.assignee.types;

import com.elster.jupiter.issue.impl.records.BaseIssueImpl;
import com.elster.jupiter.issue.impl.records.IssueAssigneeImpl;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.users.UserService;

public enum AssigneeTypes {
    USER(new UserType()),
    GROUP(new GroupType()),
    ROLE(new RoleType());

    private AssigneeType realType;

    private AssigneeTypes(AssigneeType type){
        this.realType = type;
    }

    public String getType(){
        return realType.getType();
    }

    public IssueAssigneeImpl getAssignee(BaseIssueImpl issue){
        return realType.getAssignee(issue);
    }

    public IssueAssigneeImpl getAssignee(IssueService issueService, UserService userService, long id){
        return realType.getAssignee(issueService, userService, id);
    }

    public String getColumnName(){
        return realType.getColumnName();
    }

    public void applyAssigneeToIssue(BaseIssueImpl issue, IssueAssigneeImpl issueAssignee){
        if (issue != null) {
            issue.setAssigneeType(this);
            realType.applyAssigneeToIssue(issue, issueAssignee);
        }
    }

    public static AssigneeTypes fromString(String assigneeType) {
        if (assigneeType != null) {
            for (AssigneeTypes column : AssigneeTypes.values()) {
                if (column.getType().equalsIgnoreCase(assigneeType)) {
                    return column;
                }
            }
        }
        return null;
    }
}
