package com.elster.jupiter.users.rest.actions;

import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;

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
            doDelete(group.get());
        }
    }

    private void doDelete(Group group) {
        group.delete();
    }
}
