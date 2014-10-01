package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.configuration.rest.impl.ExecutionLevelInfo;
import com.energyict.mdc.device.configuration.rest.impl.SecurityLevelInfo;
import java.util.List;
import org.codehaus.jackson.annotate.JsonProperty;

public class SecurityPropertySetInfo {

    public Long id;
    public String name;
    @JsonProperty("authenticationLevel")
    public SecurityLevelInfo authenticationLevel;
    @JsonProperty("encryptionLevel")
    public SecurityLevelInfo encryptionLevel;

    public List<ExecutionLevelInfo> executionLevels;
    public List<PropertyInfo> properties;
    public boolean userHasViewPrivilege;
    public boolean userHasEditPrivilege;

    public SecurityPropertySetInfo() {
    }


    public void writeTo(SecurityPropertySet securityPropertySet) {
        securityPropertySet.setName(this.name);
        securityPropertySet.setAuthenticationLevel(authenticationLevel.id);
        securityPropertySet.setEncryptionLevelId(encryptionLevel.id);
    }
}
