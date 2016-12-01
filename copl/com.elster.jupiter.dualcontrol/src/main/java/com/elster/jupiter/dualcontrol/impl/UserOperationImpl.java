package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.UserAction;
import com.elster.jupiter.dualcontrol.UserOperation;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import org.osgi.service.useradmin.User;

public class UserOperationImpl implements UserOperation {

    private Reference<User> user = ValueReference.absent();
    private UserAction userAction;



    @Override
    public User getUser() {
        return user.get();
    }

    @Override
    public UserAction getAction() {
        return userAction;
    }
}
