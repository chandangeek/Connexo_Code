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
    long getId();
    String getType();
    String getName();
    long getVersion();

    public static class Types {
        private Types(){}

        public static final String USER = "USER";
    }
}