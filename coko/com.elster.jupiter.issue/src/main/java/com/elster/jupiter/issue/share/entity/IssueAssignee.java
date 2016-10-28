package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface IssueAssignee {

    User getUser();
    WorkGroup getWorkGroup();
    void setUser(User user);
    void setWorkGroup(WorkGroup workGroup);
}