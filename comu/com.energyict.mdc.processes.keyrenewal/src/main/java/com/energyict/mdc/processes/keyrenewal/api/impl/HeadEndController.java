/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.pki.CertificateType;
import com.elster.jupiter.pki.KeyUsage;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.BasicCheckTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.LoadProfilesTask;
import com.energyict.mdc.common.tasks.LogBooksTask;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.common.tasks.RegistersTask;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.device.data.ami.MultiSenseHeadEndInterface;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.security.cert.CertificateParsingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
        switch (deviceCommandInfo.command) {
            case RENEW_KEY:
                performKeyRenewalOperations(endDevice, headEndInterface, serviceCall, deviceCommandInfo, device, false);
                break;
            case UPLOAD_CERTIFICATE:
                performUploadCertificateOperations(endDevice, headEndInterface, serviceCall, deviceCommandInfo, device);
                break;
            case GENERATE_KEYPAIR:
                performGenerateKeyPairOperations(endDevice, headEndInterface, serviceCall, deviceCommandInfo, device);
                break;
            case REQUEST_CSR:
                performRequestCsrOperations(endDevice, headEndInterface, serviceCall, deviceCommandInfo, device);
                break;
            case RENEW_SERVICE_KEY:
                performKeyRenewalOperations(endDevice, headEndInterface, serviceCall, deviceCommandInfo, device, true);
                break;
            default:
                performDummyOperation(endDevice, headEndInterface, serviceCall, deviceCommandInfo, device);
        }
    }

    private void performKeyRenewalOperations(EndDevice endDevice, HeadEndInterface headEndInterface, ServiceCall serviceCall, DeviceCommandInfo deviceCommandInfo, Device device, boolean isServiceKey) {
        serviceCall.log(LogLevel.INFO, "Handling key renewal.");
        EndDeviceCommand deviceCommand;
        List<SecurityAccessorType> securityAccessorTypes = Arrays.stream(deviceCommandInfo.keyAccessorType.split(","))
                .map(sA -> getKeyAccessorType(sA, device))
                .peek(aT -> {
                    if (aT == null) {
                        throw exceptionFactory.newException(MessageSeeds.UNKNOWN_KEYACCESSORTYPE);
                    }
                })
                .collect(Collectors.toList());
        deviceCommand = headEndInterface.getCommandFactory().createKeyRenewalCommand(endDevice, securityAccessorTypes, isServiceKey);
        CompletionOptions completionOptions = headEndInterface.sendCommand(deviceCommand, deviceCommandInfo.activationDate, serviceCall);
        completionOptions.whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), getCompletionOptionsDestinationSpec());
    }

    private void performUploadCertificateOperations(EndDevice endDevice, HeadEndInterface headEndInterface, ServiceCall serviceCall, DeviceCommandInfo deviceCommandInfo, Device device) {
        serviceCall.log(LogLevel.INFO, "Handling upload certificate.");
        EndDeviceCommand deviceCommand;
        SecurityAccessorType securityAccessorType = getKeyAccessorType(deviceCommandInfo.keyAccessorType, device);
        if (securityAccessorType == null) {
            throw exceptionFactory.newException(MessageSeeds.UNKNOWN_KEYACCESSORTYPE);
        }
        deviceCommand = headEndInterface.getCommandFactory().createImportCertificateCommand(endDevice, securityAccessorType);
        CompletionOptions completionOptions = headEndInterface.sendCommand(deviceCommand, deviceCommandInfo.activationDate, serviceCall);
        completionOptions.whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), getCompletionOptionsDestinationSpec());
    }

    private void performRequestCsrOperations(EndDevice endDevice, HeadEndInterface headEndInterface, ServiceCall serviceCall, DeviceCommandInfo deviceCommandInfo, Device device) {
        serviceCall.log(LogLevel.INFO, "Handling request CSR from device.");
        EndDeviceCommand deviceCommand;
        SecurityAccessorType securityAccessorType = getKeyAccessorType(deviceCommandInfo.keyAccessorType, device);
        if (securityAccessorType == null) {
            throw exceptionFactory.newException(MessageSeeds.UNKNOWN_KEYACCESSORTYPE);
        }
        CertificateType certificateType = Stream.of(CertificateType.values()).filter(e -> e.isApplicableTo(securityAccessorType.getKeyType()))
                .findFirst().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_APPLICABLE_CERTIFICATE_TYPE, securityAccessorType.getName()));
        deviceCommand = headEndInterface.getCommandFactory().createGenerateCSRCommand(endDevice, certificateType);
        CompletionOptions completionOptions = headEndInterface.sendCommand(deviceCommand, deviceCommandInfo.activationDate, serviceCall);
        completionOptions.whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), getCompletionOptionsDestinationSpec());
    }

    private void performGenerateKeyPairOperations(EndDevice endDevice, HeadEndInterface headEndInterface, ServiceCall serviceCall, DeviceCommandInfo deviceCommandInfo, Device device) {
        serviceCall.log(LogLevel.INFO, "Handling generate key pair.");
        EndDeviceCommand deviceCommand;
        SecurityAccessorType securityAccessorType = getKeyAccessorType(deviceCommandInfo.keyAccessorType, device);
        if (securityAccessorType == null) {
            throw exceptionFactory.newException(MessageSeeds.UNKNOWN_KEYACCESSORTYPE);
        }
        CertificateType certificateType = Stream.of(CertificateType.values()).filter(e -> e.isApplicableTo(securityAccessorType.getKeyType()))
                .findFirst().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_APPLICABLE_CERTIFICATE_TYPE, securityAccessorType.getName()));
        deviceCommand = headEndInterface.getCommandFactory().createGenerateKeyPairCommand(endDevice, certificateType);
        CompletionOptions completionOptions = headEndInterface.sendCommand(deviceCommand, deviceCommandInfo.activationDate, serviceCall);
        completionOptions.whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), getCompletionOptionsDestinationSpec());
    }

    private void performDummyOperation(EndDevice endDevice, HeadEndInterface headEndInterface, ServiceCall serviceCall, DeviceCommandInfo deviceCommandInfo, Device device) {
        serviceCall.log(LogLevel.INFO, "Handling command.");
        serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void performTestCommunication(EndDevice endDevice, ServiceCall serviceCall, DeviceCommandInfo deviceCommandInfo, Device device) {
        MultiSenseHeadEndInterface headEndInterface = (MultiSenseHeadEndInterface) getHeadEndInterface(endDevice);
        performTestCommunication(headEndInterface, serviceCall, deviceCommandInfo, device);
    }

    private void performTestCommunication(HeadEndInterface headEndInterface, ServiceCall serviceCall, DeviceCommandInfo deviceCommandInfo, Device device) {
        serviceCall.log(LogLevel.INFO, "Handling test communication.");
        SecurityAccessorType securityAccessorType = getKeyAccessorType(deviceCommandInfo.keyAccessorType, device);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutions(device, securityAccessorType);
        if (comTaskExecutions.isEmpty() && securityAccessorType.getTrustStore().isPresent()) {
            for (TrustedCertificate certificate : securityAccessorType.getTrustStore().get().getCertificates()) {
                if (certificate.getKeyUsages().contains(KeyUsage.digitalSignature) &&
                        certificate.getKeyUsages().contains(KeyUsage.keyAgreement)) {
                    comTaskExecutions.addAll(device.getComTaskExecutions());
                    break;
                }
            }
        }
        CompletionOptions completionOptions = ((MultiSenseHeadEndInterface) headEndInterface).runCommunicationTask(device, getFilteredList(comTaskExecutions), deviceCommandInfo.activationDate, serviceCall);
        completionOptions.whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), getCompletionOptionsDestinationSpec());
    }

    public void performTestCommunicationForSecuritySet(EndDevice endDevice, ServiceCall serviceCall, DeviceCommandInfo deviceCommandInfo, Device device) {
        MultiSenseHeadEndInterface headEndInterface = (MultiSenseHeadEndInterface) getHeadEndInterface(endDevice);
        serviceCall.log(LogLevel.INFO, "Handling test communication for security set.");
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutions(device, getSecurityPropertySet(deviceCommandInfo.securityPropertySet, device));
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

    protected SecurityAccessorType getKeyAccessorType(String keyAccessType, Device device) {
        return device.getDeviceType().getSecurityAccessorTypes().stream().filter(keyAccessorType -> keyAccessorType.getName().equals(keyAccessType)).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.UNKNOWN_KEYACCESSORTYPE));
    }

    protected SecurityPropertySet getSecurityPropertySet(String securityPropertySetName, Device device) {
        return device.getDeviceConfiguration().getSecurityPropertySets().stream()
                .filter(set -> set.getName().equals(securityPropertySetName)).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.UNKNOWN_SECURITYPROPERTYSET));
    }

    protected List<ComTaskExecution> getComTaskExecutions(Device device, SecurityAccessorType securityAccessorType) {
        return device.getComTaskExecutions()
                .stream()
                .filter(comTaskExecution -> getComTaskEnablement(comTaskExecution).map(comTaskEnablement1 -> comTaskEnablement1.getSecurityPropertySet().getConfigurationSecurityProperties().stream()
                        .anyMatch(configurationSecurityProperty -> configurationSecurityProperty.getSecurityAccessorType().equals(securityAccessorType))).orElse(false))
                .collect(Collectors.toList());
    }

    protected List<ComTaskExecution> getComTaskExecutions(Device device, SecurityPropertySet securityPropertySet) {
        return device.getComTaskExecutions().stream().filter(comTaskExecution -> getComTaskEnablement(comTaskExecution)
                .map(comTaskEnablement -> comTaskEnablement.getSecurityPropertySet().equals(securityPropertySet)).orElse(false))
                .collect(Collectors.toList());
    }

    private Optional<ComTaskEnablement> getComTaskEnablement(ComTaskExecution comTaskExecution) {
        return comTaskExecution.getDevice()
                .getDeviceConfiguration().getComTaskEnablements()
                .stream().filter(comTaskEnablement -> comTaskEnablement.getComTask().getId() == comTaskExecution.getComTask().getId()).findFirst();
    }

    private CertificateType getCertificateType(SecurityAccessorType securityAccessorType) {
        return Arrays.stream(CertificateType.values()).filter(ct -> ct.isApplicableTo(securityAccessorType.getKeyType())).findFirst().orElse(CertificateType.OTHER);
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
        if (comTaskExec != null) {
            comTaskExecutionList.add(comTaskExec);
        }
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
