/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.util.exception.MessageSeed;

public class InvalidValueException extends Exception {

    private static final long serialVersionUID = 1L;

    private String messageId;
    private String defaultPattern;
    private Object[] arguments;

    private InvalidValueException(String messageId, String defaultPattern, Object[] arguments) {
        this.messageId = messageId;
        this.defaultPattern = defaultPattern;
        this.arguments = arguments;
    }

    public InvalidValueException(MessageSeed messageSeed, String propertyName, Object value) {
        this(messageSeed.getKey(), messageSeed.getDefaultFormat(), new Object[] { propertyName, value });
    }

    public InvalidValueException(String messageId, String defaultPattern, String propertyName) {
        this(messageId, defaultPattern, new Object[] { propertyName });
    }

    public InvalidValueException(String messageId, String defaultPattern, String propertyName, Object value) {
        this(messageId, defaultPattern, new Object[] { propertyName, value });
    }

    public InvalidValueException(String messageId, String defaultPattern, String propertyName, Object value, String reason) {
        this(messageId, defaultPattern, new Object[] { propertyName, value, reason });
    }

    public InvalidValueException(String messageId, String defaultPattern, String propertyName, Object rangeStart, Object rangeEnd) {
        this(messageId, defaultPattern, new Object[] { propertyName, rangeStart, rangeEnd });
    }

    public String getMessageId() {
        return messageId;
    }

    public Object[] getArguments() {
        Object[] copied = new Object[this.arguments.length];
        System.arraycopy(this.arguments, 0, copied, 0, this.arguments.length);
        return copied;
    }

    public String getDefaultPattern() {
        return defaultPattern;
    }

}