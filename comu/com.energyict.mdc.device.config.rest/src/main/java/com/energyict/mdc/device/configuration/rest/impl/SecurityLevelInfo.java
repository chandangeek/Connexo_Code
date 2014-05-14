package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

import java.util.ArrayList;
import java.util.List;

public class SecurityLevelInfo {

    public Integer id;
    public String name;

    public SecurityLevelInfo() {
    }

    public static <T extends DeviceAccessLevel> SecurityLevelInfo from(T deviceAccessLevel, Thesaurus thesaurus) {
        SecurityLevelInfo securityLevelInfo = new SecurityLevelInfo();
        securityLevelInfo.id = deviceAccessLevel.getId();
        securityLevelInfo.name = thesaurus.getString(deviceAccessLevel.getTranslationKey(), deviceAccessLevel.getTranslationKey());

        return securityLevelInfo;
    }

    public static <T extends DeviceAccessLevel> List<SecurityLevelInfo> from(List<T> deviceAccessLevels, Thesaurus thesaurus) {
        List<SecurityLevelInfo> securityLevelInfos = new ArrayList<>(deviceAccessLevels.size());
        for (T deviceAccessLevel : deviceAccessLevels) {
            securityLevelInfos.add(SecurityLevelInfo.from(deviceAccessLevel, thesaurus));
        }
        return securityLevelInfos;
    }
}
