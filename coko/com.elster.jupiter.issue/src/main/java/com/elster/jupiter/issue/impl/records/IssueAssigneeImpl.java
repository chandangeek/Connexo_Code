package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;

public class IssueAssigneeImpl implements IssueAssignee {

    private User user;
    private WorkGroup workGroup;

    public IssueAssigneeImpl(){
    }

    public IssueAssigneeImpl(User user, WorkGroup workGroup) {
        this.user = user;
        this.workGroup = workGroup;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public WorkGroup getWorkGroup() {
        return workGroup;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void setWorkGroup(WorkGroup workGroup) {
        this.workGroup = workGroup;
    }
}