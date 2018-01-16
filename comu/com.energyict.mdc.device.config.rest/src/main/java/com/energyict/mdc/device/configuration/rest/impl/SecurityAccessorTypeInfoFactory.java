/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityAccessorUserAction;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.configuration.rest.ExecutionLevelInfoFactory;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class SecurityAccessorTypeInfoFactory {
    private final ExecutionLevelInfoFactory executionLevelInfoFactory;
    private final UserService userService;

    @Inject
    public SecurityAccessorTypeInfoFactory(ExecutionLevelInfoFactory executionLevelInfoFactory, UserService userService) {
        this.executionLevelInfoFactory = executionLevelInfoFactory;
        this.userService = userService;
    }

    public SecurityAccessorTypeInfo from(SecurityAccessorType securityAccessorType) {
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = securityAccessorType.getId();
        info.version = securityAccessorType.getVersion();
        info.name = securityAccessorType.getName();
        info.description = securityAccessorType.getDescription();
        info.keyType = new KeyTypeInfo(securityAccessorType.getKeyType());
        info.storageMethod = info.keyType.isKey ? securityAccessorType.getKeyEncryptionMethod() : null;
        info.trustStoreId = !info.keyType.isKey && securityAccessorType.getTrustStore().isPresent() ? securityAccessorType
                .getTrustStore().get().getId() : 0;
        if (securityAccessorType.getKeyType().getCryptographicType().requiresDuration() && securityAccessorType.getDuration().isPresent()) {
            info.duration = new TimeDurationInfo(securityAccessorType.getDuration().get());
        }
        return info;
    }

    public SecurityAccessorTypeInfo withSecurityLevels(SecurityAccessorType securityAccessorType) {
        SecurityAccessorTypeInfo info = from(securityAccessorType);
        Set<SecurityAccessorUserAction> allUserActions = EnumSet.allOf(SecurityAccessorUserAction.class);
        List<Group> groups = userService.getGroups();
        Set<SecurityAccessorUserAction> keyAccessorTypeUserActions = securityAccessorType.getUserActions();
        info.editLevels = executionLevelInfoFactory.getEditPrivileges(keyAccessorTypeUserActions, groups);
        info.defaultEditLevels = executionLevelInfoFactory.getEditPrivileges(allUserActions, groups);
        info.viewLevels = executionLevelInfoFactory.getViewPrivileges(keyAccessorTypeUserActions, groups);
        info.defaultViewLevels = executionLevelInfoFactory.getViewPrivileges(allUserActions, groups);
        return info;
    }
}
