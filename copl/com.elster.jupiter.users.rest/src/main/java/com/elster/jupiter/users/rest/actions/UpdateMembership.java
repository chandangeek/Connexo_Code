package com.elster.jupiter.users.rest.actions;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;
import com.elster.jupiter.users.rest.PrivilegeInfo;
import java.util.Optional;

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

    Group doUpdate(String applicationName, Group group) {
        boolean updated = updateMemberships(applicationName, group);
        updated |= info.update(group);
        if (updated) {
            group.save();
        }
        return group;
    }

    private boolean updateMemberships(String applicationName, Group group) {
        Set<Privilege> current = new LinkedHashSet<>(group.getPrivileges(applicationName));
        Set<Privilege> target = targetGrants(applicationName);
        if (target.equals(current)) {
            return false;
        }
        revoke(applicationName, group, current, target);
        grant(applicationName, group, current, target);
        return true;
    }

    private void grant(String applicationName, Group group, Set<Privilege> current, Set<Privilege> target) {
        Set<Privilege> toAdd = new LinkedHashSet<>(target);
        toAdd.removeAll(current);
        for (Privilege privilege : toAdd) {
            group.grant(applicationName, privilege);
        }
    }

    private void revoke(String applicationName, Group group, Set<Privilege> current, Set<Privilege> targetMemberships) {
        Set<Privilege> toRemove = new LinkedHashSet<>(current);
        toRemove.removeAll(targetMemberships);
        for (Privilege privilege : toRemove) {
            group.revoke(applicationName, privilege);
        }
    }

    private Set<Privilege> targetGrants(String applicationName) {
        Set<Privilege> target = new LinkedHashSet<>();
        for (PrivilegeInfo privilegeInfo : info.privileges) {
            if(applicationName.equalsIgnoreCase(privilegeInfo.applicationName)) {
                Optional<Privilege> privilege = userService.getPrivilege(privilegeInfo.name);
                if (privilege.isPresent()) {
                    target.add(privilege.get());
                } else {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }
            }
        }
        return target;
    }
}
