package com.elster.jupiter.cps.rest.impl;

import com.elster.jupiter.util.units.Quantity;

public enum PropertyType implements com.elster.jupiter.properties.rest.PropertyType {

    QUANTITY(Quantity.class)
    ;

    private Class typeClass;

    PropertyType(Class typeClass) {
        this.typeClass = typeClass;
    }
}