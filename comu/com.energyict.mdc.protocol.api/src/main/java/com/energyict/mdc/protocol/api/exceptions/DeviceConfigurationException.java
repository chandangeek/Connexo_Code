/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.protocol.api.MessageSeeds;

import java.io.IOException;

/**
 * Provides functionality to create exceptions based on the configuration of a Device
 *
 * @author gna
 * @since 29/03/12 - 12:50
 */
public class DeviceConfigurationException extends ComServerRuntimeException {


    /**
     * Constructs a new DeviceConfigurationException identified by the {@link MessageSeed}.
     *
     * @param messageSeed The MessageSeed
     * @param messageArguments A sequence of values for the arguments of the human readable description
     * that is associated with the MessageSeed
     */
    public DeviceConfigurationException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    /**
     * Creates a {@link DeviceConfigurationException} indicating the serialNumber which was fetched from the device does not match the serialNumber which is configured
     * in the HeadEnd system.
     *
     * @param meterSerialNumber the serialNumber which was read from the Device
     * @param configuredSerialNumber the serialNumber configured in the HeadEnd system
     * @return the newly created serialNumber-mismatch exception
     */
    public static SerialNumberMismatchException serialNumberMisMatch(String meterSerialNumber, String configuredSerialNumber, MessageSeed messageSeed) {
        return new SerialNumberMismatchException(messageSeed, meterSerialNumber, configuredSerialNumber);
    }

    /**
     * Creates a {@link DeviceConfigurationException} indicating the time difference between the Collection Software and the Meter exceeds the maximum allowed time difference.
     *
     * @param actualTimeDifference the actual difference in time between the Collection Software and the Meter, expressed in milliseconds
     * @param maximumTimeDifference the maximum allowed time difference, expressed in milliseconds
     * @return the newly created timDifference-exceeded exception
     */
    public static TimeDifferenceExceededException timeDifferenceExceeded(MessageSeed messageSeed, long actualTimeDifference, long maximumTimeDifference) {
        return new TimeDifferenceExceededException(messageSeed, actualTimeDifference, maximumTimeDifference);
    }

    /**
     * Creates a {@link DeviceConfigurationException} indicating that a required property was not filled in
     *
     * @param propertyName the name of the required property
     */
    public static DeviceConfigurationException missingProperty(final String propertyName) {
        return new DeviceConfigurationException(MessageSeeds.MISSING_PROPERTY, propertyName);
    }

    public static DeviceConfigurationException unsupportedPropertyValue(String key, String value) {
        return new DeviceConfigurationException(MessageSeeds.INVALID_PROPERTY_VALUE, key, value);
    }

    public static DeviceConfigurationException invalidPropertyFormat(String key, String value, String s) {
        return unsupportedPropertyValue(key, value);
    }

    public static DeviceConfigurationException notAllowedToExecuteCommand(String s, IOException cause) {
        return new DeviceConfigurationException(MessageSeeds.COMMAND_NOT_SUPPORTED, s);
    }
}
