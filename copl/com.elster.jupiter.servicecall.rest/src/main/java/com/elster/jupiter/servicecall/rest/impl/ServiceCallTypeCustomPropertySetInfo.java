package com.elster.jupiter.servicecall.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.properties.PropertySpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceCallTypeCustomPropertySetInfo {

    public long id;
    public String name;
    public List<ServiceCallTypeCustomPropertySetAttributeInfo> attributes;
    public ServiceCallTypeCustomPropertySetInfo() {

    }

    public ServiceCallTypeCustomPropertySetInfo(RegisteredCustomPropertySet registeredCustomPropertySet) {
        this.id = registeredCustomPropertySet.getId();
        this.name = registeredCustomPropertySet.getCustomPropertySet().getName();
        this.attributes = getAttributes(registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs());
    }

    public static List<ServiceCallTypeCustomPropertySetAttributeInfo> getAttributes(List<PropertySpec> propertySpecs) {
        List<ServiceCallTypeCustomPropertySetAttributeInfo> serviceCallTypeCustomPropertySetAttributeInfos = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            serviceCallTypeCustomPropertySetAttributeInfos.add(new ServiceCallTypeCustomPropertySetAttributeInfo(propertySpec));
        }
        return serviceCallTypeCustomPropertySetAttributeInfos;
    }

    public static List<ServiceCallTypeCustomPropertySetInfo> from(List<RegisteredCustomPropertySet> registeredCustomPropertySets) {
        List<ServiceCallTypeCustomPropertySetInfo> serviceCallTypeCustomPropertySetInfos = new ArrayList<>();
        for (RegisteredCustomPropertySet registeredCustomPropertySet : registeredCustomPropertySets) {
            serviceCallTypeCustomPropertySetInfos.add(new ServiceCallTypeCustomPropertySetInfo(registeredCustomPropertySet));
        }
        return serviceCallTypeCustomPropertySetInfos;
    }
}

