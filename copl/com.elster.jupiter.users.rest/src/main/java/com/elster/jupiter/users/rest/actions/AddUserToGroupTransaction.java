package com.elster.jupiter.users.rest.actions;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.UserInGroupInfo;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

public class AddUserToGroupTransaction implements Transaction<List<Group>> {
    private final UserInGroupInfo info;
    private final UserService userService;

    public AddUserToGroupTransaction(UserInGroupInfo info, UserService userService) {
        this.info = info;
        this.userService = userService;
    }

    @Override
    public List<Group> perform() {
        Optional<User> foundUser = userService.getUser(info.userId);
        Optional<Group> foundGroup = userService.getGroup(info.groupInfo.id);
        if(foundUser.isPresent() && foundGroup.isPresent()){
            foundUser.get().join(foundGroup.get());

            return foundUser.get().getGroups();
        }

        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
