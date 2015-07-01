package com.elster.jupiter.users.rest.actions;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;

import java.util.stream.Collectors;

public class CreateGroupTransaction extends UpdateMembership implements Transaction<Group> {

    public CreateGroupTransaction(GroupInfo info, UserService userService) {
        super(info, userService);
    }

    @Override
    public Group perform() {
        Group group = userService.createGroup(info.name, info.description);
        info.privileges.stream().collect(Collectors.groupingBy(pi -> pi.applicationName))
                .entrySet()
                .stream()
                .forEach(p->doUpdate(p.getKey(), group));
        return group;
    }
}
