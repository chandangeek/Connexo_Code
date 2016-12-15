package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.issue.impl.records.IssueAssigneeImpl;
import com.elster.jupiter.issue.impl.records.IssueImpl;
import com.elster.jupiter.issue.impl.records.assignee.types.AssigneeTypeImpl;
import com.elster.jupiter.issue.impl.records.assignee.types.UserType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.users.UserService;

import java.util.Optional;

public enum AssigneeType {

    USER(new UserType()), ;

    private AssigneeTypeImpl typeImpl;

    private AssigneeType(AssigneeTypeImpl type) {
        this.typeImpl = type;
    }

    public String getType() {
        return typeImpl.getType();
    }

    public IssueAssigneeImpl getAssignee(IssueImpl issue) {
        return typeImpl.getAssignee(issue);
    }

    public Optional<IssueAssignee> getAssignee(IssueService issueService, UserService userService, long id) {
        return typeImpl.getAssignee(issueService, userService, id);
    }

    public String getColumnName() {
        return typeImpl.getColumnName();
    }

    public void applyAssigneeToIssue(IssueImpl issue, IssueAssigneeImpl issueAssignee) {
        if (issue != null) {
            typeImpl.applyAssigneeToIssue(issue, issueAssignee);
        }
    }

    public static AssigneeType fromString(String assigneeType) {
        if (assigneeType != null) {
            for (AssigneeType column : AssigneeType.values()) {
                if (column.getType().equalsIgnoreCase(assigneeType)) {
                    return column;
                }
            }
        }
        return null;
    }
}
