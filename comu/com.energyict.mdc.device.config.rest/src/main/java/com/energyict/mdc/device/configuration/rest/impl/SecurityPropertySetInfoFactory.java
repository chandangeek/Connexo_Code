package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.configuration.rest.SecurityLevelInfo;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

import javax.inject.Inject;

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

    public SecurityPropertySetInfo from(SecurityPropertySet securityPropertySet) {
        SecurityPropertySetInfo securityPropertySetInfo = new SecurityPropertySetInfo();
        securityPropertySetInfo.id = securityPropertySet.getId();
        securityPropertySetInfo.name = securityPropertySet.getName();
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = securityPropertySet.getAuthenticationDeviceAccessLevel();
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = securityPropertySet.getEncryptionDeviceAccessLevel();
        securityPropertySetInfo.authenticationLevelId = authenticationDeviceAccessLevel.getId();
        securityPropertySetInfo.encryptionLevelId = encryptionDeviceAccessLevel.getId();
        securityPropertySetInfo.authenticationLevel = SecurityLevelInfo.from(authenticationDeviceAccessLevel, thesaurus);
        securityPropertySetInfo.encryptionLevel = SecurityLevelInfo.from(encryptionDeviceAccessLevel, thesaurus);

        securityPropertySetInfo.executionLevels = executionLevelInfoFactory.from(securityPropertySet.getUserActions());
        return securityPropertySetInfo;
    }


}
