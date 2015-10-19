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
import java.util.LinkedHashSet;
import java.util.Set;

public class UpdateUserTransaction implements Transaction<User> {

    private final UserInfo info;

    private final UserService userService;

    public UpdateUserTransaction(UserInfo info, UserService userService) {
        this.info = info;
        this.userService = userService;
    }

    @Override
    public User perform() {
        User user = fetchUser();
        validateUpdate(user);
        return doUpdate(user);
    }

    private User doUpdate(User user) {
        boolean updated = updateMemberships(user);
        updated |= info.update(user);
        if(updated){
            user.update();
        }
        return user;
    }

    private boolean updateMemberships(User user) {
        Set<Group> current = new LinkedHashSet<>(user.getGroups());
        Set<Group> target = targetMemberships();
        if (target.equals(current)) {
            return false;
        }

        removeMemberships(user, current, target);
        addMemberships(user, current, target);
        return true;
    }

    private void addMemberships(User user, Set<Group> current, Set<Group> target) {
        Set<Group> toAdd = new LinkedHashSet<>(target);
        toAdd.removeAll(current);
        for (Group group : toAdd) {
            user.join(group);
        }
    }

    private void removeMemberships(User user, Set<Group> current, Set<Group> targetMemberships) {
        Set<Group> toRemove = new LinkedHashSet<>(current);
        toRemove.removeAll(targetMemberships);
        for (Group group : toRemove) {
            user.leave(group);
        }
    }

    private Set<Group> targetMemberships() {
        Set<Group> target = new LinkedHashSet<>();
        for (GroupInfo groupInfo : info.groups) {
            Optional<Group> group = userService.getGroup(groupInfo.id);
            if (group.isPresent()) {
                target.add(group.get());
            } else {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }
        return target;
    }

    private void validateUpdate(User user) {
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
