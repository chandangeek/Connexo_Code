/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.configuration.rest.ExecutionLevelInfoFactory;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class KeyFunctionTypeInfoFactory {
    private final ExecutionLevelInfoFactory executionLevelInfoFactory;
    private final UserService userService;

    @Inject
    public KeyFunctionTypeInfoFactory(ExecutionLevelInfoFactory executionLevelInfoFactory, UserService userService) {
        this.executionLevelInfoFactory = executionLevelInfoFactory;
        this.userService = userService;
    }

    public SecurityAccessorInfo from(SecurityAccessorType securityAccessorType, DeviceType deviceType) {
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = securityAccessorType.getId();
        info.name = securityAccessorType.getName();
        info.description = securityAccessorType.getDescription();
        info.keyType = new KeyTypeInfo(securityAccessorType.getKeyType());
        info.storageMethod = info.keyType.isKey ? securityAccessorType.getKeyEncryptionMethod() : null;
        info.trustStoreId = !info.keyType.isKey && securityAccessorType.getTrustStore().isPresent() ? securityAccessorType
                .getTrustStore().get().getId() : 0;
        if (securityAccessorType.getKeyType().getCryptographicType().requiresDuration() && securityAccessorType.getDuration().isPresent()) {
            info.duration = new TimeDurationInfo(securityAccessorType.getDuration().get());
        }
        info.parent = new VersionInfo<>(deviceType.getName(), deviceType.getVersion());
        return info;
    }

    public SecurityAccessorInfo withSecurityLevels(SecurityAccessorType securityAccessorType, DeviceType deviceType) {
        SecurityAccessorInfo info = from(securityAccessorType, deviceType);
        Set<DeviceSecurityUserAction> allUserActions = EnumSet.allOf(DeviceSecurityUserAction.class);
        List<Group> groups = userService.getGroups();
        Set<DeviceSecurityUserAction> keyAccessorTypeUserActions = deviceType.getSecurityAccessorTypeUserActions(securityAccessorType);
        info.editLevels = executionLevelInfoFactory.getEditPrivileges(keyAccessorTypeUserActions, groups);
        info.defaultEditLevels = executionLevelInfoFactory.getEditPrivileges(allUserActions, groups);
        info.viewLevels = executionLevelInfoFactory.getViewPrivileges(keyAccessorTypeUserActions, groups);
        info.defaultViewLevels = executionLevelInfoFactory.getViewPrivileges(allUserActions, groups);
        return info;
    }
}
