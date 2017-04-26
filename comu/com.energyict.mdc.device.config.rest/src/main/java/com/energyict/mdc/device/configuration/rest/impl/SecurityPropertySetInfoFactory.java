/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.users.Group;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.configuration.rest.ExecutionLevelInfoFactory;
import com.energyict.mdc.device.configuration.rest.SecurityLevelInfo;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.RequestSecurityLevel;
import com.energyict.mdc.protocol.api.security.ResponseSecurityLevel;
import com.energyict.mdc.protocol.api.security.SecuritySuite;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * Created by bvn on 9/12/14.
 */
public class SecurityPropertySetInfoFactory {
    private final Thesaurus thesaurus;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final ExecutionLevelInfoFactory executionLevelInfoFactory;

    @Inject
    public SecurityPropertySetInfoFactory(Thesaurus thesaurus, MdcPropertyUtils mdcPropertyUtils, ExecutionLevelInfoFactory executionLevelInfoFactory) {
        this.thesaurus = thesaurus;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.executionLevelInfoFactory = executionLevelInfoFactory;
    }

    public SecurityPropertySetInfo from(SecurityPropertySet securityPropertySet, List<Group> allGroups) {
        SecurityPropertySetInfo info = new SecurityPropertySetInfo();
        info.id = securityPropertySet.getId();
        info.name = securityPropertySet.getName();
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = securityPropertySet.getAuthenticationDeviceAccessLevel();
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = securityPropertySet.getEncryptionDeviceAccessLevel();
        SecuritySuite securitySuite = securityPropertySet.getSecuritySuite();
        RequestSecurityLevel requestSecurityLevel = securityPropertySet.getRequestSecurityLevel();
        ResponseSecurityLevel responseSecurityLevel = securityPropertySet.getResponseSecurityLevel();
        info.authenticationLevelId = authenticationDeviceAccessLevel.getId();
        info.authenticationLevel = SecurityLevelInfo.from(authenticationDeviceAccessLevel);
        info.encryptionLevelId = encryptionDeviceAccessLevel.getId();
        info.encryptionLevel = SecurityLevelInfo.from(encryptionDeviceAccessLevel);
        info.client = securityPropertySet.getClient();
        info.securitySuiteId = securitySuite.getId();
        info.securitySuite = SecurityLevelInfo.from(securitySuite);
        info.requestSecurityLevelId = requestSecurityLevel.getId();
        info.requestSecurityLevel = SecurityLevelInfo.from(requestSecurityLevel);
        info.responseSecurityLevelId = responseSecurityLevel.getId();
        info.responseSecurityLevel = SecurityLevelInfo.from(responseSecurityLevel);
        info.version = securityPropertySet.getVersion();
        DeviceConfiguration deviceConfiguration = securityPropertySet.getDeviceConfiguration();
        info.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());

        TypedProperties typedProperties = this.toTypedProperties(securityPropertySet.getConfigurationSecurityProperties());
        info.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(securityPropertySet.getPropertySpecs(), typedProperties);
        Collections.sort(info.properties, (o1, o2) -> o1.name.compareToIgnoreCase(o2.name)); // Properties are sorted by their name
        return info;
    }

    private TypedProperties toTypedProperties(List<ConfigurationSecurityProperty> configurationSecurityProperties) {
        TypedProperties typedProperties = TypedProperties.empty();
        for (ConfigurationSecurityProperty configurationSecurityProperty : configurationSecurityProperties) {
            typedProperties.setProperty(configurationSecurityProperty.getName(), configurationSecurityProperty.getKeyAccessor());
        }
        return typedProperties;
    }
}