/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.actions;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class DeleteGroupTransaction extends VoidTransaction {
    private final GroupInfo info;
    private final UserService userService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    public DeleteGroupTransaction(GroupInfo info, UserService userService, ConcurrentModificationExceptionFactory conflictFactory) {
        this.info = info;
        this.userService = userService;
        this.conflictFactory = conflictFactory;
    }

    @Override
    protected void doPerform() {
        Optional<Group> group = userService.findAndLockGroupByIdAndVersion(info.id, info.version);
        if (group.isPresent()) {
            if(canDeleteGroup(group.get())) {
                doDelete(group.get());
            } else {
                throw new IllegalArgumentException("Can't delete this group");
            }
        }
    }

    private boolean canDeleteGroup(Group group) {
        return !group.getName().equals(UserService.DEFAULT_ADMIN_ROLE) && group.getPrivileges()
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .allMatch(privilege -> privilege.getCategory().getName().equals(UserService.DEFAULT_CATEGORY_NAME));
    }

    private void doDelete(Group group) {
        group.delete();
    }
}
