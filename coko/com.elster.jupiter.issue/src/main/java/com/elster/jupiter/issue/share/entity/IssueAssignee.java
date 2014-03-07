package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.users.User;

public class IssueAssignee {

    private IssueAssigneeType type;
    private User user;
    private AssigneeRole role;
    private AssigneeTeam team;

    public IssueAssigneeType getType() {
        return type;
    }

    public void setType(IssueAssigneeType type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AssigneeRole getRole() {
        return role;
    }

    public void setRole(AssigneeRole role) {
        this.role = role;
    }

    public AssigneeTeam getTeam() {
        return team;
    }

    public void setTeam(AssigneeTeam team) {
        this.team = team;
    }
}