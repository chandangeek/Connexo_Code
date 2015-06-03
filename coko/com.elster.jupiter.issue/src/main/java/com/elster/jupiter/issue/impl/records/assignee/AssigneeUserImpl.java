package com.elster.jupiter.issue.impl.records.assignee;

import com.elster.jupiter.issue.impl.records.IssueAssigneeImpl;
import com.elster.jupiter.issue.share.entity.AssigneeType;
import com.elster.jupiter.users.User;

public class AssigneeUserImpl extends IssueAssigneeImpl {

    private User user;

    public AssigneeUserImpl(User user) {
        super(null, AssigneeType.USER);
        this.user = user;
    }

    @Override
    public String getName() {
        return user.getName();
    }

    public User getUser(){
        return user;
    }

    @Override
    public long getVersion() {
        return user.getVersion();
    }

    @Override
    public long getId() {
        return user.getId();
    }
}
