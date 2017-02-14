/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Set;

public class KeyFunctionTypeInfoFactory {
    private final ExecutionLevelInfoFactory executionLevelInfoFactory;
    private final UserService userService;

    @Inject
    public KeyFunctionTypeInfoFactory(ExecutionLevelInfoFactory executionLevelInfoFactory, UserService userService) {
        this.executionLevelInfoFactory = executionLevelInfoFactory;
        this.userService = userService;
    }

    public KeyFunctionTypeInfo from(KeyAccessorType keyFunctionType, DeviceType deviceType) {
        KeyFunctionTypeInfo info = new KeyFunctionTypeInfo();
        info.id = keyFunctionType.getId();
        info.name = keyFunctionType.getName();
        info.description = keyFunctionType.getDescription();
        info.keyType = new KeyTypeInfo(keyFunctionType.getKeyType());
        if (keyFunctionType.getDuration().isPresent()) {
            info.validityPeriod = new TimeDurationInfo(keyFunctionType.getDuration().get());
        }
        info.parent = new VersionInfo<>(deviceType.getName(), deviceType.getVersion());
        Set<DeviceSecurityUserAction> allUserActions = EnumSet.allOf(DeviceSecurityUserAction.class);
        info.editLevels = executionLevelInfoFactory.getEditPrivileges(deviceType.getKeyAccessorTypeUserActions(keyFunctionType), userService.getGroups());
        info.defaultEditLevels = executionLevelInfoFactory.getEditPrivileges(allUserActions, userService.getGroups());
        info.viewLevels = executionLevelInfoFactory.getViewPrivileges(deviceType.getKeyAccessorTypeUserActions(keyFunctionType), userService.getGroups());
        info.defaultViewLevels = executionLevelInfoFactory.getViewPrivileges(allUserActions, userService.getGroups());
        return info;
    }
}
