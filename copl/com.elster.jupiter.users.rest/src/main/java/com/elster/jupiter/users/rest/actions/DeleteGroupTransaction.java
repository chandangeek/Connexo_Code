package com.elster.jupiter.users.rest.actions;

import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class DeleteGroupTransaction extends VoidTransaction {

    private final GroupInfo info;
    private final UserService userService;

    public DeleteGroupTransaction(GroupInfo info, UserService userService) {
        this.info = info;
        this.userService = userService;
    }

    @Override
    protected void doPerform() {
        Group group = fetchGroup();
        //validateDelete(group); TODO: check the reason for this validation
        doDelete(group);
    }

    private void doDelete(Group group) {
        group.delete();
    }

    private void validateDelete(Group group) {
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
