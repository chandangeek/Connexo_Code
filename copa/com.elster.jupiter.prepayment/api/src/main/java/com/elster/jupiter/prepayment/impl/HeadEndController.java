/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.units.Quantity;
import com.energyict.mdc.common.Unit;

import javax.inject.Inject;
import java.time.Instant;

/**
 * This class contains the controller who will translate request coming from our internal Prepayment code
 * to the proper FullDuplex commands
 *
 * @author sva
 * @since 31/03/2016 - 15:04
 */
public class HeadEndController {

    private static final String OVER_THRESHOLD_DURATION_ATTRIBUTE_NAME = "LoadBalanceDeviceMessage.parameters.overthresholdduration";
    private static final boolean ARM_FOR_CLOSURE = false;

    private final MessageService messageService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public HeadEndController(MessageService messageService, ExceptionFactory exceptionFactory) {
        this.messageService = messageService;
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
            Instant activationDate = contactorInfo.activationDate != null ? contactorInfo.activationDate : Instant.now();
            switch (contactorInfo.status) {
                case connected:
                    deviceCommand = headEndInterface.getCommandFactory().createConnectCommand(endDevice, activationDate);
                    break;
                case disconnected:
                    deviceCommand = headEndInterface.getCommandFactory().createDisconnectCommand(endDevice, activationDate);
                    break;
                case armed:
                    deviceCommand = headEndInterface.getCommandFactory().createArmCommand(endDevice, ARM_FOR_CLOSURE, activationDate);
                    break;
                default:
                    throw exceptionFactory.newException(MessageSeeds.UNKNOWN_STATUS);
            }
            CompletionOptions completionOptions = headEndInterface.sendCommand(deviceCommand, contactorInfo.activationDate, serviceCall);
            completionOptions.whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), getCompletionOptionsDestinationSpec());
        }
    }

    private void performLoadLimitOperations(EndDevice endDevice, HeadEndInterface headEndInterface, ServiceCall serviceCall, ContactorInfo contactorInfo) {
        if (shouldPerformLoadLimitOperations(contactorInfo)) {
            serviceCall.log(LogLevel.INFO, "Handling load limitation operations.");
            EndDeviceCommand deviceCommand;
            if (contactorInfo.loadLimit.shouldDisableLoadLimit()) {
                deviceCommand = headEndInterface.getCommandFactory().createDisableLoadLimitCommand(endDevice);
            } else {
                Unit unit = contactorInfo.loadLimit.getUnit().orElse(Unit.getUndefined());
                deviceCommand = headEndInterface.getCommandFactory()
                        .createEnableLoadLimitCommand(endDevice, Quantity.create(contactorInfo.loadLimit.limit, unit.getScale(), unit.getBaseUnit().toString()));
                if (contactorInfo.loadTolerance != null) {
                    deviceCommand.setPropertyValue(getCommandArgumentSpec(deviceCommand, OVER_THRESHOLD_DURATION_ATTRIBUTE_NAME), TimeDuration.seconds(contactorInfo.loadTolerance));
                }
            }
            CompletionOptions completionOptions = headEndInterface.sendCommand(deviceCommand, Instant.now(), serviceCall);
            completionOptions.whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), getCompletionOptionsDestinationSpec());
        }
    }

    private PropertySpec getCommandArgumentSpec(EndDeviceCommand endDeviceCommand, String commandArgumentName) {
        return endDeviceCommand.getCommandArgumentSpecs().stream()
                .filter(propertySpec -> propertySpec.getName().equals(commandArgumentName))
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.COMMAND_ARGUMENT_SPEC_NOT_FOUND, commandArgumentName, endDeviceCommand.getEndDeviceControlType().getName()));
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

    private DestinationSpec getCompletionOptionsDestinationSpec() {
        return messageService.getDestinationSpec(CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.COULD_NOT_FIND_DESTINATION_SPEC, CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION));
    }
}