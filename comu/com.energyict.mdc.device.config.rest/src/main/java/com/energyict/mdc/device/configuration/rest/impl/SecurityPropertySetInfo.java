/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.configuration.rest.SecurityLevelInfo;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SecurityPropertySetInfo {

    public Long id;
    public String name;
    @JsonProperty("authenticationLevelId")
    public Integer authenticationLevelId;
    @JsonProperty("encryptionLevelId")
    public Integer encryptionLevelId;
    @JsonProperty("authenticationLevel")
    public SecurityLevelInfo authenticationLevel;
    @JsonProperty("encryptionLevel")
    public SecurityLevelInfo encryptionLevel;

    public List<ExecutionLevelInfo> executionLevels;
    public long version;
    public VersionInfo<Long> parent;

    public SecurityPropertySetInfo() {
    }

    public void writeTo(SecurityPropertySet securityPropertySet) {
        securityPropertySet.setName(this.name);
        if (this.authenticationLevelId == null) {
            this.authenticationLevelId = DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }
        if (this.encryptionLevelId == null) {
            this.encryptionLevelId = DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }
        securityPropertySet.setAuthenticationLevel(this.authenticationLevelId);
        securityPropertySet.setEncryptionLevelId(this.encryptionLevelId);
    }

}