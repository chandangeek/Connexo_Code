/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.properties.PropertySpec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceTypeCustomPropertySetInfo {

    public long id;
    public String name;
    public boolean isVersioned;
    public Set<ViewPrivilege> viewPrivileges;
    public Set<EditPrivilege> editPrivileges;
    public List<DeviceTypeCustomPropertySetAttributeInfo> attributes;

    public DeviceTypeCustomPropertySetInfo() {
    }

    public DeviceTypeCustomPropertySetInfo(RegisteredCustomPropertySet registeredCustomPropertySet) {
        this.id = registeredCustomPropertySet.getId();
        this.name = registeredCustomPropertySet.getCustomPropertySet().getName();
        this.viewPrivileges = registeredCustomPropertySet.getViewPrivileges();
        this.editPrivileges = registeredCustomPropertySet.getEditPrivileges();
        this.isVersioned = registeredCustomPropertySet.getCustomPropertySet().isVersioned();
        this.attributes = getAttributes(registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs());
    }

    public static List<DeviceTypeCustomPropertySetAttributeInfo> getAttributes(List<PropertySpec> propertySpecs) {
        List<DeviceTypeCustomPropertySetAttributeInfo> deviceTypeCustomPropertySetAttributeInfos = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            deviceTypeCustomPropertySetAttributeInfos.add(new DeviceTypeCustomPropertySetAttributeInfo(propertySpec));
        }
        return deviceTypeCustomPropertySetAttributeInfos;
    }

    public static List<DeviceTypeCustomPropertySetInfo> from(List<RegisteredCustomPropertySet> registeredCustomPropertySets) {
        List<DeviceTypeCustomPropertySetInfo> deviceTypeCustomPropertySetInfos = new ArrayList<>();
        for (RegisteredCustomPropertySet registeredCustomPropertySet : registeredCustomPropertySets) {
            deviceTypeCustomPropertySetInfos.add(new DeviceTypeCustomPropertySetInfo(registeredCustomPropertySet));
        }
        return deviceTypeCustomPropertySetInfos;
    }
}