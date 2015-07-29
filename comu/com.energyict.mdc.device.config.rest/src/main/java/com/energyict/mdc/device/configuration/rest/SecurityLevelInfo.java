package com.energyict.mdc.device.configuration.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class SecurityLevelInfo {

    public Integer id;
    public String name;

    public SecurityLevelInfo() {
    }

    public static SecurityLevelInfo from(DeviceAccessLevel deviceAccessLevel, Thesaurus thesaurus) {
        SecurityLevelInfo securityLevelInfo = new SecurityLevelInfo();
        securityLevelInfo.id = deviceAccessLevel.getId();
        securityLevelInfo.name = thesaurus.getString(deviceAccessLevel.getTranslationKey(), deviceAccessLevel.getTranslationKey());

        return securityLevelInfo;
    }

    public static List<SecurityLevelInfo> from(List<? extends DeviceAccessLevel> deviceAccessLevels, Thesaurus thesaurus) {
        List<SecurityLevelInfo> securityLevelInfos = new ArrayList<>(deviceAccessLevels.size());
        securityLevelInfos.addAll(deviceAccessLevels.stream().map(deviceAccessLevel -> SecurityLevelInfo.from(deviceAccessLevel, thesaurus)).collect(toList()));
        return securityLevelInfos;
    }
}
