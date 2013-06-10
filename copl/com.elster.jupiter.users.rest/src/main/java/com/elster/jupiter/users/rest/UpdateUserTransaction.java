package com.elster.jupiter.users.rest;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class UpdateUserTransaction implements Transaction<User> {

    private final UserInfo info;

    public UpdateUserTransaction(UserInfo info) {
        this.info = info;
    }

    @Override
    public User perform() {
        User user = fetchPerson();
        validateUpdate(user);
        return doUpdate(user);
    }

    private User doUpdate(User user) {
        info.update(user);
        user.save();
        return user;
    }

    private void validateUpdate(User user) {
        if (user.getVersion() != info.version) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    private User fetchPerson() {
        Optional<User> user = Bus.getUserService().getUser(info.id);
        if (user.isPresent()) {
            return user.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
