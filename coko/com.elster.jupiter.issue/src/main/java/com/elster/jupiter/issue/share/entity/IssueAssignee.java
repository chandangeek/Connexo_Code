package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.users.User;

public class IssueAssignee {

    private IssueAssigneeType type;
    private User user;
    private AssigneeRole role;
    private AssigneeTeam team;

    public static IssueAssignee fromUser(User user){
        if (user != null) {
            IssueAssignee assignee = new IssueAssignee();
            assignee.setType(IssueAssigneeType.USER);
            assignee.setUser(user);
            return assignee;
        }
        return null;
    }

    public static IssueAssignee fromRole(AssigneeRole role){
        if (role != null) {
            IssueAssignee assignee = new IssueAssignee();
            assignee.setType(IssueAssigneeType.ROLE);
            assignee.setRole(role);
            return assignee;
        }
        return null;
    }

    public static IssueAssignee fromTeam(AssigneeTeam team){
        if (team != null) {
            IssueAssignee assignee = new IssueAssignee();
            assignee.setType(IssueAssigneeType.TEAM);
            assignee.setTeam(team);
            return assignee;
        }
        return null;
    }

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