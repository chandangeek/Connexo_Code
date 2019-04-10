/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
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
    public PropertyInfo client;
    @JsonProperty("securitySuite")
    public SecurityLevelInfo securitySuite;
    @JsonProperty("requestSecurityLevel")
    public SecurityLevelInfo requestSecurityLevel;
    @JsonProperty("responseSecurityLevel")
    public SecurityLevelInfo responseSecurityLevel;

    public List<PropertyInfo> properties;

    public long version;
    public VersionInfo<String> parent;
    public Boolean hasServiceKeys;

    public SecurityPropertySetInfo() {
    }

}