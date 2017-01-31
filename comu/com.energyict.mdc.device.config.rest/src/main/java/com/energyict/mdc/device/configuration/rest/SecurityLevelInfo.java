/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest;

import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class SecurityLevelInfo {

    public Integer id;
    public String name;

    public SecurityLevelInfo() {
    }

    public static SecurityLevelInfo from(DeviceAccessLevel deviceAccessLevel) {
        SecurityLevelInfo securityLevelInfo = new SecurityLevelInfo();
        securityLevelInfo.id = deviceAccessLevel.getId();
        securityLevelInfo.name = deviceAccessLevel.getTranslation();

        return securityLevelInfo;
    }

    public static List<SecurityLevelInfo> from(List<? extends DeviceAccessLevel> deviceAccessLevels) {
        List<SecurityLevelInfo> securityLevelInfos = new ArrayList<>(deviceAccessLevels.size());
        securityLevelInfos.addAll(deviceAccessLevels.stream().map(deviceAccessLevel -> SecurityLevelInfo.from(deviceAccessLevel)).collect(toList()));
        return securityLevelInfos;
    }

}