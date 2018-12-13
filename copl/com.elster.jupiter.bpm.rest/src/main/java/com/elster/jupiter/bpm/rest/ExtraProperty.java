/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

public class ExtraProperty {
    public String propertyName;
    public Object propertyValue;

    public ExtraProperty() {

    }

    public ExtraProperty(String name, Object value) {
        propertyName = name;
        propertyValue = value;
    }
}
