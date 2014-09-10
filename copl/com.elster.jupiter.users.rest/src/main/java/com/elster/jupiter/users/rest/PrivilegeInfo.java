package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Privilege;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivilegeInfo {
    
    public String name;

    public PrivilegeInfo(Privilege privilege) {
        name = privilege.getName();
    }

    public PrivilegeInfo() {
    }
}
