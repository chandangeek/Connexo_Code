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
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.firmware.impl.MessageSeeds;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.firmware.impl.TranslationKeys;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import javax.inject.Inject;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
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
        this.firmwareCampaignService = firmwareService.getFirmwareCampaignService();
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
                    transactionService.run(() -> firmwareCampaignService.handleCampaignUpdate((FirmwareCampaign) event.getSource()));
                }, Executors.newSingleThreadExecutor());
                break;
            default:
                break;
        }
    }

    private void onComTaskFailed(ComTaskExecution comTaskExecution) {
        Optional<FirmwareCampaign> firmwareCampaignOptional = firmwareCampaignService.getCampaignOn(comTaskExecution);
        if (firmwareCampaignOptional.isPresent()) {
            Device device = comTaskExecution.getDevice();
            DeviceInFirmwareCampaign deviceInFirmwareCampaign = firmwareCampaignService.findActiveFirmwareItemByDevice(device).get();
            if (comTaskExecution.isFirmware()) {
                Optional<DeviceMessage> deviceMessage = deviceInFirmwareCampaign.getDeviceMessage();
                if (deviceMessage.isPresent() && deviceMessage.get().getStatus().equals(DeviceMessageStatus.FAILED)) {
                    ServiceCall serviceCall = deviceInFirmwareCampaign.getServiceCall();
                    serviceCallService.lockServiceCall(serviceCall.getId());
                    serviceCall.requestTransition(DefaultState.FAILED);
                    serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.FIRMWARE_INSTALLATION_FAILED).format());
                }
            } else if (comTaskExecution.getComTask().getProtocolTasks().stream()
                    .anyMatch(StatusInformationTask.class::isInstance)) {
                FirmwareCampaign firmwareCampaign = firmwareCampaignOptional.get();
                if (firmwareCampaign.isWithVerification()) {
                    Instant firmwareTimeUpload = deviceInFirmwareCampaign.getServiceCall().getLastModificationTime();
                    if (firmwareTimeUpload.plusMillis(firmwareCampaign.getValidationTimeout().getMilliSeconds()).isBefore(clock.instant())) {
                        ServiceCall serviceCall = deviceInFirmwareCampaign.getServiceCall();
                        if (deviceInFirmwareCampaign.getDeviceMessage().isPresent() && deviceInFirmwareCampaign.getDeviceMessage().get().getStatus().equals(DeviceMessageStatus.CONFIRMED)) {
                            serviceCallService.lockServiceCall(serviceCall.getId());
                            serviceCall.requestTransition(DefaultState.FAILED);
                            serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.VERIFICATION_FAILED).format());
                        }
                    } else {
                        scheduleVerification(deviceInFirmwareCampaign, firmwareTimeUpload.plusSeconds(firmwareCampaign.getValidationTimeout().getSeconds()));
                    }
                }
            } else if (comTaskExecution.getComTask().getId() == firmwareCampaignOptional.get().getValidationComTaskId()) {
                ServiceCall serviceCall = deviceInFirmwareCampaign.getServiceCall();
                serviceCallService.lockServiceCall(serviceCall.getId());
                serviceCall.requestTransition(DefaultState.FAILED);
                serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.TASK_FOR_VALIDATION_LOST_ACTION).format());
            }
        }
    }

    private void onComTaskCompleted(ComTaskExecution comTaskExecution) {
        Optional<FirmwareCampaign> firmwareCampaignOptional = firmwareCampaignService.getCampaignOn(comTaskExecution);
        if (firmwareCampaignOptional.isPresent()) {
            FirmwareCampaign firmwareCampaign = firmwareCampaignOptional.get();
            DeviceInFirmwareCampaign deviceInFirmwareCampaign = firmwareCampaignService.findActiveFirmwareItemByDevice(comTaskExecution.getDevice()).get();
            if (comTaskExecution.isFirmware()) {
                if (deviceInFirmwareCampaign.getDeviceMessage().isPresent() && deviceInFirmwareCampaign.getDeviceMessage().get().getStatus().equals(DeviceMessageStatus.CONFIRMED)) {
                    ServiceCall serviceCall = deviceInFirmwareCampaign.getServiceCall();
                    serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.FIRMWARE_INSTALLATION_COMPLETED).format());
                    if (!firmwareCampaign.isWithVerification()) {
                        serviceCallService.lockServiceCall(serviceCall.getId());
                        serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                    } else {
                        scheduleVerification(deviceInFirmwareCampaign, clock.instant().plusSeconds(firmwareCampaign.getValidationTimeout().getSeconds()));
                        serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.VERIFICATION_SCHEDULED).format());
                    }
                }
            } else if (comTaskExecution.getComTask().getProtocolTasks().stream()
                    .anyMatch(StatusInformationTask.class::isInstance)) {
                if (firmwareCampaign.isWithVerification()) {
                    Instant firmwareTimeUpload = deviceInFirmwareCampaign
                            .getServiceCall().getLastModificationTime();
                    if (firmwareTimeUpload.plusMillis(firmwareCampaign.getValidationTimeout().getMilliSeconds()).isBefore(clock.instant())) {
                        ServiceCall serviceCall = deviceInFirmwareCampaign.getServiceCall();
                        if (deviceInFirmwareCampaign.getDeviceMessage().isPresent() && deviceInFirmwareCampaign.getDeviceMessage().get().getStatus().equals(DeviceMessageStatus.CONFIRMED)) {
                            if (deviceInFirmwareCampaign.doesDeviceAlreadyHaveTheSameVersion()) {
                                serviceCallService.lockServiceCall(serviceCall.getId());
                                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                                serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.VERIFICATION_COMPLETED).format());
                                firmwareCampaignService.getFirmwareService().cancelFirmwareUploadForDevice(deviceInFirmwareCampaign.getDevice());
                            } else {
                                serviceCallService.lockServiceCall(serviceCall.getId());
                                serviceCall.requestTransition(DefaultState.FAILED);
                                serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.VERIFICATION_FAILED_WRONG_FIRMWAREVERSION).format());
                            }
                        }
                    } else {
                        scheduleVerification(deviceInFirmwareCampaign, firmwareTimeUpload.plusSeconds(firmwareCampaign.getValidationTimeout().getSeconds()));
                    }
                }
            } else if (comTaskExecution.getComTask().getId() == firmwareCampaignOptional.get().getValidationComTaskId()){
                ServiceCall serviceCall = deviceInFirmwareCampaign.getServiceCall();
                serviceCallService.lockServiceCall(serviceCall.getId());
                serviceCall.requestTransition(DefaultState.FAILED);
                serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.TASK_FOR_VALIDATION_LOST_ACTION).format());
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
                        .filter(deviceMessage -> deviceMessage.getAttributes().stream().anyMatch(attr -> attr.getValue().equals(firmwareCampaignOptional.get().getFirmwareVersion())))
                        .anyMatch(deviceMessage -> deviceMessage.getStatus().equals(DeviceMessageStatus.PENDING)
                                || deviceMessage.getStatus().equals(DeviceMessageStatus.WAITING))) {
                    serviceCallService.lockServiceCall(serviceCall.getId());
                    if (serviceCall.canTransitionTo(DefaultState.ONGOING)) {
                        serviceCall.requestTransition(DefaultState.ONGOING);
                    }
                    serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.FIRMWARE_INSTALLATION_STARTED).format());
                } else {
                    firmwareItem.cancel(false);
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

    private void scheduleVerification(DeviceInFirmwareCampaign deviceInFirmwareCampaign, Instant when) {
        ServiceCall serviceCall = deviceInFirmwareCampaign.getServiceCall();
        Optional<? extends FirmwareCampaign> campaignOptional = serviceCall.getParent().get().getExtension(FirmwareCampaignDomainExtension.class);
        if (campaignOptional.isPresent()) {
            FirmwareCampaign campaign = campaignOptional.get();
            Optional<ComTaskExecution> comTaskExecutionOptional = deviceInFirmwareCampaign.findOrCreateVerificationComTaskExecution();
            if (comTaskExecutionOptional.isPresent()) {
                ComTaskExecution comTaskExecution = comTaskExecutionOptional.get();
                if (comTaskExecution.getConnectionTask().isPresent()) {
                    ConnectionStrategy connectionStrategy = ((ScheduledConnectionTask) comTaskExecution.getConnectionTask().get()).getConnectionStrategy();
                    if (comTaskExecution.getConnectionTask().get().isActive() && (!campaign.getValidationConnectionStrategy()
                            .isPresent() || connectionStrategy == campaign.getValidationConnectionStrategy().get())) {
                        comTaskExecution.schedule(when);
                    } else {
                        serviceCallService.lockServiceCall(serviceCall.getId());
                        serviceCall.log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.CONNECTION_METHOD_DOESNT_MEET_THE_REQUIREMENT)
                                .format(thesaurus.getFormat(TranslationKeys.valueOf(campaign.getValidationConnectionStrategy().get().name())).format(), comTaskExecution.getComTask().getName()));
                        serviceCall.requestTransition(DefaultState.FAILED);
                    }
                } else {
                    serviceCallService.lockServiceCall(serviceCall.getId());
                    serviceCall.log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.CONNECTION_METHOD_MISSING_ON_COMTASK).format(comTaskExecution.getComTask().getName()));
                    serviceCall.requestTransition(DefaultState.FAILED);
                }
            } else {
                serviceCallService.lockServiceCall(serviceCall.getId());
                serviceCall.log(LogLevel.SEVERE, thesaurus.getSimpleFormat(MessageSeeds.TASK_FOR_VALIDATION_IS_MISSING).format());
                serviceCall.requestTransition(DefaultState.FAILED);
            }
        }
    }
}