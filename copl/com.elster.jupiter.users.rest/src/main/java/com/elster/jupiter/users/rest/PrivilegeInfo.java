package com.elster.jupiter.users.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.Privilege;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivilegeInfo {

    public String name;
    public String translatedName;
    public String applicationName;
    public String translatedApplicationName;

    public static PrivilegeInfo asApplicationPrivilege(Thesaurus thesaurus, String applicationName, Privilege privilege){
        PrivilegeInfo privilegeInfo = new PrivilegeInfo(thesaurus, privilege);
        privilegeInfo.applicationName = applicationName;
        privilegeInfo.translatedApplicationName = thesaurus.getStringBeyondComponent(applicationName, applicationName);
        return privilegeInfo;
    }

    public PrivilegeInfo() {
    }

    public PrivilegeInfo(Thesaurus thesaurus, Privilege privilege) {
        this();
        this.name = privilege.getName();
        this.translatedName = thesaurus.getStringBeyondComponent(this.name, this.name);
    }

    public PrivilegeInfo(Thesaurus thesaurus, String applicationName, Privilege privilege) {
        this(thesaurus, privilege);
        this.applicationName = applicationName;
        this.translatedApplicationName = thesaurus.getStringBeyondComponent(applicationName, applicationName);
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