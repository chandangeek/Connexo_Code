package com.elster.jupiter.users.rest.actions;

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

public class UpdateMembership {
    final GroupInfo info;
    final UserService userService;

    public UpdateMembership(GroupInfo info, UserService userService) {
        this.info = info;
        this.userService = userService;
    }

    Group doUpdate(Group group) {
        boolean updated = updateMemberships(group);
        if (updated) {
            group.save();
        }
        return group;
    }

    private boolean updateMemberships(Group group) {
        Set<Privilege> current = new LinkedHashSet<>(group.getPrivileges());
        Set<Privilege> target = targetGrants();
        if (target.equals(current)) {
            return false;
        }
        revoke(group, current, target);
        grant(group, current, target);
        return true;
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
            Optional<Privilege> privilege = userService.getPrivilege(privilegeInfo.name);
            if (privilege.isPresent()) {
                target.add(privilege.get());
            } else {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }
        return target;
    }
}
