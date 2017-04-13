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
    @JsonProperty("authenticationLevel")
    public SecurityLevelInfo authenticationLevel;
    @JsonProperty("encryptionLevelId")
    public Integer encryptionLevelId;
    @JsonProperty("encryptionLevel")
    public SecurityLevelInfo encryptionLevel;
    @JsonProperty("securitySuiteId")
    public Integer securitySuiteId;
    @JsonProperty("securitySuite")
    public SecurityLevelInfo securitySuite;
    @JsonProperty("requestSecurityLevelId")
    public Integer requestSecurityLevelId;
    @JsonProperty("requestSecurityLevel")
    public SecurityLevelInfo requestSecurityLevel;
    @JsonProperty("responseSecurityLevelId")
    public Integer responseSecurityLevelId;
    @JsonProperty("responseSecurityLevel")
    public SecurityLevelInfo responseSecurityLevel;

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
        if (this.securitySuiteId == null) {
            this.securitySuiteId = DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }
        if (this.requestSecurityLevelId == null) {
            this.requestSecurityLevelId = DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }
        if (this.responseSecurityLevelId == null) {
            this.responseSecurityLevelId = DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        securityPropertySet.setAuthenticationLevelId(this.authenticationLevelId);
        securityPropertySet.setEncryptionLevelId(this.encryptionLevelId);
        securityPropertySet.setSecuritySuiteId(this.securitySuiteId);
        securityPropertySet.setRequestSecurityLevelId(this.requestSecurityLevelId);
        securityPropertySet.setResponseSecurityLevelId(this.responseSecurityLevelId);;
    }

}