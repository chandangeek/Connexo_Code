/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ami.MultiSenseHeadEndInterface;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.*;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * This class contains the controller who will translate request coming from our workflow code
 * to the proper FullDuplex commands
 */
public class HeadEndController {

    private final MessageService messageService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public HeadEndController(MessageService messageService, ExceptionFactory exceptionFactory) {
        this.messageService = messageService;
        this.exceptionFactory = exceptionFactory;
    }

    public void performOperations(EndDevice endDevice, ServiceCall serviceCall, DeviceCommandInfo deviceCommandInfo, Device device) {
        HeadEndInterface headEndInterface = getHeadEndInterface(endDevice);
        performOperations(endDevice, headEndInterface, serviceCall, deviceCommandInfo, device);
    }

    private void performOperations(EndDevice endDevice, HeadEndInterface headEndInterface, ServiceCall serviceCall, DeviceCommandInfo deviceCommandInfo, Device device) {
        serviceCall.log(LogLevel.INFO, "Handling key renewal.");
        EndDeviceCommand deviceCommand;
        KeyAccessorType keyAccessorType = getKeyAccessorType(deviceCommandInfo.keyAccessorType, device);
        if (keyAccessorType == null) {
            throw exceptionFactory.newException(MessageSeeds.UNKNOWN_KEYACCESSORTYPE);
        }
        deviceCommand = headEndInterface.getCommandFactory().createKeyRenewalCommand(endDevice, keyAccessorType);
        CompletionOptions completionOptions = headEndInterface.sendCommand(deviceCommand, deviceCommandInfo.activationDate, serviceCall);
        completionOptions.whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), getCompletionOptionsDestinationSpec());
    }

    public void performTestCommunication(EndDevice endDevice, ServiceCall serviceCall, DeviceCommandInfo deviceCommandInfo, Device device) {
        MultiSenseHeadEndInterface headEndInterface = (MultiSenseHeadEndInterface) getHeadEndInterface(endDevice);
        performTestCommunication(headEndInterface, serviceCall, deviceCommandInfo, device);
    }

    private void performTestCommunication(HeadEndInterface headEndInterface, ServiceCall serviceCall, DeviceCommandInfo deviceCommandInfo, Device device) {
        serviceCall.log(LogLevel.INFO, "Handling test communication.");
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutions(device, getKeyAccessorType(deviceCommandInfo.keyAccessorType, device));
        CompletionOptions completionOptions = ((MultiSenseHeadEndInterface) headEndInterface).runCommunicationTask(device, getFilteredList(comTaskExecutions), deviceCommandInfo.activationDate, serviceCall);
        completionOptions.whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), getCompletionOptionsDestinationSpec());
    }


    private PropertySpec getCommandArgumentSpec(EndDeviceCommand endDeviceCommand, String commandArgumentName) {
        return endDeviceCommand.getCommandArgumentSpecs().stream()
                .filter(propertySpec -> propertySpec.getName().equals(commandArgumentName))
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.COMMAND_ARGUMENT_SPEC_NOT_FOUND, commandArgumentName, endDeviceCommand.getEndDeviceControlType().getName()));
    }

    private HeadEndInterface getHeadEndInterface(EndDevice endDevice) {
        return endDevice.getHeadEndInterface().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_HEAD_END_INTERFACE, endDevice.getMRID()));
    }

    private DestinationSpec getCompletionOptionsDestinationSpec() {
        return messageService.getDestinationSpec(CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.COULD_NOT_FIND_DESTINATION_SPEC, CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION));
    }

    protected KeyAccessorType getKeyAccessorType(String keyAccessType, Device device) {
        return device.getDeviceType().getKeyAccessorTypes().stream().filter(keyAcccessorType -> keyAcccessorType.getName().equals(keyAccessType)).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.UNKNOWN_KEYACCESSORTYPE));
    }

    protected List<ComTaskExecution> getComTaskExecutions(Device device, KeyAccessorType keyAccessorType) {
        return device.getComTaskExecutions().stream().filter(comTaskExecution -> getComTaskEnablement(comTaskExecution).map(comTaskEnablement1 -> comTaskEnablement1.getSecurityPropertySet().getConfigurationSecurityProperties().stream()
                .anyMatch(configurationSecurityProperty -> configurationSecurityProperty.getKeyAccessorType().equals(keyAccessorType))).orElse(false)).collect(Collectors.toList());
    }

    private Optional<ComTaskEnablement> getComTaskEnablement(ComTaskExecution comTaskExecution) {
        return comTaskExecution.getDevice()
                .getDeviceConfiguration().getComTaskEnablements()
                .stream().filter(comTaskEnablement -> comTaskEnablement.getComTask().getId() == comTaskExecution.getComTask().getId()).findFirst();
    }

    protected List<ComTaskExecution> getFilteredList(List<ComTaskExecution> comTaskExecutions) {
        List<ComTaskExecution> comTaskExecutionList = new ArrayList<>();
        ComTaskExecution comTaskExec = null;
        int priority = 0;
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            List<ProtocolTask> protocolTasks = comTaskExecution.getComTask().getProtocolTasks();
            int newPriority;
            boolean basicCheckFound = false;
            boolean statusInformationFound = false;
            boolean otherFound = false;
            for (ProtocolTask protocolTask : protocolTasks) {
                if (protocolTask instanceof BasicCheckTask) {
                    basicCheckFound = true;
                }
                if (protocolTask instanceof StatusInformationTask) {
                    statusInformationFound = true;
                }
                if (protocolTask instanceof LogBooksTask || protocolTask instanceof RegistersTask || protocolTask instanceof LoadProfilesTask) {
                    otherFound = true;
                }
            }
            newPriority = findPriority(basicCheckFound, statusInformationFound, otherFound);
            if (newPriority > priority) {
                priority = newPriority;
                comTaskExec = comTaskExecution;
            }

        }
        comTaskExecutionList.add(comTaskExec);
        return comTaskExecutionList;
    }

    private int findPriority(boolean basicCheckFound, boolean statusInformationFound, boolean otherFound) {
        int priority;
        if (basicCheckFound && !statusInformationFound && !otherFound) {
            priority = 7;
        } else if (!basicCheckFound && statusInformationFound && !otherFound) {
            priority = 6;
        } else if (basicCheckFound && statusInformationFound && !otherFound) {
            priority = 5;
        } else if (basicCheckFound && !statusInformationFound) {
            priority = 4;
        } else if (basicCheckFound) {
            priority = 3;
        } else if (statusInformationFound) {
            priority = 2;
        } else {
            priority = 1;
        }
        return priority;
    }
}