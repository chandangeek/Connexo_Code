package com.energyict.protocol.exceptions;

import java.io.IOException;

/**
 * Provides functionality to create exceptions based on the configuration of a Device
 *
 * @author gna
 * @since 29/03/12 - 12:50
 */
public final class DeviceConfigurationException extends ProtocolRuntimeException {

    protected DeviceConfigurationException(Throwable cause, ProtocolExceptionReference code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

    protected DeviceConfigurationException(ProtocolExceptionReference reference, Object... messageArguments) {
        super(reference, messageArguments);
    }

    private DeviceConfigurationException(ProtocolExceptionReference reference, Exception cause) {
        super(cause, reference, cause.getMessage());
    }

    /**
     * Creates a {@link DeviceConfigurationException} indicating that a property was filled in but the format was unexpected
     *
     * @param propertyName   the name of the property
     * @param propertyValue  the value of the property
     * @param expectedFormat the expected format of the property
     */
    public static DeviceConfigurationException invalidPropertyFormat(final String propertyName, final String propertyValue, final String expectedFormat) {
        return new DeviceConfigurationException(ProtocolExceptionReference.INVALID_PROPERTY_FORMAT, propertyName, propertyValue, expectedFormat);
    }

    /**
     * Creates a {@link DeviceConfigurationException} indicating that a required property was not filled in
     *
     * @param propertyName the name of the required property
     */
    public static DeviceConfigurationException missingProperty(final String propertyName) {
        return new DeviceConfigurationException(ProtocolExceptionReference.MISSING_PROPERTY, propertyName);
    }

    /**
     * Creates a {@link DeviceConfigurationException} indicating that a required property was not filled in for a specific device
     *
     * @param propertyName the name of the required property
     */
    public static DeviceConfigurationException missingProperty(final String propertyName, String deviceIdentifier) {
        return new DeviceConfigurationException(ProtocolExceptionReference.MISSING_PROPERTY_FOR_DEVICE, propertyName, deviceIdentifier);
    }

    /**
     * Creates a {@link DeviceConfigurationException} indicating that a property was filled in correctly, but the value is not supported by the protocol.
     *
     * @param propertyName  the name of the property
     * @param propertyValue the value of the property
     */
    public static DeviceConfigurationException unsupportedPropertyValue(final String propertyName, final String propertyValue) {
        return new DeviceConfigurationException(ProtocolExceptionReference.INVALID_PROPERTY_VALUE, propertyName, propertyValue);
    }

    /**
     * Creates a {@link DeviceConfigurationException} indicating a protocol received an unexpected com channel.
     * This usually means a wrong connectiontype was configured.
     */
    public static DeviceConfigurationException unexpectedComChannel(final String expectedComChannel, final String actualComChannel) {
        return new DeviceConfigurationException(ProtocolExceptionReference.UNEXPECTED_COM_CHANNEL, expectedComChannel, actualComChannel);
    }

    /**
     * Creates a {@link DeviceConfigurationException} indicating it is not allowed to execute the given command
     * Use for example when trying to execute a three-phase command on a mono-phase configured device or
     * when trying to modify parts of the device config restricted by higher security rights
     *
     * @param command the name of the not allowed command
     * @param cause   The cause of the error
     * @return the newly created timeDifference-exceeded exception
     */
    public static DeviceConfigurationException notAllowedToExecuteCommand(final String command, final IOException cause) {
        return new DeviceConfigurationException(cause, ProtocolExceptionReference.NOT_ALLOWED_TO_EXECUTE_COMMAND, command, cause.getMessage());
    }
}