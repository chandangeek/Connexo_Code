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
        return !group.getPrivileges().entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .filter(privilege -> privilege.getCategory().getName().equals(DualControlService.DUAL_CONTROL_GRANT_CATEGORY)
                        || privilege.getCategory().getName().equals(DualControlService.DUAL_CONTROL_APPROVE_CATEGORY))
                .findAny()
                .isPresent();
    }

    private void doDelete(Group group) {
        group.delete();
    }
}
