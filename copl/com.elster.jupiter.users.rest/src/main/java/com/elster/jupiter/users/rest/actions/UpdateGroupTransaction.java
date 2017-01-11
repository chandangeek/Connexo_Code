package com.elster.jupiter.users.rest.actions;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateGroupTransaction extends UpdateMembership implements Transaction<Group> {
    private final ConcurrentModificationExceptionFactory conflictFactory;

    public UpdateGroupTransaction(GroupInfo info, UserService userService, ConcurrentModificationExceptionFactory conflictFactory) {
        super(info, userService);
        this.conflictFactory = conflictFactory;
    }

    @Override
    public Group perform() {
        final Group group = findAndLockGroupByIdAndVersion(info);
        if(canEditGroup(group)) {
            if(info.privileges.isEmpty()){
                Group groupNoRights = doUpdateEmpty(group);
                info.update(group);
                groupNoRights.update();
                return groupNoRights;
            }else {
                final Group removedGroup = doUpdateEmpty(group, info.privileges);
                info.privileges.stream().collect(Collectors.groupingBy(pi -> pi.applicationName))
                        .entrySet()
                        .stream()
                        .forEach(p -> doUpdate(p.getKey(), removedGroup));
                removedGroup.update();
                return removedGroup;
            }
        } else {
            throw new IllegalArgumentException("Can't edit this group");
        }
    }

    private boolean canEditGroup(Group group) {
        return !group.getPrivileges().entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .filter(privilege ->  !privilege.getCategory().getName().equals(UserService.DEFAULT_CATEGORY_NAME))
                .findAny()
                .isPresent();
    }

    private Group findAndLockGroupByIdAndVersion(GroupInfo info) {
        return userService.findAndLockGroupByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> userService.getGroup(info.id).map(Group::getVersion).orElse(null))
                        .supplier());
    }
}
