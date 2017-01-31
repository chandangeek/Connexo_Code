/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public class DeviceFirmwareVersionInfo {
    public FirmwareTypeInfo firmwareType;
    public ActiveVersion activeVersion;
    @JsonIgnore
    public Map<String, Map<String, Object>> upgradeVersions = new HashMap<>();

    public static class ActiveVersion {
        public String firmwareVersion;
        public FirmwareStatusInfo firmwareVersionStatus;
        public Long lastCheckedDate;

        public ActiveVersion() {}
    }

    public DeviceFirmwareVersionInfo() {}

    @JsonAnyGetter
    public Map<String, Map<String, Object>> any() {
        upgradeVersions.remove(null);
        return upgradeVersions;
    }
}
