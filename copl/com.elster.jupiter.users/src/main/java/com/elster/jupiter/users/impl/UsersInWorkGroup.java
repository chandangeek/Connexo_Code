/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;


import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;

import javax.inject.Inject;
import java.time.Instant;

public class UsersInWorkGroup {

    private long workGroupId;
    private long userId;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private User user;
    private WorkGroup workGroup;
    private final DataModel dataModel;

    @Inject
    private UsersInWorkGroup(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    UsersInWorkGroup init(WorkGroup workGroup, User user) {
        this.workGroupId = workGroup.getId();
        this.workGroup = workGroup;
        this.user = user;
        this.userId = user.getId();
        return this;
    }

    static UsersInWorkGroup from(DataModel dataModel, WorkGroup workGroup, User user) {
        return dataModel.getInstance(UsersInWorkGroup.class).init(workGroup, user);
    }

    User getUser() {
        if (user == null) {
            user = dataModel.mapper(User.class).getExisting(userId);
        }
        return user;
    }

    WorkGroup getWorkGroup() {
        if (workGroup == null) {
            workGroup = dataModel.mapper(WorkGroup.class).getExisting(workGroupId);
        }
        return workGroup;
    }

    void persist() {
        dataModel.mapper(UsersInWorkGroup.class).persist(this);
    }

    public void delete() {
        dataModel.mapper(UsersInWorkGroup.class).remove(this);
    }

}
