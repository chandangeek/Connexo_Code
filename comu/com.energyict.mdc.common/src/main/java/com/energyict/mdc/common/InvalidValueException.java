package com.energyict.mdc.common;

/**
 * Represents an invalid value exception.
 * Usefull to tell the user that he entered an invalid value for a specific attribute:
 *
 * e.g. the interval of a channel can't be negative:
 * the Errormessage can be: "The interval can't be negative"
 *
 * new InvalidValueException("XcannotbeNegative", "\"{0}\" cannot be negative", "interval")
 * "interval" must be a key in the language resource bundle

 * @author Pasquien
 * @since 28 oktober 2004, 17:36
 */
public class InvalidValueException extends BusinessException {

    private String fieldId;

    /**
     * Constructs a new InvalidValueException.
     *
     * @param messageId localization key
     * @param defaultPattern default pattern
     * @param fieldId localization key for attribute
     */
    public InvalidValueException(String messageId, String defaultPattern, String fieldId) {
        super(messageId, defaultPattern, getFieldName(fieldId));
        this.fieldId = fieldId;
    }

    /**
     * Constructs a new InvalidValueException that informs
     * the user that the specified value is not a valid
     * value for the specified property.
     *
     * @param messageId localization key
     * @param defaultPattern default pattern
     * @param propertyName localization key for attribute
     * @param value The invalid value
     */
    public InvalidValueException(String messageId, String defaultPattern, String propertyName, Object value) {
        super(messageId, defaultPattern, getFieldName(propertyName), value);
        fieldId=propertyName;
    }

    /**
     * Constructs a new InvalidValueException that informs
     * the user that the specified value is not a valid
     * value for the specified property.
     *
     * @param messageId localization key
     * @param defaultPattern default pattern
     * @param propertyName localization key for attribute
     * @param value The invalid value
     * @param reason The reason why the value is not valid
     */
    public InvalidValueException(String messageId, String defaultPattern, String propertyName, Object value, String reason) {
        super(messageId, defaultPattern, getFieldName(propertyName), value, reason);
        fieldId=propertyName;
    }

    /**
     * Constructs a new InvalidValueException that informs
     * the user that a value is not in the acceptable
     * range for the specified property.
     *
     * @param messageId localization key
     * @param defaultPattern default pattern
     * @param propertyName localization key for attribute
     * @param rangeStart The start of the acceptable range
     * @param rangeEnd The end of the acceptable range
     */
    public InvalidValueException(String messageId, String defaultPattern, String propertyName, Object rangeStart, Object rangeEnd) {
        super(messageId, defaultPattern, getFieldName(propertyName), rangeStart, rangeEnd);
        fieldId = propertyName;
    }

    /**
    * Returns the message pattern for the given key.
     *
    * @param fieldId message key
    * @return localized message pattern
    */
    private static String getFieldName (String fieldId) {
        return fieldId;
    }

    public String getFieldId() {
        return fieldId;
    }
}