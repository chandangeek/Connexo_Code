package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.rest.GroupInfo;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class DeleteGroupTransaction extends VoidTransaction {

    private final GroupInfo info;

    public DeleteGroupTransaction(GroupInfo info) {
        this.info = info;
    }

    @Override
    protected void doPerform() {
        Group group = fetchGroup();
        validateDelete(group);
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
        Optional<Group> group = Bus.getUserService().getGroup(info.id);
        if (group.isPresent()) {
            return group.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

}
