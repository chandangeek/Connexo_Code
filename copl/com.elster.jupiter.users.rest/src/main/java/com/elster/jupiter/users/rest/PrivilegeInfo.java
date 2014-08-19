package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Privilege;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivilegeInfo {
    
    public String name;

    public PrivilegeInfo(Privilege privilege) {
        name = privilege.getName();
    }

    public PrivilegeInfo() {
    }
}
