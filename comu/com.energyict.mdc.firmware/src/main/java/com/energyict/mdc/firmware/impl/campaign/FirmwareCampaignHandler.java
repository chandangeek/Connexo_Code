/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.firmware.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.tasks.FirmwareManagementTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import javax.inject.Inject;
import java.security.Principal;
import java.time.Clock;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class FirmwareCampaignHandler extends EventHandler<LocalEvent> {

    private final static String MANUAL_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/manualcomtaskexecution/COMPLETED";
    private final static String MANUAL_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/manualcomtaskexecution/FAILED";
    private final static String SCHEDULED_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/COMPLETED";
    private final static String SCHEDULED_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/FAILED";
    private final static String FIRMWARE_COMTASKEXECUTION_STARTED = "com/energyict/mdc/device/data/firmwarecomtaskexecution/STARTED";
    private final static String FIRMWARE_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/firmwarecomtaskexecution/COMPLETED";
    private final static String FIRMWARE_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/firmwarecomtaskexecution/FAILED";
    private final static String FIRMWARE_CAMPAIGN_EDITED = "com/energyict/mdc/firmware/firmwarecampaign/EDITED";
    private FirmwareCampaignServiceImpl firmwareCampaignService;
    private Clock clock;
    private ServiceCallService serviceCallService;
    private Thesaurus thesaurus;
    private ThreadPrincipalService threadPrincipalService;
    private TransactionService transactionService;

    @Inject
    public FirmwareCampaignHandler(FirmwareServiceImpl firmwareService, Clock clock, ServiceCallService serviceCallService,
                                   Thesaurus thesaurus, ThreadPrincipalService threadPrincipalService, TransactionService transactionService) {
        super(LocalEvent.class);
        this.firmwareCampaignService = firmwareService.getFirmwareCampaignServiceImpl();
        this.serviceCallService = serviceCallService;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.threadPrincipalService = threadPrincipalService;
        this.transactionService = transactionService;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        switch (event.getType().getTopic()) {
            case FIRMWARE_COMTASKEXECUTION_STARTED:
                processEvent(event, this::onComTaskStarted);
                break;
            case FIRMWARE_COMTASKEXECUTION_COMPLETED:
            case MANUAL_COMTASKEXECUTION_COMPLETED:
            case SCHEDULED_COMTASKEXECUTION_COMPLETED:
                processEvent(event, this::onComTaskCompleted);
                break;
            case FIRMWARE_COMTASKEXECUTION_FAILED:
            case MANUAL_COMTASKEXECUTION_FAILED:
            case SCHEDULED_COMTASKEXECUTION_FAILED:
                processEvent(event, this::onComTaskFailed);
                break;
            case FIRMWARE_CAMPAIGN_EDITED:
                Principal principal = threadPrincipalService.getPrincipal();
                CompletableFuture.runAsync(() -> {
                    threadPrincipalService.set(principal);
                    transactionService.run(() -> firmwareCampaignService.editCampaignItems((FirmwareCampaign) event.getSource()));
                }, Executors.newSingleThreadExecutor());
                break;
            default:
                break;
        }
    }

    private void onComTaskFailed(ComTaskExecution comTaskExecution) {
        if (comTaskExecution.isFirmware()) {
            Optional<FirmwareCampaign> firmwareCampaignOptional = firmwareCampaignService.getCampaignOn(comTaskExecution);
            if (firmwareCampaignOptional.isPresent()) {
                FirmwareCampaign firmwareCampaign = firmwareCampaignOptional.get();
                Device device = comTaskExecution.getDevice();
                if (firmwareCampaignService.findActiveFirmwareItemByDevice(device).get().getDeviceMessage().get().getStatus().equals(DeviceMessageStatus.FAILED)) {
                    ServiceCall serviceCall = firmwareCampaignService.findActiveFirmwareItemByDevice(device).get().getServiceCall();
                    serviceCallService.lockServiceCall(serviceCall.getId());
                    serviceCall.requestTransition(DefaultState.FAILED);
                    serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.FIRMWARE_INSTALLATION_FAILED).format());
                }
            }
        } else if (comTaskExecution.getComTask().getProtocolTasks().stream()
                .anyMatch(StatusInformationTask.class::isInstance)) {
            Optional<FirmwareCampaign> firmwareCampaignOptional = firmwareCampaignService.getCampaignOn(comTaskExecution);
            if (firmwareCampaignOptional.isPresent()) {
                FirmwareCampaign firmwareCampaign = firmwareCampaignOptional.get();
                if (firmwareCampaignService.isWithVerification(firmwareCampaign)) {
                    ServiceCall serviceCall = firmwareCampaignService.findActiveFirmwareItemByDevice(comTaskExecution.getDevice()).get().getServiceCall();
                    if (serviceCall.getExtension(FirmwareCampaignItemDomainExtension.class)
                            .flatMap(FirmwareCampaignItemDomainExtension::getDeviceMessage)
                            .map(DeviceMessage::getStatus)
                            .filter(deviceMessageStatus -> deviceMessageStatus.equals(DeviceMessageStatus.CONFIRMED))
                            .isPresent()) {
                        serviceCallService.lockServiceCall(serviceCall.getId());
                        serviceCall.requestTransition(DefaultState.FAILED);
                        serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.VERIFICATION_FAILED).format());
                    }
                }
            }
        }
    }

    private void onComTaskCompleted(ComTaskExecution comTaskExecution) {
        if (comTaskExecution.isFirmware()) {
            Optional<FirmwareCampaign> firmwareCampaignOptional = firmwareCampaignService.getCampaignOn(comTaskExecution);
            if (firmwareCampaignOptional.isPresent()) {
                FirmwareCampaign firmwareCampaign = firmwareCampaignOptional.get();
                Device device = comTaskExecution.getDevice();
                ServiceCall serviceCall = firmwareCampaignService.findActiveFirmwareItemByDevice(comTaskExecution.getDevice()).get().getServiceCall();
                if (!firmwareCampaignService.isWithVerification(firmwareCampaign)) {
                    serviceCallService.lockServiceCall(serviceCall.getId());
                    serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                    serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.FIRMWARE_INSTALLATION_COMPLETED).format());
                    firmwareCampaignService.revokeCommands(device); //in case calendar has already been uploaded out of campaign scope
                } else {
                    scheduleVerification(device, firmwareCampaign.getValidationTimeout());
                    serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.VERIFICATION_SCHEDULED).format());
                }
            }
        } else if (comTaskExecution.getComTask().getProtocolTasks().stream()
                .anyMatch(StatusInformationTask.class::isInstance)) {
            Optional<FirmwareCampaign> firmwareCampaignOptional = firmwareCampaignService.getCampaignOn(comTaskExecution);
            if (firmwareCampaignOptional.isPresent()) {
                FirmwareCampaign firmwareCampaign = firmwareCampaignOptional.get();
                if (firmwareCampaignService.isWithVerification(firmwareCampaign)) {
                    ServiceCall serviceCall = firmwareCampaignService.findActiveFirmwareItemByDevice(comTaskExecution.getDevice()).get().getServiceCall();
                    if (serviceCall.getExtension(FirmwareCampaignItemDomainExtension.class)
                            .flatMap(FirmwareCampaignItemDomainExtension::getDeviceMessage)
                            .map(DeviceMessage::getStatus)
                            .filter(deviceMessageStatus -> deviceMessageStatus.equals(DeviceMessageStatus.CONFIRMED))
                            .isPresent()) {
                        if (firmwareCampaignService.getFirmwareService().getActiveFirmwareVersion(comTaskExecution.getDevice(), firmwareCampaign.getFirmwareType())
                                .get().getFirmwareVersion().getFirmwareVersion().equals(firmwareCampaign.getFirmwareVersion().getFirmwareVersion())) {
                            serviceCallService.lockServiceCall(serviceCall.getId());
                            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                            serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.VERIFICATION_COMPLETED).format());
                        } else {
                            serviceCallService.lockServiceCall(serviceCall.getId());
                            serviceCall.requestTransition(DefaultState.FAILED);
                            serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.VERIFICATION_FAILED_WRONG_FIRMWAREVERSION).format());
                        }
                    }
                }
            }
        }
    }

    private void onComTaskStarted(ComTaskExecution comTaskExecution) {
        if (comTaskExecution.isFirmware()) {
            Optional<FirmwareCampaign> firmwareCampaignOptional = firmwareCampaignService.getCampaignOn(comTaskExecution);
            if (firmwareCampaignOptional.isPresent()) {
                Device device = comTaskExecution.getDevice();
                DeviceInFirmwareCampaign firmwareItem = firmwareCampaignService.findActiveFirmwareItemByDevice(device).get();
                ServiceCall serviceCall = firmwareItem.getServiceCall();
                if (device.getMessages().stream().filter(deviceMessage -> deviceMessage.getSpecification().getCategory().getId() == 9)
                        .anyMatch(deviceMessage -> deviceMessage.getStatus().equals(DeviceMessageStatus.PENDING)
                                || deviceMessage.getStatus().equals(DeviceMessageStatus.WAITING))) {
                    serviceCallService.lockServiceCall(serviceCall.getId());
                    serviceCall.requestTransition(DefaultState.ONGOING);
                    serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.FIRMWARE_INSTALLATION_STARTED).format());
                } else {
                    firmwareItem.cancel();
                }
            }
        }
    }

    private interface EventProcessor {
        void process(ComTaskExecution source);
    }

    private void processEvent(LocalEvent event, EventProcessor processor) {
        Object source = event.getSource();
        if (source instanceof ComTaskExecution) {
            ComTaskExecution comTaskExecution = (ComTaskExecution) source;
            processor.process(comTaskExecution);
        }
    }

    private void scheduleVerification(Device device, long validationTimeout) {
        Optional<ComTaskEnablement> comTaskEnablementOptional = device.getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks().stream()
                        .anyMatch(task -> task instanceof StatusInformationTask))
                .filter(comTaskEnablement -> !comTaskEnablement.isSuspended())
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks().stream()
                        .noneMatch(protocolTask -> protocolTask instanceof FirmwareManagementTask))
                .filter(comTaskEnablement -> (firmwareCampaignService.findComTaskExecution(device, comTaskEnablement) == null)
                        || (!firmwareCampaignService.findComTaskExecution(device, comTaskEnablement).isOnHold()))
                .findAny();
        if (comTaskEnablementOptional.isPresent()) {
            ComTaskExecution comTaskExecution = device.getComTaskExecutions().stream()
                    .filter(comTaskExecution1 -> comTaskExecution1.getComTask().equals(comTaskEnablementOptional.get().getComTask()))
                    .findAny().orElse(null);
            if (comTaskExecution == null) {
                comTaskExecution = device.newAdHocComTaskExecution(comTaskEnablementOptional.get()).add();
            }
            comTaskExecution.schedule(clock.instant().plusSeconds(validationTimeout));
        } else {
            ServiceCall serviceCall = firmwareCampaignService.findActiveFirmwareItemByDevice(device).get().getServiceCall();
            serviceCallService.lockServiceCall(serviceCall.getId());
            serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.ACTIVE_VERIFICATION_TASK_ISNT_FOUND).format());
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }
}
