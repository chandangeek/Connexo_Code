package com.energyict.mdc.common;

/**
 * Models the exceptional situation that occurs when <code>null</code>
 * was specified as a value for a required attribute or property.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-18 (13:01)
 */
public class ValueRequiredException extends InvalidValueException {

    /**
     * Constructs a new ValueRequiredException that reports that
     * the specified property should have a value, i.e. <code>null</code>
     * is not a valid value.
     *
     * @param propertyName The identifier of the property that does not support <code>null</code>
     */
    public ValueRequiredException (String propertyName) {
        super("XcannotBeEmpty", "\"{0}\" is a required property", propertyName);
    }

    /**
     * Constructs a new ValueRequiredException that reports that
     * the specified property should have a value, i.e. <code>null</code>
     * is not a valid value.
     *
     * @param messageId The String in the resource bundle that produces a human readable
     *                  explanation of this error message
     * @param defaultPattern The default human readable explanation should the actual
     *                       not be found in the resource bundle
     * @param propertyName The identifier of the property that does not support <code>null</code>
     */
    public ValueRequiredException (String messageId, String defaultPattern, String propertyName) {
        super(messageId, defaultPattern, propertyName);
    }

    /**
     * Constructs a new ValueRequiredException that reports that
     * the specified property should have a value, i.e. <code>null</code>
     * is not a valid value.
     *
     * @param messageId The String in the resource bundle that produces a human readable
     *                  explanation of this error message
     * @param defaultPattern The default human readable explanation should the actual
     *                       not be found in the resource bundle
     * @param propertyName The identifier of the property that does not support <code>null</code>
     * @param context Additional context information that will be copied to the defaultPattern
     */
    public ValueRequiredException (String messageId, String defaultPattern, String propertyName, Object context) {
        super(messageId, defaultPattern, propertyName, context);
    }

}