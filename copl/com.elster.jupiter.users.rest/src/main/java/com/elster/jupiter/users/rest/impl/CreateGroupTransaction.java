package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.rest.GroupInfo;

public class CreateGroupTransaction implements Transaction<Group> {

    private final GroupInfo info;

    public CreateGroupTransaction(GroupInfo info) {
        this.info = info;
    }

    @Override
    public Group perform() {
        Group group = Bus.getUserService().newGroup(info.name);

        group.save();

        return group;
    }
}
