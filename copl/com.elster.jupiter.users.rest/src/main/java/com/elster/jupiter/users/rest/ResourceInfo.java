package com.elster.jupiter.users.rest;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.PrivilegeThesaurus;
import com.elster.jupiter.users.Resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceInfo {
    public String componentName;
    public String translatedName;
    public String name;
    public String qualifiedName;
    public String description;
    public List<PrivilegeInfo> privileges;

    public ResourceInfo(NlsService nlsService, Resource resource) {
        this();
        PrivilegeThesaurus thesaurus = nlsService.getPrivilegeThesaurus();
        componentName = resource.getComponentName();
        translatedName = thesaurus.translateComponentName(resource.getComponentName());
        name = thesaurus.translateResourceName(resource.getName());
        description = thesaurus.translatePrivilegeKey(resource.getDescription());
        qualifiedName = componentName + "." + name;
        privileges =
                resource
                        .getPrivileges()
                        .stream()
                        .map(privilege -> new PrivilegeInfo(nlsService, privilege))
                        .filter(privilegeInfo -> privilegeInfo.canGrant)
                        .sorted((p1, p2) -> p1.name.compareTo(p2.name))
                        .collect(Collectors.toList());
    }

    public ResourceInfo() {
    }

}