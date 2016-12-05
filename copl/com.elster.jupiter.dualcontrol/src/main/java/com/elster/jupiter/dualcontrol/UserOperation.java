package com.elster.jupiter.dualcontrol;

import com.elster.jupiter.users.User;

public interface UserOperation {
    User getUser();

    UserAction getAction();

    default boolean isApproval() {
        return UserAction.APPROVE.equals(getAction());
    }

    default boolean isRequest() {
        return UserAction.REQUEST.equals(getAction());
    }
}
