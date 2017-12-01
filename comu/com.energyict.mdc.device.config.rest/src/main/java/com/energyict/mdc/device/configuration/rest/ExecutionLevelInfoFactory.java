/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessorUserAction;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.users.Group;

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
    public ExecutionLevelInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public List<ExecutionLevelInfo> getEditPrivileges(Collection<SecurityAccessorUserAction> userActions, List<Group> allGroups) {
        return userActions.stream()
                .filter(SecurityAccessorUserAction::isEditing)
                .map(userAction -> from(userAction, allGroups))
                .sorted((l1, l2) -> l1.name.compareToIgnoreCase(l2.name))
                .collect(toList());
    }

    public List<ExecutionLevelInfo> getViewPrivileges(Collection<SecurityAccessorUserAction> userActions, List<Group> allGroups) {
        return userActions.stream()
                .filter(SecurityAccessorUserAction::isViewing)
                .map(userAction -> from(userAction, allGroups))
                .sorted((l1, l2) -> l1.name.compareToIgnoreCase(l2.name))
                .collect(toList());
    }

    public ExecutionLevelInfo from(SecurityAccessorUserAction userAction, List<Group> allGroups) {
        ExecutionLevelInfo info = new ExecutionLevelInfo();
        info.id = userAction.getPrivilege();
        info.name = KeyFunctionTypePrivilegeTranslationKeys.translationFor(userAction.getPrivilege(), thesaurus);
        info.userRoles = allGroups.stream()
                .filter(group -> group.hasPrivilege("MDC", userAction.getPrivilege()))
                .sorted(Comparator.comparing(Group::getName, String.CASE_INSENSITIVE_ORDER))
                .map(group -> new IdWithNameInfo(group.getId(), group.getName()))
                .collect(toList());
        return info;
    }
}
