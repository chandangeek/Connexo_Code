/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
