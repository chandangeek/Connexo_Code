package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.UserAction;
import com.elster.jupiter.dualcontrol.UserOperation;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;

public class UserOperationImpl implements UserOperation {

    private Reference<User> user = ValueReference.absent();
    private UserAction userAction;

    static UserOperationImpl of(User user, UserAction userAction) {
        UserOperationImpl userOperation = new UserOperationImpl();
        userOperation.init(user, userAction);
        return userOperation;
    }

    private void init(User user, UserAction userAction) {
        this.user.set(user);
        this.userAction = userAction;
    }

    @Override
    public User getUser() {
        return user.get();
    }

    @Override
    public UserAction getAction() {
        return userAction;
    }
}
