/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

public class ValueRequiredException extends InvalidValueException {

    public ValueRequiredException(String propertyName) {
        super("XcannotBeEmpty", "\"{0}\" is a required property", propertyName);
    }

    public ValueRequiredException(String messageId, String defaultPattern, String propertyName) {
        super(messageId, defaultPattern, propertyName);
    }

    public ValueRequiredException(String messageId, String defaultPattern, String propertyName, Object context) {
        super(messageId, defaultPattern, propertyName, context);
    }
}
