/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.properties.rest.PropertyInfo;

import java.util.List;

public class ServiceCallCustomPropertySetInfo {
    public long id;
    public String name;
    public boolean editable;
    public List<PropertyInfo> properties;

    public ServiceCallCustomPropertySetInfo() {
    }

    public ServiceCallCustomPropertySetInfo(RegisteredCustomPropertySet registeredCustomPropertySet, List<PropertyInfo> properties) {
        this.id = registeredCustomPropertySet.getId();
        this.name = registeredCustomPropertySet.getCustomPropertySet().getName();
        this.editable = false;
        this.properties = properties;
    }

}
