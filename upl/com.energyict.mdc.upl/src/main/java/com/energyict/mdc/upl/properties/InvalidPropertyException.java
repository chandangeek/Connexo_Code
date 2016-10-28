/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl.properties;

/**
 * Thrown when a meter protocol specific parameter has an invalid value.
 *
 * @author Karel
 */
public class InvalidPropertyException extends PropertyValidationException {

    public InvalidPropertyException() {
        super();
    }

    public InvalidPropertyException(String msg) {
        super(msg);
    }

}