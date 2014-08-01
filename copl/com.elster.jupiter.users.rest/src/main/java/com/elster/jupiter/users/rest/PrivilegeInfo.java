package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Privilege;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivilegeInfo {
    
    public String id;
    public String name;

    public PrivilegeInfo(Privilege privilege) {
        id = privilege.getCode();
        name = privilege.getName();
    }

    public PrivilegeInfo() {
    }
}
