/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.rest.PropertyType;

/**
 * Created by bvn on 2/2/17.
 */
public enum SimplePropertyType implements PropertyType {
    IDWITHNAME(SecurityAccessorType.class, true),
    ;

    private Class discriminatorClass;
    private boolean isReference;

    SimplePropertyType(Class discriminatorClass, boolean reference) {
        this.isReference = reference;
        this.discriminatorClass = discriminatorClass;
    }

    public boolean isReference() {
        return isReference;
    }

}
