/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.Monitor;
import com.elster.jupiter.dualcontrol.UserAction;
import com.elster.jupiter.dualcontrol.UserOperation;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;

import java.time.Instant;

class UserOperationImpl implements UserOperation {

    enum Fields {
        MONITOR("monitor"),
        USER("user"),
        USER_ACTION("userAction"),
        POSITION("position")
        ;

        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private Reference<Monitor> monitor = ValueReference.absent();
    private Reference<User> user = ValueReference.absent();
    private UserAction userAction;
    private int position;

    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;


    static UserOperationImpl of(Monitor monitor, User user, UserAction userAction) {
        UserOperationImpl userOperation = new UserOperationImpl();
        userOperation.init(monitor, user, userAction);
        return userOperation;
    }

    @Override
    public User getUser() {
        return user.get();
    }

    @Override
    public UserAction getAction() {
        return userAction;
    }

    private void init(Monitor monitor, User user, UserAction userAction) {
        this.monitor.set(monitor);
        this.user.set(user);
        this.userAction = userAction;
    }
}
