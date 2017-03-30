/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.properties.PropertySpec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceTypeCustomPropertySetAttributeInfo {

    public String name;
    public boolean required;

    public DeviceTypeCustomPropertySetAttributeInfo() {
    }

    public DeviceTypeCustomPropertySetAttributeInfo(PropertySpec propertySpec) {
        this();
        this.name = propertySpec.getName();
        this.required = propertySpec.isRequired();
    }
}