package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.configuration.rest.SecurityPropertySetPrivilegeTranslationKeys;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 9/16/14.
 */
public class ExecutionLevelInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public ExecutionLevelInfoFactory(Thesaurus thesaurus, UserService userService) {
        this.thesaurus = thesaurus;
    }

    public List<ExecutionLevelInfo> from(Collection<DeviceSecurityUserAction> userActions, List<Group> allGroups, SecurityPropertySet securityPropertySet) {
        return userActions.stream()
                .map(userAction -> from(userAction, allGroups, securityPropertySet))
                .sorted((l1, l2) -> l1.name.compareToIgnoreCase(l2.name))
                .collect(toList());
    }

    public ExecutionLevelInfo from(DeviceSecurityUserAction userAction, List<Group> allGroups, SecurityPropertySet securityPropertySet) {
        ExecutionLevelInfo info = new ExecutionLevelInfo();
        info.id = userAction.getPrivilege();
        info.name = SecurityPropertySetPrivilegeTranslationKeys.translationFor(userAction.getPrivilege(), thesaurus);
        info.userRoles = allGroups.stream()
                .filter(group -> group.hasPrivilege("MDC", userAction.getPrivilege()))
                .sorted(Comparator.comparing(Group::getName, String.CASE_INSENSITIVE_ORDER))
                .map(group -> new IdWithNameInfo(group.getId(), group.getName()))
                .collect(toList());
        info.parent = new VersionInfo<>(securityPropertySet.getId(), securityPropertySet.getVersion());
        return info;
    }
}
