/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.configuration.rest.SecurityLevelInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SecurityPropertySetInfo {

    public long id;
    public String name;
    @JsonProperty("authenticationLevel")
    public SecurityLevelInfo authenticationLevel;
    @JsonProperty("encryptionLevel")
    public SecurityLevelInfo encryptionLevel;
    @JsonProperty("client")
    public String client;
    @JsonProperty("securitySuite")
    public SecurityLevelInfo securitySuite;
    @JsonProperty("requestSecurityLevel")
    public SecurityLevelInfo requestSecurityLevel;
    @JsonProperty("responseSecurityLevel")
    public SecurityLevelInfo responseSecurityLevel;

    public IdWithNameInfo status;

    public List<PropertyInfo> properties;

    public boolean saveAsIncomplete;
    public long version;
    public VersionInfo<String> parent;

    public SecurityPropertySetInfo() {
    }

}
