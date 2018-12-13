/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.properties.PropertySpec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceCallTypeCustomPropertySetInfo {

    public long id;
    public String name;
    public boolean active;
    public List<ServiceCallTypeCustomPropertySetAttributeInfo> attributes;

    public ServiceCallTypeCustomPropertySetInfo() {
    }

    ServiceCallTypeCustomPropertySetInfo(RegisteredCustomPropertySet registeredCustomPropertySet) {
        this.id = registeredCustomPropertySet.getId();
        if (registeredCustomPropertySet.isActive()) {
            this.active = true;
            this.name = registeredCustomPropertySet.getCustomPropertySet().getName();
            this.attributes = getAttributes(registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs());
        } else {
            this.active = false;
            this.name = registeredCustomPropertySet.getCustomPropertySetId();
            this.attributes = Collections.emptyList();
        }
    }

    private List<ServiceCallTypeCustomPropertySetAttributeInfo> getAttributes(List<PropertySpec> propertySpecs) {
        List<ServiceCallTypeCustomPropertySetAttributeInfo> serviceCallTypeCustomPropertySetAttributeInfos = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            serviceCallTypeCustomPropertySetAttributeInfos.add(new ServiceCallTypeCustomPropertySetAttributeInfo(propertySpec));
        }
        return serviceCallTypeCustomPropertySetAttributeInfos;
    }

}