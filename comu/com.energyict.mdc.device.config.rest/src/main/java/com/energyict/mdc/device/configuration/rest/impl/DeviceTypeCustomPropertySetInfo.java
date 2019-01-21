/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;

import com.energyict.mdc.device.config.DeviceType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceTypeCustomPropertySetInfo {

    public long id;
    public String name;
    public String deviceTypeName;
    public String domainName;
    public long deviceTypeVersion;
    public boolean isVersioned;
    public boolean editable;
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
        this.editable = registeredCustomPropertySet.isEditableByCurrentUser();
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

    public void addVersion(DeviceType deviceType) {
        this.deviceTypeName = deviceType.getName();
        this.deviceTypeVersion = deviceType.getVersion();
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

    public static List<DeviceTypeCustomPropertySetInfo> from(DeviceType deviceType,
                                                             List<RegisteredCustomPropertySet> rcpss) {
        List<DeviceTypeCustomPropertySetInfo> deviceTypeCPSInfos = from(rcpss);
        deviceTypeCPSInfos.forEach(deviceTypeCPSInfo -> deviceTypeCPSInfo.addVersion(deviceType));
        return deviceTypeCPSInfos;
    }

    public static DeviceTypeCustomPropertySetInfo from(DeviceType deviceType,
                                                       RegisteredCustomPropertySet rcps,
                                                       List<PropertyInfo> properties) {
        DeviceTypeCustomPropertySetInfo deviceTypeCPSInfo = new DeviceTypeCustomPropertySetInfo(rcps);
        deviceTypeCPSInfo.addProperties(properties);
        deviceTypeCPSInfo.addVersion(deviceType);
        return deviceTypeCPSInfo;
    }

    public static List<DeviceTypeCustomPropertySetInfo> from(DeviceType deviceType,
                                                             Map<RegisteredCustomPropertySet, List<PropertyInfo>> rcpss) {
        List<DeviceTypeCustomPropertySetInfo> deviceTypeCustomPropertySetInfos = new ArrayList<>();
        for (Map.Entry<RegisteredCustomPropertySet, List<PropertyInfo>> rcps : rcpss.entrySet()) {
            deviceTypeCustomPropertySetInfos.add(from(deviceType, rcps.getKey(), rcps.getValue()));
        }
        return deviceTypeCustomPropertySetInfos;
    }
}