/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

    default boolean isRejection() {
        return UserAction.REJECT.equals(getAction());
    }
}
