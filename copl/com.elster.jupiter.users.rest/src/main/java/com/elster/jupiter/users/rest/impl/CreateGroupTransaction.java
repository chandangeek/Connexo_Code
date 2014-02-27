package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;

public class CreateGroupTransaction implements Transaction<Group> {

    private final GroupInfo info;
    private final UserService userService;

    public CreateGroupTransaction(GroupInfo info, UserService userService) {
        this.info = info;
        this.userService = userService;
    }

    @Override
    public Group perform() {
        Group group = userService.newGroup(info.name);

        group.save();

        return group;
    }
}
