package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Privilege;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivilegeInfo {
    
    public String name;
    public String applicationName;

    public static PrivilegeInfo asApllicationPrivilege(String applicationName, Privilege privilege){
        PrivilegeInfo privilegeInfo = new PrivilegeInfo(privilege);
        privilegeInfo.applicationName = applicationName;
        return privilegeInfo;
    }
    public PrivilegeInfo(Privilege privilege) {
        name = privilege.getName();
    }

    public PrivilegeInfo() {
    }
}
