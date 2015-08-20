package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;

import javax.inject.Inject;
import java.util.Collection;
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

    public List<ExecutionLevelInfo> from(Collection<DeviceSecurityUserAction> userActions, List<Group> allGroups) {
        return userActions.stream().
                        map(userAction -> new ExecutionLevelInfo(
                                userAction.getPrivilege(),
                                thesaurus.getString(userAction.getPrivilege(), userAction.getPrivilege()),
                                allGroups.stream()
                                        .filter(group -> group.hasPrivilege("MDC", userAction.getPrivilege()))
                                        .sorted((group1, group2) -> group1.getName().compareToIgnoreCase(group2.getName()))
                                        .map(group -> new IdWithNameInfo(group.getId(), group.getName()))
                                        .collect(toList())))
                        .sorted((l1, l2) -> l1.name.compareToIgnoreCase(l2.name))
                        .collect(toList());
    }

}