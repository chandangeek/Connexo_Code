/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap;

import com.elster.jupiter.util.exception.BaseException;

/**
 * Exception to be thrown when a required property was not found.
 *
 * Note that the constructor takes the property key, and not a message.
 *
 */
public class PropertyNotFoundException extends BaseException {
	private static final long serialVersionUID = 1L;

    /**
     * @param propertyKey key of the property
     */
    public PropertyNotFoundException(String propertyKey) {
        super(MessageSeeds.PROPERTY_NOT_FOUND, propertyKey);
        set("propertyKey", propertyKey);
    }
}
