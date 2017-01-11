package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.dualcontrol.Privileges;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.rest.GroupInfo;
import com.elster.jupiter.users.rest.PrivilegeInfo;

import javax.inject.Inject;
import java.security.Principal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GroupInfoFactory {
    private final ThreadPrincipalService principalService;

    @Inject
    public GroupInfoFactory(ThreadPrincipalService principalService) {
        this.principalService = principalService;
    }

    public GroupInfo from(NlsService nlsService, Group group) {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.id = group.getId();
        groupInfo.name = group.getName();
        groupInfo.version = group.getVersion();
        groupInfo.description = group.getDescription();
        groupInfo.createdOn = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(group.getCreationDate().atZone(ZoneId.systemDefault()));
        groupInfo.modifiedOn = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(group.getModifiedDate().atZone(ZoneId.systemDefault()));
        groupInfo.privileges.addAll(group.getPrivileges()
                .entrySet()
                .stream()
                .flatMap(x->x.getValue().stream().map(p->PrivilegeInfo.asApplicationPrivilege(nlsService, x.getKey(), p)))
                .collect(Collectors.toList()));

        Collections.sort(groupInfo.privileges, (p1, p2) -> {
            int result = p1.applicationName.compareTo(p2.applicationName);
            return result != 0 ? result : p1.name.compareTo(p2.name);
            });
        groupInfo.canEdit = !groupInfo.privileges.stream()
                .filter(privilegeInfo -> !privilegeInfo.canGrant)
                .findAny()
                .isPresent();

        groupInfo.currentUserCanGrant = canCurrentUserGrantRole(group);

        return groupInfo;
    }

    private Optional<User> getCurrentUser() {
        Principal principal = principalService.getPrincipal();
        if (!(principal instanceof com.elster.jupiter.users.User)) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of((com.elster.jupiter.users.User) principal);
    }

    private boolean canCurrentUserGrantRole(Group group) {
        if (!getCurrentUser().isPresent()) {
            return false;
        }
        User currentUser = getCurrentUser().get();
        boolean canGrantNormalPrivileges = currentUser.getPrivileges()
                .stream()
                .filter(privilege -> privilege.getName().equals(com.elster.jupiter.users.security.Privileges.ADMINISTRATE_USER_ROLE.getKey()))
                .findAny()
                .isPresent();
        boolean canGrantDualControlPrivileges = currentUser.getPrivileges().stream()
                .filter(privilege -> privilege.getName().equals(Privileges.GRANT_DUAL_CONTROL_APPROVAL.getKey()))
                .findAny()
                .isPresent();

        boolean groupContainsDualControlPrivileges = group.getPrivileges().entrySet().stream()
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .filter(privilege -> privilege.getCategory().getName().equals(DualControlService.DUAL_CONTROL_APPROVE_CATEGORY))
                .findAny()
                .isPresent();

        return canGrantNormalPrivileges && !groupContainsDualControlPrivileges || canGrantDualControlPrivileges && groupContainsDualControlPrivileges;
    }

}
