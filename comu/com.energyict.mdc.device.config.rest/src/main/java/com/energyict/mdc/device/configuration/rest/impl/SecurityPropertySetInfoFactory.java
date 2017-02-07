/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.users.Group;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.configuration.rest.SecurityLevelInfo;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by bvn on 9/12/14.
 */
public class SecurityPropertySetInfoFactory {
    private final Thesaurus thesaurus;
    private final ExecutionLevelInfoFactory executionLevelInfoFactory;

    @Inject
    public SecurityPropertySetInfoFactory(Thesaurus thesaurus, ExecutionLevelInfoFactory executionLevelInfoFactory) {
        this.thesaurus = thesaurus;
        this.executionLevelInfoFactory = executionLevelInfoFactory;
    }

    public SecurityPropertySetInfo from(SecurityPropertySet securityPropertySet, List<Group> allGroups) {
        SecurityPropertySetInfo info = new SecurityPropertySetInfo();
        info.id = securityPropertySet.getId();
        info.name = securityPropertySet.getName();
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = securityPropertySet.getAuthenticationDeviceAccessLevel();
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = securityPropertySet.getEncryptionDeviceAccessLevel();
        info.authenticationLevelId = authenticationDeviceAccessLevel.getId();
        info.encryptionLevelId = encryptionDeviceAccessLevel.getId();
        info.authenticationLevel = SecurityLevelInfo.from(authenticationDeviceAccessLevel);
        info.encryptionLevel = SecurityLevelInfo.from(encryptionDeviceAccessLevel);

        info.executionLevels = executionLevelInfoFactory.from(securityPropertySet.getUserActions(), allGroups, securityPropertySet);
        info.version = securityPropertySet.getVersion();
        DeviceConfiguration deviceConfiguration = securityPropertySet.getDeviceConfiguration();
        info.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());
        return info;
    }

}