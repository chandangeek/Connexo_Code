package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Privilege;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivilegeInfo {
    
    public String componentName;
    public String name;
    public String description;

    public PrivilegeInfo(Privilege privilege) {
        componentName = privilege.getComponentName();
        name = privilege.getName();
        description = privilege.getDescription();
    }

    public PrivilegeInfo() {
    }
}
