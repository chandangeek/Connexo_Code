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

    public static InvalidPropertyException forNameAndValue(String propertyName, Object value) {
        return new InvalidPropertyException(String.valueOf(value) + " is not a valid value for property " + propertyName);
    }

    public static InvalidPropertyException forNameAndValue(String propertyName, Object value, Exception cause) {
        return new InvalidPropertyException(cause, String.valueOf(value) + " is not a valid value for property " + propertyName);
    }

    public InvalidPropertyException(String message) {
        super(message);
    }

    public InvalidPropertyException(Exception e, String msg) {
        super(e, msg);
    }

}