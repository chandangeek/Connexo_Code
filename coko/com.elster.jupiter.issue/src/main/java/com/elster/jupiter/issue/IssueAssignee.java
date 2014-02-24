package com.elster.jupiter.issue;

import com.elster.jupiter.users.User;

public interface IssueAssignee {
    IssueAssigneeType getType();
    User getUser();
    AssigneeTeam getTeam();
    AssigneeRole getRole();
}
