package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.IssueAssignee;
import com.elster.jupiter.issue.IssueAssigneeType;
import com.elster.jupiter.issue.AssigneeRole;
import com.elster.jupiter.issue.AssigneeTeam;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;

public class IssueAssigneeImpl implements IssueAssignee {

    private IssueAssigneeType type;
    private Reference<User> user = ValueReference.absent();
    private Reference<AssigneeTeam> team = ValueReference.absent();
    private Reference<AssigneeRole> role = ValueReference.absent();

    public IssueAssigneeType getType() {
        return type;
    }

    public void setType(IssueAssigneeType type) {
        this.type = type;
    }

    public User getUser() {
        return user.orNull();
    }

    public void setUser(User user) {
        this.user.set(user);
    }

    public AssigneeTeam getTeam() {
        return team.orNull();
    }

    public void setTeam(AssigneeTeam team) {
        this.team.set(team);
    }

    public AssigneeRole getRole() {
        return role.orNull();
    }

    public void setRole(AssigneeRole role) {
        this.role.set(role);
    }
}