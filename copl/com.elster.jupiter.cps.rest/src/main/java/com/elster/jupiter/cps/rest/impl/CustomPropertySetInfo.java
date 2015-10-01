package com.elster.jupiter.cps.rest.impl;

import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomPropertySetInfo {

    public long id;
    public String name;
    public String domainName;
    public boolean isActive;
    public boolean isRequired;
    public boolean isVersioned;
    public Set<ViewPrivilege> viewPrivileges;
    public Set<EditPrivilege> editPrivileges;
    public Set<ViewPrivilege> defaultViewPrivileges;
    public Set<EditPrivilege> defaultEditPrivileges;
    public List<CustomPropertySetAttributeInfo> attributes;

    public CustomPropertySetInfo() {
    }

    public CustomPropertySetInfo(RegisteredCustomPropertySet registeredCustomPropertySet,
                                 List<CustomPropertySetAttributeInfo> attributes, String domainName) {
        this.id = registeredCustomPropertySet.getId();
        this.name = registeredCustomPropertySet.getCustomPropertySet().getName();
        this.domainName = domainName;
        this.isActive = true;
        this.isRequired = registeredCustomPropertySet.getCustomPropertySet().isRequired();
        this.isVersioned = registeredCustomPropertySet.getCustomPropertySet().isVersioned();
        this.attributes = attributes;
        this.viewPrivileges = registeredCustomPropertySet.getViewPrivileges();
        this.editPrivileges = registeredCustomPropertySet.getEditPrivileges();
        this.defaultViewPrivileges = registeredCustomPropertySet.getCustomPropertySet().defaultViewPrivileges();
        this.defaultEditPrivileges = registeredCustomPropertySet.getCustomPropertySet().defaultEditPrivileges();
    }
}