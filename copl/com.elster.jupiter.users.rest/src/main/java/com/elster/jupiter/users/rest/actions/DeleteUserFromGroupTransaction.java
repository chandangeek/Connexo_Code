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

public class DeleteUserFromGroupTransaction  implements Transaction<List<Group>> {

    private final UserInGroupInfo info;
    private final UserService userService;

    public DeleteUserFromGroupTransaction(UserInGroupInfo info, UserService userService) {
        this.info = info;
        this.userService = userService;
    }

    @Override
    public List<Group> perform() {
        User user = fetchUser();
        Group group = fetchGroup();

        validateDelete(user, group);
        doDelete(user, group);

        return user.getGroups();
    }

    private void doDelete(User user, Group group) {
        user.leave(group);
    }

    private void validateDelete(User user, Group group) {
        if(!user.isMemberOf(group)){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        //TODO: check what is the reason for this validation
        /*if (group.getVersion() != info.groupInfo.version) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }*/
    }

    private User fetchUser() {
        Optional<User> foundUser = userService.getUser(info.userId);
        if(!foundUser.isPresent()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return foundUser.get();
    }

    private Group fetchGroup() {
        Optional<Group> foundGroup = userService.getGroup(info.groupInfo.id);
        if(!foundGroup.isPresent()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return foundGroup.get();
    }
}
