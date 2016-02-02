package com.elster.jupiter.cps.rest;

import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.ViewPrivilege;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomPropertySetInfo {
    public long id;
    public String customPropertySetId;
    public String name;
    public String domainName;
    public boolean isActive;
    public boolean isRequired;
    public boolean isVersioned;
    public Set<ViewPrivilege> viewPrivileges;
    public Set<EditPrivilege> editPrivileges;
    public Set<ViewPrivilege> defaultViewPrivileges;
    public Set<EditPrivilege> defaultEditPrivileges;
    public List<CustomPropertySetAttributeInfo> properties;
}