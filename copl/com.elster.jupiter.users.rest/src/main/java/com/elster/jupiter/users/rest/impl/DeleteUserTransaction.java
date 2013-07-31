package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.rest.UserInfo;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class DeleteUserTransaction extends VoidTransaction {

    private final UserInfo info;

    public DeleteUserTransaction(UserInfo info) {
        this.info = info;
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
        Optional<User> user = Bus.getUserService().getUser(info.id);
        if (user.isPresent()) {
            return user.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
