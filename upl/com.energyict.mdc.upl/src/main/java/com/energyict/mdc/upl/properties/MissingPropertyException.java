/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl.properties;

/**
 * Thrown when a required protocol specific parameter is not present.
 *
 * @author Karel
 */
public class MissingPropertyException extends PropertyValidationException {

    public MissingPropertyException() {
        super();
    }

    public MissingPropertyException(String msg) {
        super(msg);
    }

}