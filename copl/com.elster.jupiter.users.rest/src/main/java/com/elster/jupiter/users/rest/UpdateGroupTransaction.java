package com.elster.jupiter.users.rest;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.LinkedHashSet;
import java.util.Set;

public class UpdateGroupTransaction implements Transaction<Group> {

    private final GroupInfo info;

    public UpdateGroupTransaction(GroupInfo info) {
        this.info = info;
    }

    @Override
    public Group perform() {
        Group group = fetchGroup();
        validateUpdate(group);
        return doUpdate(group);
    }

    private Group doUpdate(Group group) {
        group.save();
        updateMemberships(group);
        return group;
    }

    private void updateMemberships(Group group) {
        Set<Privilege> current = new LinkedHashSet<>(group.getPrivileges());
        Set<Privilege> target = targetGrants();
        revoke(group, current, target);
        grant(group, current, target);
    }

    private void grant(Group group, Set<Privilege> current, Set<Privilege> target) {
        Set<Privilege> toAdd = new LinkedHashSet<>(target);
        toAdd.removeAll(current);
        for (Privilege privilege : toAdd) {
            group.grant(privilege);
        }
    }

    private void revoke(Group group, Set<Privilege> current, Set<Privilege> targetMemberships) {
        Set<Privilege> toRemove = new LinkedHashSet<>(current);
        toRemove.removeAll(targetMemberships);
        for (Privilege privilege : toRemove) {
            group.revoke(privilege);
        }
    }

    private Set<Privilege> targetGrants() {
        Set<Privilege> target = new LinkedHashSet<>();
        for (PrivilegeInfo privilegeInfo : info.privileges) {
            Optional<Privilege> privilege = Bus.getUserService().getPrivilege(privilegeInfo.name);
            if (privilege.isPresent()) {
                target.add(privilege.get());
            } else {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }
        return target;
    }

    private void validateUpdate(Group group) {
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
