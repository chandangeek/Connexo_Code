/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.actions;

import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.UserInfo;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class DeleteUserTransaction extends VoidTransaction {

    private final UserInfo info;
    private final UserService userService;

    public DeleteUserTransaction(UserInfo info, UserService userService) {
        this.info = info;
        this.userService = userService;
    }

    @Override
    protected void doPerform() {
        User user = fetchUser();
        validateDelete(user);
        doDelete(user);
    }

    private void doDelete(User user) {
        user.delete();
    }

    private void validateDelete(User user) {
        if (user.getVersion() != info.version) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    private User fetchUser() {
        Optional<User> user = userService.getUser(info.id);
        if (user.isPresent()) {
            return user.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
