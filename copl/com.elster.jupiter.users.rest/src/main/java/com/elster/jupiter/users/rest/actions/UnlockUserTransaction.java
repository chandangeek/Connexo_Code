/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.actions;

import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.UserInfo;

public class UnlockUserTransaction implements Transaction<User> {

    private final UserInfo info;
    private final UserService userService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    public UnlockUserTransaction(UserInfo info, UserService userService, ConcurrentModificationExceptionFactory conflictFactory) {
        this.info = info;
        this.userService = userService;
        this.conflictFactory = conflictFactory;
    }

    @Override
    public User perform() {
        User user = fetchUser();
        return doUpdate(user);
    }

    private User doUpdate(User user) {
        boolean updated = info.updateUnsuccessfullLoginCount(user);

        if(updated){
            user.update();
        }
        return user;
    }

    private User fetchUser() {
        return userService.findAndLockUserByIdAndVersion(info.id, info.version).orElseThrow(conflictFactory
                .contextDependentConflictOn(
                        info.authenticationName != null ? info.authenticationName : getUserNameFromId(info.id))
                .withActualVersion(() -> userService.getUser(info.id).map(User::getVersion).orElse(null)).supplier());
    }

    private String getUserNameFromId(long id) {
        return userService.getUser(id).map(User::getName).orElse("-");
    }
}
