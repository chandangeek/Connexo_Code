package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import javax.inject.Inject;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 9/12/14.
 */
public class SecurityPropertySetInfoFactory {
    private final Thesaurus thesaurus;
    private final UserService userService;

    @Inject
    public SecurityPropertySetInfoFactory(Thesaurus thesaurus, UserService userService) {
        this.thesaurus = thesaurus;
        this.userService = userService;
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
        securityPropertySetInfo.encryptionLevels = SecurityLevelInfo.from(encryptionDeviceAccessLevel, thesaurus);

        securityPropertySetInfo.executionLevels = securityPropertySet.getUserActions().stream().
                map(userAction -> new ExecutionLevelInfo(
                        userAction.getPrivilege(),
                        thesaurus.getString(userAction.getPrivilege(), userAction.getPrivilege()),
                        userService.getGroups().stream()
                                .filter(group -> group.hasPrivilege(userAction.getPrivilege()))
                                .sorted((group1, group2) -> group1.getName().compareToIgnoreCase(group2.getName()))
                                .map(group -> new IdWithNameInfo(group.getId(), group.getName()))
                                .collect(toList())))
                .sorted((l1, l2) -> l1.name.compareToIgnoreCase(l2.name))
                .collect(toList());
        return securityPropertySetInfo;
    }


}
