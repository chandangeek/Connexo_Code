/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

public class LastSeenDateInfo {

    /**
     * The name of the LastSeenDate general property
     */
    private String propertyName;

    /**
     * The value of the LastSeenDate general property
     */
    private Object propertyValue;

    public LastSeenDateInfo(String propertyName, Object propertyValue) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Object getPropertyValue() {
        return propertyValue;
    }
}