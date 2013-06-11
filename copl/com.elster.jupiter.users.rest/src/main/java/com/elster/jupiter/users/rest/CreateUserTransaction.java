package com.elster.jupiter.users.rest;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


public class CreateUserTransaction implements Transaction<User> {

    private final UserInfo info;

    public CreateUserTransaction(UserInfo info) {
        this.info = info;
    }

    @Override
    public User perform() {
        User user = Bus.getUserService().newUser(info.authenticationName);
        user.setDescription(info.description);

        user.save();

        for (GroupInfo groupInfo : info.groups) {
            Optional<Group> group = Bus.getUserService().getGroup(groupInfo.id);
            if (group.isPresent()) {
                user.join(group.get());
            } else {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }

        return user;
    }

}