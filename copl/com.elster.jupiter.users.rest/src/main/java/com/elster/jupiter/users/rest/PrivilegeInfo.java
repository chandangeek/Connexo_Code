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

    public PrivilegeInfo(String applicationName, Privilege privilege) {
        this.applicationName = applicationName;
        name = privilege.getName();
    }

    public PrivilegeInfo() {
    }

    public String getApplicationName(){
        return applicationName;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PrivilegeInfo)) {
            return false;
        }

        PrivilegeInfo privilegeInfo = (PrivilegeInfo) o;

        return (name.equals(privilegeInfo.name)) && (applicationName.equals(privilegeInfo.applicationName)) ;

    }
}
