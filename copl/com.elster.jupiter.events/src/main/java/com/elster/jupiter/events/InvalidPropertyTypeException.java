/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Thrown when a property reached through an access path does not have the expected type.
 */
public class InvalidPropertyTypeException extends LocalizedException {

	private static final long serialVersionUID = 1L;

	public InvalidPropertyTypeException(Thesaurus thesaurus, Object bean, String accessPath, Class<?> expectedType, Class<?> actualType) {
        super(thesaurus, MessageSeeds.INVALID_PROPERTY_TYPE, bean, accessPath, expectedType, actualType);
        set("bean", bean);
        set("accessPath", accessPath);
        set("expectedType", expectedType);
        set("actualType", actualType);
    }
}
