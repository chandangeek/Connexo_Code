package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.common.rest.IdWithNameInfo;
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
    public IdWithNameInfo status;

    public List<PropertyInfo> properties;
    public boolean userHasViewPrivilege;
    public boolean userHasEditPrivilege;

    public boolean saveAsIncomplete;
    public long version;
    public VersionInfo<String> parent;

    public SecurityPropertySetInfo() {
    }

}
