package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

/**
 * Provides functionality to create exceptions based on the configuration of a Device
 *
 * @author gna
 * @since 29/03/12 - 12:50
 */
public final class DeviceConfigurationException extends ComServerRuntimeException {


    /**
     * Constructs a new DeviceConfigurationException identified by the {@link ExceptionCode}.
     *
     * @param code             The ExceptionCode
     * @param messageArguments A sequence of values for the arguments of the human readable description
     *                         that is associated with the ExceptionCode
     */
    private DeviceConfigurationException(ExceptionCode code, Object... messageArguments) {
        super(code, messageArguments);
    }

    /**
     * Creates a {@link DeviceConfigurationException} indicating the configuration of a Device could not be fetched.
     *
     * @param loadProfileObisCode the <code>ObisCode</code> of the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile LoadProfile}
     * @return the newly created config-not-accessible exception
     */
    public static DeviceConfigurationException notAccessible(final ObisCode loadProfileObisCode) {
        return new DeviceConfigurationException(generateExceptionCodeByReference(CommonExceptionReferences.CONFIG_NOT_ACCESSIBLE), loadProfileObisCode);
    }

    /**
     * Creates a {@link DeviceConfigurationException} indicating the given channelName is not convertible to a proper ObisCode. You should change this in
     * the Protocol implementation
     *
     * @param channelName the name which was given to the {@link com.energyict.protocol.ChannelInfo} object
     * @return the newly created channelInfo-name-is-not-an-obiscode exception
     */
    public static DeviceConfigurationException channelNameNotAnObisCode(final String channelName) {
        return new DeviceConfigurationException(generateExceptionCodeByReference(CommonExceptionReferences.CONFIG_CHANNEL_NAME_NOT_OBISCODE), channelName);
    }

    /**
     * Creates a {@link DeviceConfigurationException} indicating the serialNumber which was fetched from the device does not match the serialNumber which is configured
     * in the HeadEnd system.
     *
     * @param meterSerialNumber      the serialNumber which was read from the Device
     * @param configuredSerialNumber the serialNumber configured in the HeadEnd system
     * @return the newly created serialNumber-mismatch exception
     */
    public static DeviceConfigurationException serialNumberMisMatch(final String meterSerialNumber, final String configuredSerialNumber) {
        return new DeviceConfigurationException(generateExceptionCodeByReference(CommonExceptionReferences.CONFIG_SERIAL_NUMBER_MISMATCH), meterSerialNumber, configuredSerialNumber);
    }

    /**
     * Creates a {@link DeviceConfigurationException} indicating the time difference between the Collection Software and the Meter exceeds the maximum allowed time difference.
     *
     * @param actualTimeDifference  the actual difference in time between the Collection Software and the Meter, expressed in milliseconds
     * @param maximumTimeDifference the maximum allowed time difference, expressed in milliseconds
     * @return the newly created timDifference-exceeded exception
     */
    public static DeviceConfigurationException timeDifferenceExceeded(final long actualTimeDifference, final long maximumTimeDifference) {
        return new DeviceConfigurationException(generateExceptionCodeByReference(CommonExceptionReferences.MAXIMUM_TIME_DIFFERENCE_EXCEEDED), actualTimeDifference, maximumTimeDifference);
    }

    /**
     * Creates a {@link DeviceConfigurationException} indicating that a property was filled in but the format was unexpected
     *
     * @param propertyName   the name of the property
     * @param propertyValue  the value of the property
     * @param expectedFormat the expected format of the property
     */
    public static DeviceConfigurationException invalidPropertyFormat(final String propertyName, final String propertyValue, final String expectedFormat) {
        return new DeviceConfigurationException(generateExceptionCodeByReference(CommonExceptionReferences.INVALID_PROPERTY_FORMAT), propertyName, propertyValue, expectedFormat);
    }

    /**
     * Creates a {@link DeviceConfigurationException} indicating a protocol received an unexpected com channel.
     * This usually means a wrong connectiontype was configured.
     */
    public static DeviceConfigurationException unexpectedComChannel(final String expectedComChannel, final String actualComChannel) {
        return new DeviceConfigurationException(generateExceptionCodeByReference(CommonExceptionReferences.UNEXPECTED_COM_CHANNEL), expectedComChannel, actualComChannel);
    }

    /**
     * Generate an <code>ExceptionCode</code> based on the given <code>ComServerExecutionExceptionReferences</code>
     *
     * @param reference the {@link ExceptionCode#reference reference} to use in the <code>ExceptionCode</code>
     * @return the newly created <code>ExceptionCode</code>
     */
    private static ExceptionCode generateExceptionCodeByReference(CommonExceptionReferences reference) {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CONFIGURATION, reference);
    }

}
