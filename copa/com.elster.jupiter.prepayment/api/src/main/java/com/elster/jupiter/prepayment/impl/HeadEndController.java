package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

import javax.inject.Inject;
import java.sql.Date;
import java.time.Instant;

/**
 * This class contains the controller who will translate request coming from our internal Prepayment code
 * to the proper FullDuplex commands
 *
 * @author sva
 * @since 31/03/2016 - 15:04
 */
public class HeadEndController {

    private static final boolean ARM_FOR_CLOSURE = false;
    private static final String UNDEFINED_UNIT = "undefined";

    private final ExceptionFactory exceptionFactory;

    @Inject
    public HeadEndController(ExceptionFactory exceptionFactory) {
        this.exceptionFactory = exceptionFactory;
    }

    public void performContactorOperations(EndDevice endDevice, ServiceCall serviceCall, ContactorInfo contactorInfo) {
        HeadEndInterface headEndInterface = getHeadEndInterface(endDevice);
        performBreakerOperations(endDevice, headEndInterface, serviceCall, contactorInfo);
        performLoadLimitOperations(endDevice, headEndInterface, serviceCall, contactorInfo);
    }

    private void performBreakerOperations(EndDevice endDevice, HeadEndInterface headEndInterface, ServiceCall serviceCall, ContactorInfo contactorInfo) {
        if (shouldPerformBreakerOperations(contactorInfo)) {
            serviceCall.log(LogLevel.INFO, "Handling breaker operations - the breaker will be " + contactorInfo.status.getDescription() + ".");
            EndDeviceCommand deviceCommand;
            switch (contactorInfo.status) {
                case connected:
                    deviceCommand = headEndInterface.getCommandFactory().createConnectCommand(endDevice);
                    break;
                case disconnected:
                    deviceCommand = headEndInterface.getCommandFactory().createDisconnectCommand(endDevice);
                    break;
                case armed:
                    deviceCommand = headEndInterface.getCommandFactory().createArmCommand(endDevice, ARM_FOR_CLOSURE);
                    break;
                default:
                    throw exceptionFactory.newException(MessageSeeds.UNKNOWN_STATUS);
            }

            deviceCommand.setPropertyValue(getCommandArgumentSpec(deviceCommand, DeviceMessageConstants.contactorActivationDateAttributeName), Date.from(contactorInfo.activationDate));
            headEndInterface.sendCommand(deviceCommand, contactorInfo.activationDate, serviceCall); // No need to do something with the CompletionOptions returned by the #sendCommand method
            // The parent service call is already notified of child process by AbstractContactorOperationServiceCallHandler#onChildStateChange
        }
    }

    private PropertySpec getCommandArgumentSpec(EndDeviceCommand endDeviceCommand, String commandArgumentName) {
        return endDeviceCommand.getCommandArgumentSpecs().stream()
                .filter(propertySpec -> propertySpec.getName().equals(commandArgumentName))
                .findFirst().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_HEAD_END_INTERFACE));
    }

    private int performLoadLimitOperations(EndDevice endDevice, HeadEndInterface headEndInterface, ServiceCall serviceCall, ContactorInfo contactorInfo) {
        int nrOfDeviceCommands = 0;
        if (shouldPerformLoadLimitOperations(contactorInfo)) {
            serviceCall.log(LogLevel.INFO, "Handling load limitation operations.");
            EndDeviceCommand deviceCommand;
            if (contactorInfo.loadLimit.shouldDisableLoadLimit()) {
                deviceCommand = headEndInterface.getCommandFactory().createDisableLoadLimitCommand(endDevice);
            } else {
                deviceCommand = headEndInterface.getCommandFactory().createEnableLoadLimitCommand(endDevice);
                deviceCommand.setPropertyValue(getCommandArgumentSpec(deviceCommand, DeviceMessageConstants.normalThresholdAttributeName), contactorInfo.loadLimit.limit);
                deviceCommand.setPropertyValue(getCommandArgumentSpec(deviceCommand, DeviceMessageConstants.unitAttributeName),
                        (contactorInfo.loadLimit.unit == null || contactorInfo.loadLimit.unit.isEmpty()) ? UNDEFINED_UNIT : contactorInfo.loadLimit.unit
                );
                if (contactorInfo.loadTolerance != null) {
                    deviceCommand.setPropertyValue(getCommandArgumentSpec(deviceCommand, DeviceMessageConstants.overThresholdDurationAttributeName), TimeDuration.seconds(contactorInfo.loadTolerance));
                }
            }
            headEndInterface.sendCommand(deviceCommand, Instant.now(), serviceCall);
        }
        return nrOfDeviceCommands;
    }

    private boolean shouldPerformBreakerOperations(ContactorInfo contactorInfo) {
        return contactorInfo.status != null;
    }

    private boolean shouldPerformLoadLimitOperations(ContactorInfo contactorInfo) {
        return contactorInfo.loadLimit != null;
    }

    private HeadEndInterface getHeadEndInterface(EndDevice endDevice) {
        return endDevice.getHeadEndInterface().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_HEAD_END_INTERFACE, endDevice.getMRID()));
    }
}