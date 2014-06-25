package com.elster.jupiter.properties;

public class InvalidValueException extends java.lang.Exception {

    private static final long serialVersionUID = 1L;

    private String messageId;
    private String defaultPattern;
    private Object[] arguments;

    private InvalidValueException(String messageId, String defaultPattern, Object[] arguments) {
        this.messageId = messageId;
        this.defaultPattern = defaultPattern;
        this.arguments = arguments;
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
        return arguments;
    }

    public String getDefaultPattern() {
        return defaultPattern;
    }
}