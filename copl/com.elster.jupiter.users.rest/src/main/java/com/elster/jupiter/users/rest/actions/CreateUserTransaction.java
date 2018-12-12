/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.actions;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;
import com.elster.jupiter.users.rest.UserInfo;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


public class CreateUserTransaction implements Transaction<User> {

    private final UserInfo info;

    private final UserService userService;

    public CreateUserTransaction(UserInfo info, UserService userService) {
        this.info = info;
        this.userService = userService;
    }

    @Override
    public User perform() {
        User user = userService.createUser(info.authenticationName, info.description);
        user.update();

        for (GroupInfo groupInfo : info.groups) {
            Optional<Group> group = userService.getGroup(groupInfo.id);
            if (group.isPresent()) {
                user.join(group.get());
            } else {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }

        return user;
    }

}