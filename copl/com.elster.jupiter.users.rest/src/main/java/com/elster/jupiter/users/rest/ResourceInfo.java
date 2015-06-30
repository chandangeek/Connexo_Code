package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.Resource;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceInfo {
    public String componentName;
    public String name;
    public String qualifiedName;
    public String description;
    public List<PrivilegeInfo> privileges = new ArrayList<>();

    public ResourceInfo(Resource resource) {
        componentName = resource.getComponentName();
        name = resource.getName();
        description = resource.getDescription();
        qualifiedName = resource.getComponentName()+"."+resource.getName();
        for (Privilege privilege : resource.getPrivileges()) {
            privileges.add(new PrivilegeInfo(privilege));
        }

        Collections.sort(privileges, new Comparator<PrivilegeInfo>() {
            public int compare(PrivilegeInfo p1, PrivilegeInfo p2) {
                return p1.name.compareTo(p2.name);
            }
        });
    }

    public ResourceInfo() {
    }
}
