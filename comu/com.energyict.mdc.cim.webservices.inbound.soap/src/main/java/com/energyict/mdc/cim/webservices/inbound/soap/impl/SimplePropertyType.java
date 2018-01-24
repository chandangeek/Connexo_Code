/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.properties.rest.PropertyType;

import com.energyict.obis.ObisCode;

public enum SimplePropertyType implements PropertyType {
    OBISCODE(ObisCode.class),
    ;

    private Class discriminatorClass;
    private boolean isReference;

    SimplePropertyType(Class valueFactoryClass) {
        this(valueFactoryClass, false);
    }

    SimplePropertyType(Class discriminatorClass, boolean reference) {
        this.isReference = reference;
        this.discriminatorClass = discriminatorClass;
    }

    public boolean isReference() {
        return isReference;
    }
}
