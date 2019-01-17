/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceTypeCustomPropertySetInfo {

    public long id;
    public String name;
    public String domainName;
    public boolean isVersioned;
    public Set<ViewPrivilege> viewPrivileges;
    public Set<EditPrivilege> editPrivileges;
    public List<DeviceTypeCustomPropertySetAttributeInfo> attributes;
    public List<PropertyInfo> properties;

    public DeviceTypeCustomPropertySetInfo() {
    }

    public DeviceTypeCustomPropertySetInfo(RegisteredCustomPropertySet registeredCustomPropertySet) {
        this.id = registeredCustomPropertySet.getId();
        this.name = registeredCustomPropertySet.getCustomPropertySet().getName();
        this.domainName = registeredCustomPropertySet.getCustomPropertySet().getDomainClassDisplayName();
        this.viewPrivileges = registeredCustomPropertySet.getViewPrivileges();
        this.editPrivileges = registeredCustomPropertySet.getEditPrivileges();
        this.isVersioned = registeredCustomPropertySet.getCustomPropertySet().isVersioned();
    }

    public void addAttributes(List<PropertySpec> propertySpecs) {
        List<DeviceTypeCustomPropertySetAttributeInfo> deviceTypeCustomPropertySetAttributeInfos = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            deviceTypeCustomPropertySetAttributeInfos.add(new DeviceTypeCustomPropertySetAttributeInfo(propertySpec));
        }
        this.attributes = deviceTypeCustomPropertySetAttributeInfos;
    }

    public void addProperties(List<PropertyInfo> properties) {
        this.properties = properties;
    }

    public static List<DeviceTypeCustomPropertySetInfo> from(List<RegisteredCustomPropertySet> rcpss) {
        List<DeviceTypeCustomPropertySetInfo> deviceTypeCustomPropertySetInfos = new ArrayList<>();
        for (RegisteredCustomPropertySet rcps : rcpss) {
            DeviceTypeCustomPropertySetInfo deviceTypeCPSInfo = new DeviceTypeCustomPropertySetInfo(rcps);
            deviceTypeCPSInfo.addAttributes(rcps.getCustomPropertySet().getPropertySpecs());
            deviceTypeCustomPropertySetInfos.add(deviceTypeCPSInfo);
        }
        return deviceTypeCustomPropertySetInfos;
    }

    public static List<DeviceTypeCustomPropertySetInfo> from(Map<RegisteredCustomPropertySet, List<PropertyInfo>> rcpss) {
        List<DeviceTypeCustomPropertySetInfo> deviceTypeCustomPropertySetInfos = new ArrayList<>();
        for (Map.Entry<RegisteredCustomPropertySet, List<PropertyInfo>> rcps : rcpss.entrySet()) {
            DeviceTypeCustomPropertySetInfo deviceTypeCPSInfo = new DeviceTypeCustomPropertySetInfo(rcps.getKey());
            deviceTypeCPSInfo.addProperties(rcps.getValue());
            deviceTypeCustomPropertySetInfos.add(deviceTypeCPSInfo);
        }
        return deviceTypeCustomPropertySetInfos;
    }
}