package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.properties.PropertySpec;

public class ServiceCallTypeCustomPropertySetAttributeInfo {
    public String name;

    public ServiceCallTypeCustomPropertySetAttributeInfo() {
    }

    public ServiceCallTypeCustomPropertySetAttributeInfo(PropertySpec propertySpec) {
        this();
        this.name = propertySpec.getName();
    }
}
