package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;
import com.elster.jupiter.users.rest.PrivilegeInfo;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.LinkedHashSet;
import java.util.Set;

public class UpdateGroupTransaction extends UpdateMembership implements Transaction<Group> {

    public UpdateGroupTransaction(GroupInfo info, UserService userService) {
        super(info, userService);
    }

    @Override
    public Group perform() {
        Group group = fetchGroup();
        validateUpdate(group);
        return doUpdate(group);
    }

    private void validateUpdate(Group group) {
        if (group.getVersion() != info.version) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    private Group fetchGroup() {
        Optional<Group> group = userService.getGroup(info.id);
        if (group.isPresent()) {
            return group.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
