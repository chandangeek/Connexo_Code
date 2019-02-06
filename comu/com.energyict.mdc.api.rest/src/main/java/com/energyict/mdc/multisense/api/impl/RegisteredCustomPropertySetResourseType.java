/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.DeviceType;

import java.util.Optional;

public enum RegisteredCustomPropertySetResourseType {

    DEVICE_TYPE_CPS(DeviceType.class, DeviceTypeResource.class, "getDeviceTypeCustomPropertySet"),

    ;

    private final Class<?> domainClass;
    private final Class<?> resourceClass;
    private final String methodName;

    RegisteredCustomPropertySetResourseType(Class<?> domainClass, Class<?> resourceClass, String methodName) {
        this.domainClass = domainClass;
        this.resourceClass = resourceClass;
        this.methodName = methodName;
    }

    public Class<?> getResourceClass() {
        return resourceClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public static Optional<RegisteredCustomPropertySetResourseType> fromClass(Class<?> cls) {
        for(RegisteredCustomPropertySetResourseType value : values()) {
            if(value.domainClass == cls)
                return Optional.of(value);
        }
        return Optional.empty();
    }
}