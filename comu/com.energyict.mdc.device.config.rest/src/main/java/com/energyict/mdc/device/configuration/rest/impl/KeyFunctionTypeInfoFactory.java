/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.KeyAccessorType;
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

    public SecurityAccessorInfo from(KeyAccessorType keyAccessorType, DeviceType deviceType) {
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = keyAccessorType.getId();
        info.name = keyAccessorType.getName();
        info.description = keyAccessorType.getDescription();
        info.keyType = new KeyTypeInfo(keyAccessorType.getKeyType());
        info.storageMethod = info.keyType.isKey ? keyAccessorType.getKeyEncryptionMethod() : null;
        info.trustStoreId = !info.keyType.isKey && keyAccessorType.getTrustStore().isPresent() ? keyAccessorType.getTrustStore().get().getId() : 0;
        if (keyAccessorType.getKeyType().getCryptographicType().requiresDuration() && keyAccessorType.getDuration().isPresent()) {
            info.duration = new TimeDurationInfo(keyAccessorType.getDuration().get());
        }
        info.parent = new VersionInfo<>(deviceType.getName(), deviceType.getVersion());
        return info;
    }

    public SecurityAccessorInfo withSecurityLevels(KeyAccessorType keyAccessorType, DeviceType deviceType) {
        SecurityAccessorInfo info = from(keyAccessorType, deviceType);
        Set<DeviceSecurityUserAction> allUserActions = EnumSet.allOf(DeviceSecurityUserAction.class);
        List<Group> groups = userService.getGroups();
        Set<DeviceSecurityUserAction> keyAccessorTypeUserActions = deviceType.getKeyAccessorTypeUserActions(keyAccessorType);
        info.editLevels = executionLevelInfoFactory.getEditPrivileges(keyAccessorTypeUserActions, groups);
        info.defaultEditLevels = executionLevelInfoFactory.getEditPrivileges(allUserActions, groups);
        info.viewLevels = executionLevelInfoFactory.getViewPrivileges(keyAccessorTypeUserActions, groups);
        info.defaultViewLevels = executionLevelInfoFactory.getViewPrivileges(allUserActions, groups);
        return info;
    }
}
