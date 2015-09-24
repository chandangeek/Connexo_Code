package com.energyict.mdc.masterdata.rest;

import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomAttributeSetInfo {

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
    public List<CustomAttributeSetAttributeInfo> attributes;

    public CustomAttributeSetInfo() {
    }

    public CustomAttributeSetInfo(RegisteredCustomPropertySet registeredCustomPropertySet, Thesaurus thesaurus) {
        this.id = registeredCustomPropertySet.getId();
        this.name = registeredCustomPropertySet.getCustomPropertySet().getName();
        this.domainName = thesaurus.getString(registeredCustomPropertySet.getCustomPropertySet().getDomainClass().getName(),
                registeredCustomPropertySet.getCustomPropertySet().getDomainClass().getName());
        this.isActive = true;
        this.isRequired = registeredCustomPropertySet.getCustomPropertySet().isRequired();
        this.isVersioned = registeredCustomPropertySet.getCustomPropertySet().isVersioned();
        this.attributes = CustomAttributeSetAttributeInfo.from(registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs());
        this.viewPrivileges = registeredCustomPropertySet.getViewPrivileges();
        this.editPrivileges = registeredCustomPropertySet.getEditPrivileges();
        this.defaultViewPrivileges = registeredCustomPropertySet.getCustomPropertySet().defaultViewPrivileges();
        this.defaultEditPrivileges = registeredCustomPropertySet.getCustomPropertySet().defaultEditPrivileges();
    }

    public static List<CustomAttributeSetInfo> from(Iterable<? extends RegisteredCustomPropertySet> registeredCustomPropertySets, Thesaurus thesaurus) {
        List<CustomAttributeSetInfo> customAttributeSetInfos = new ArrayList<>();
        for (RegisteredCustomPropertySet registeredCustomPropertySet : registeredCustomPropertySets) {
            customAttributeSetInfos.add(new CustomAttributeSetInfo(registeredCustomPropertySet, thesaurus));
        }
        return customAttributeSetInfos;
    }
}