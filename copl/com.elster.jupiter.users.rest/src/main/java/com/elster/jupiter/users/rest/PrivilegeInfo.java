package com.elster.jupiter.users.rest;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.users.Privilege;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivilegeInfo {

    public String name;
    public String translatedName;
    public String applicationName;
    public String translatedApplicationName;

    public static PrivilegeInfo asApplicationPrivilege(NlsService nlsService, String applicationName, Privilege privilege){
        PrivilegeInfo privilegeInfo = new PrivilegeInfo(nlsService, privilege);
        privilegeInfo.applicationName = applicationName;
        privilegeInfo.translatedApplicationName = nlsService.getPrivilegeThesaurus().get(applicationName);
        return privilegeInfo;
    }

    public PrivilegeInfo() {
    }

    public PrivilegeInfo(NlsService nlsService, Privilege privilege) {
        this();
        this.name = privilege.getName();
        this.translatedName = nlsService.getPrivilegeThesaurus().get(this.name);
    }

    public PrivilegeInfo(NlsService nlsService, String applicationName, Privilege privilege) {
        this(nlsService, privilege);
        this.applicationName = applicationName;
        this.translatedApplicationName = nlsService.getPrivilegeThesaurus().get(applicationName);
    }

    public String getApplicationName(){
        return applicationName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, applicationName);
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