/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.properties.rest.PropertyInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class OverriddenPropertyInfo {

    @JsonUnwrapped
    public PropertyInfo propertyInfo;
    public boolean canBeOverridden;
    public boolean overridden;

    public OverriddenPropertyInfo() {
    }

    public OverriddenPropertyInfo(PropertyInfo propertyInfo, boolean canBeOverridden, boolean overridden) {
        this();
        this.propertyInfo = propertyInfo;
        this.canBeOverridden = canBeOverridden;
        this.overridden = overridden;
    }
}
