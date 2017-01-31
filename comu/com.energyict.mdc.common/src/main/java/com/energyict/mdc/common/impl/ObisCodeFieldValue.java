/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.impl;

/**
 * @author Koen
 */
public class ObisCodeFieldValue {

    private int code;
    private StringBuilder descriptionBuilder;

    public ObisCodeFieldValue(int code, String description) {
        this.code = code;
        this.descriptionBuilder = new StringBuilder(description);
    }

    /**
     * Getter for property code.
     *
     * @return Value of property code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Getter for property description.
     *
     * @return Value of property description.
     */
    public java.lang.String getDescription() {
        return this.descriptionBuilder.toString();
    }

    public String toString() {
        return getDescription();
    }

    public void add2Description(String toAdd) {
        this.descriptionBuilder.append(toAdd);
    }

}
