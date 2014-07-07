package com.energyict.mdc.device.config.impl;

/**
 * Defines constants for the maximum length of String properties
 * of entities of the device type and configuration bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-07 (15:53)
 */
public class StringColumnLengthConstraints {

    public static final int SHORT_NAME_LENGTH = 80;
    public static final int LONG_NAME_LENGTH = 255;
    public static final int DEFAULT_DESCRIPTION_LENGTH = 4000;

    public static final int DEVICE_TYPE_NAME = SHORT_NAME_LENGTH;
    public static final int DEVICE_TYPE_DESCRIPTION = DEFAULT_DESCRIPTION_LENGTH;

    public static final int DEVICE_CONFIGURATION_NAME = SHORT_NAME_LENGTH;
    public static final int DEVICE_CONFIGURATION_DESCRIPTION = DEFAULT_DESCRIPTION_LENGTH;

    public static final int CHANNEL_SPEC_NAME = SHORT_NAME_LENGTH;

    public static final int PARTIAL_CONNECTION_TASK_NAME = LONG_NAME_LENGTH;
    public static final int SECURITY_ROPERTY_SET_NAME = LONG_NAME_LENGTH;
    public static final int PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_NAME = LONG_NAME_LENGTH;

}