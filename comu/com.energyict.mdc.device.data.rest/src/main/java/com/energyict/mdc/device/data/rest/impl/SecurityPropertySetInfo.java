package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.SecurityLevelInfo;
import java.util.List;
import org.codehaus.jackson.annotate.JsonProperty;

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

    public SecurityPropertySetInfo() {
    }

}
