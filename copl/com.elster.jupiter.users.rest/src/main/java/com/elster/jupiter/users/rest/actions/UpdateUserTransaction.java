/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.actions;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.FailToDeactivateUser;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;
import com.elster.jupiter.users.rest.UserInfo;

import java.util.LinkedHashSet;
import java.util.Set;

public class UpdateUserTransaction implements Transaction<User> {

    private final UserInfo info;
    private final UserService userService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    public UpdateUserTransaction(UserInfo info, UserService userService, ConcurrentModificationExceptionFactory conflictFactory) {
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
        boolean userStatus = user.getStatus();
        boolean updated = updateMemberships(user);
        updated |= info.update(user);
        if(userStatus != user.getStatus() && !canUserBeDeactivated(user)){
            throw new FailToDeactivateUser(userService.getThesaurus(), MessageSeeds.CANNOT_REMOVE_ALL_USER_ADMINISTRATORS);
        }
        if(updated){
            user.update();
        }
        return user;
    }

    private boolean canUserBeDeactivated(User user){
        return user.getGroups().stream().noneMatch(g -> g.getName().equals(UserService.DEFAULT_ADMIN_ROLE)) || isAnotherUserWithUserAdministratorRole(user);
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

    private boolean isAnotherUserWithUserAdministratorRole(User user){
        return userService.getUsers().stream()
                .filter(u -> u.getId() != user.getId())
                .filter(User::getStatus)
                .map(User::getGroups)
                .anyMatch(groupList -> groupList.stream().anyMatch(g -> g.getName().equals(UserService.DEFAULT_ADMIN_ROLE)));
    }

    private boolean isGroupNotRemovable(User user, Set<Group> current, Set<Group> targetMemberships){
        return current.stream().anyMatch(g -> g.getName().equals(UserService.DEFAULT_ADMIN_ROLE))
                && targetMemberships.stream().noneMatch(g -> g.getName().equals(UserService.DEFAULT_ADMIN_ROLE))
                && !isAnotherUserWithUserAdministratorRole(user);

    }

    private void removeMemberships(User user, Set<Group> current, Set<Group> targetMemberships) {
        if(isGroupNotRemovable(user, current, targetMemberships)){
            throw new LocalizedFieldValidationException(MessageSeeds.CANNOT_REMOVE_ALL_USER_ADMINISTRATORS, "roles");
        }
        Set<Group> toRemove = new LinkedHashSet<>(current);
        toRemove.removeAll(targetMemberships);
        for (Group group : toRemove) {
            user.leave(group);
        }
    }

    private Set<Group> targetMemberships() {

        Set<Group> target = new LinkedHashSet<>();
        for (GroupInfo groupInfo : info.groups) {
            Group group = userService.getGroup(groupInfo.id).orElseThrow(conflictFactory.contextDependentConflictOn(groupInfo.name)
                    .withActualVersion(() -> userService.getGroup(groupInfo.id).map(Group::getVersion).orElse(0L))
                    .supplier());
            target.add(group);
        }
        return target;
    }

    private User fetchUser() {
        return userService.findAndLockUserByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.authenticationName)
                        .withActualVersion(() -> userService.getUser(info.id).map(User::getVersion).orElse(null))
                        .supplier());
    }
}
