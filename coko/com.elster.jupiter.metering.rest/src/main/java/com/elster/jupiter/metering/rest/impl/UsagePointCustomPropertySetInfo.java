package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.properties.PropertySpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsagePointCustomPropertySetInfo {

    public long id;
    public String name;
    public boolean isVersioned;
    public Set<ViewPrivilege> viewPrivileges;
    public Set<EditPrivilege> editPrivileges;
    public List<UsagePointCustomPropertySetAttributeInfo> attributes;

    public UsagePointCustomPropertySetInfo() {
    }

    public UsagePointCustomPropertySetInfo(RegisteredCustomPropertySet registeredCustomPropertySet) {
        this.id = registeredCustomPropertySet.getId();
        this.name = registeredCustomPropertySet.getCustomPropertySet().getName();
        this.viewPrivileges = registeredCustomPropertySet.getViewPrivileges();
        this.editPrivileges = registeredCustomPropertySet.getEditPrivileges();
        this.isVersioned = registeredCustomPropertySet.getCustomPropertySet().isVersioned();
        this.attributes = getAttributes(registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs());
    }

    public static List<UsagePointCustomPropertySetAttributeInfo> getAttributes(List<PropertySpec> propertySpecs) {
        List<UsagePointCustomPropertySetAttributeInfo> usagePointCustomPropertySetAttributeInfos = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            usagePointCustomPropertySetAttributeInfos.add(new UsagePointCustomPropertySetAttributeInfo(propertySpec));
        }
        return usagePointCustomPropertySetAttributeInfos;
    }

    public static List<UsagePointCustomPropertySetInfo> from(List<RegisteredCustomPropertySet> registeredCustomPropertySets) {
        List<UsagePointCustomPropertySetInfo> usagePointCustomPropertySetInfos = new ArrayList<>();
        for (RegisteredCustomPropertySet registeredCustomPropertySet : registeredCustomPropertySets) {
            usagePointCustomPropertySetInfos.add(new UsagePointCustomPropertySetInfo(registeredCustomPropertySet));
        }
        return usagePointCustomPropertySetInfos;
    }
}