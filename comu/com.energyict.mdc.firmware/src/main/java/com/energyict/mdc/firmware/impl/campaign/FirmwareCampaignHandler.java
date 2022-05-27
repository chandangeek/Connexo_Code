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
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.firmware.impl.MessageSeeds;
import com.energyict.mdc.firmware.impl.TranslationKeys;
import com.energyict.mdc.upl.messages.DeviceMessageAttribute;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import javax.inject.Inject;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class FirmwareCampaignHandler extends EventHandler<LocalEvent> {

    private static final String MANUAL_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/manualcomtaskexecution/COMPLETED";
    private static final String MANUAL_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/manualcomtaskexecution/FAILED";
    private static final String SCHEDULED_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/COMPLETED";
    private static final String SCHEDULED_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/FAILED";
    private static final String FIRMWARE_COMTASKEXECUTION_STARTED = "com/energyict/mdc/device/data/firmwarecomtaskexecution/STARTED";
    private static final String FIRMWARE_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/firmwarecomtaskexecution/COMPLETED";
    private static final String FIRMWARE_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/firmwarecomtaskexecution/FAILED";
    private static final String FIRMWARE_CAMPAIGN_EDITED = "com/energyict/mdc/firmware/firmwarecampaign/EDITED";
    private static final String DEVICE_BEFORE_DELETE = "com/energyict/mdc/device/data/device/BEFORE_DELETE";
    private final FirmwareCampaignServiceImpl firmwareCampaignService;
    private final Clock clock;
    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;
    private final ThreadPrincipalService threadPrincipalService;
    private final TransactionService transactionService;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

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
            case DEVICE_BEFORE_DELETE:
                Device device = (Device) event.getSource();
                firmwareCampaignService.findActiveFirmwareItemByDevice(device).ifPresent(item -> {
                    item.getParent().log(LogLevel.WARNING, "Device '" + device.getName() + "' is being removed. Cancelling the firmware campaign item...");
                    item.cancel();
                });
                firmwareCampaignService.findFirmwareCampaignItems(device).forEach(DeviceInFirmwareCampaign::delete);
                break;
            default:
                break;
        }
    }

    private void onComTaskFailed(ComTaskExecution comTaskExecution) {
        Device device = comTaskExecution.getDevice();
        logger.info("[FWC] onComTaskFailed " + device.getName() + " / "+ comTaskExecution.getComTask().getName() + " -> " + comTaskExecution.getStatusDisplayName());
        Optional<? extends DeviceInFirmwareCampaign> deviceInFirmwareCampaignOptional = firmwareCampaignService.findActiveFirmwareItemByDevice(device);
        if (deviceInFirmwareCampaignOptional.isPresent()) {
            DeviceInFirmwareCampaign deviceInFirmwareCampaign = deviceInFirmwareCampaignOptional.get();
            ServiceCall serviceCall = deviceInFirmwareCampaign.getServiceCall();
            if (serviceCall.getState().equals(DefaultState.ONGOING)) {
                FirmwareCampaign firmwareCampaign = serviceCall.getParent().get().getExtension(FirmwareCampaignDomainExtension.class).get();
                if (comTaskExecution.isFirmware()) {
                    Optional<DeviceMessage> deviceMessageOptional = deviceInFirmwareCampaign.getDeviceMessage();
                    if (deviceMessageOptional.isPresent() && deviceMessageOptional.get().getReleaseDate().isBefore(clock.instant())) {
                        DeviceMessage deviceMessage = deviceMessageOptional.get();
                        if (deviceMessage.getStatus().isPredecessorOf(DeviceMessageStatus.FAILED)) {
                            deviceMessage.updateDeviceMessageStatus(DeviceMessageStatus.FAILED);
                        }
                        serviceCallService.lockServiceCall(serviceCall.getId());
                        serviceCall.requestTransition(DefaultState.FAILED);
                        serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.FIRMWARE_INSTALLATION_FAILED).format());
                    }
                } else if (comTaskExecution.getComTask().getProtocolTasks().stream()
                        .anyMatch(StatusInformationTask.class::isInstance)) {
                    if (firmwareCampaign.isWithVerification()) {
                        Instant firmwareUploadTime = deviceInFirmwareCampaign.getServiceCall().getLastModificationTime();
                        if (firmwareUploadTime.plusMillis(firmwareCampaign.getValidationTimeout().getMilliSeconds()).isBefore(clock.instant())) {
                            if (deviceInFirmwareCampaign.getDeviceMessage().isPresent() && deviceInFirmwareCampaign.getDeviceMessage().get().getStatus().equals(DeviceMessageStatus.CONFIRMED)) {
                                serviceCallService.lockServiceCall(serviceCall.getId());
                                serviceCall.requestTransition(DefaultState.FAILED);
                                serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.VERIFICATION_FAILED).format());
                            }
                        } else {
                            scheduleVerification(deviceInFirmwareCampaign, firmwareUploadTime.plusSeconds(firmwareCampaign.getValidationTimeout().getSeconds()));
                        }
                    }
                } else if (comTaskExecution.getComTask().getId() == firmwareCampaign.getValidationComTaskId()) {
                    serviceCallService.lockServiceCall(serviceCall.getId());
                    serviceCall.requestTransition(DefaultState.FAILED);
                    serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.TASK_FOR_VALIDATION_LOST_ACTION).format());
                }
            }
        }
    }

    private void onComTaskCompleted(ComTaskExecution comTaskExecution) {
        Device device = comTaskExecution.getDevice();

        String logInfo = "[FWC] onComTaskCompleted " + device.getName() + " / "+ comTaskExecution.getComTask().getName()
                + " on " + comTaskExecution.getDevice().getName() + " -> " + comTaskExecution.getStatusDisplayName();


        if (comTaskExecution.isLastExecutionFailed()) {
            logger.info(logInfo + " last execution failed" );
            onComTaskFailed(comTaskExecution);
            return;
        }

        if (comTaskExecution.getLastSession().isPresent()){
            CompletionCode completionCode = comTaskExecution.getLastSession().get().getHighestPriorityCompletionCode();
            if (sessionHasProtocolError(completionCode)){
                logger.warning(logInfo + " has completion code " + completionCode.name()
                        + "! This is a PROTOCOL-ERROR disguised as success! Handling as failed!");
                onComTaskFailed(comTaskExecution);
                return;
            } else {
                logger.info(logInfo + " last highestPriorityCompletionCode is "+completionCode.name());
            }
        } else {
            logger.info(logInfo + " last session is not present!" );
        }

        Optional<? extends DeviceInFirmwareCampaign> deviceInFirmwareCampaignOptional = firmwareCampaignService.findActiveFirmwareItemByDevice(device);
        if (deviceInFirmwareCampaignOptional.isPresent()) {
            DeviceInFirmwareCampaign deviceInFirmwareCampaign = deviceInFirmwareCampaignOptional.get();
            ServiceCall serviceCall = deviceInFirmwareCampaign.getServiceCall();
            if (serviceCall.getState().equals(DefaultState.ONGOING)) {
                FirmwareCampaign firmwareCampaign = serviceCall.getParent().get().getExtension(FirmwareCampaignDomainExtension.class).get();
                if (comTaskExecution.isFirmware()) {
                    if (deviceInFirmwareCampaign.getDeviceMessage().isPresent() && deviceInFirmwareCampaign.getDeviceMessage().get().getStatus().equals(DeviceMessageStatus.CONFIRMED)) {
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
                        Instant firmwareUploadTime = deviceInFirmwareCampaign.getServiceCall().getLastModificationTime();
                        if (firmwareUploadTime.plusMillis(firmwareCampaign.getValidationTimeout().getMilliSeconds()).isBefore(clock.instant())) {
                            if (deviceInFirmwareCampaign.getDeviceMessage().isPresent() && deviceInFirmwareCampaign.getDeviceMessage().get().getStatus().equals(DeviceMessageStatus.CONFIRMED)) {
                                if (deviceInFirmwareCampaign.doesDeviceAlreadyHaveTheSameVersion()) {
                                    serviceCallService.lockServiceCall(serviceCall.getId());
                                    serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                                    serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.VERIFICATION_COMPLETED).format());
                                    firmwareCampaignService.getFirmwareService().cancelFirmwareUploadForDevice(deviceInFirmwareCampaign.getDevice()); // TODO: why here?
                                } else {
                                    serviceCallService.lockServiceCall(serviceCall.getId());
                                    serviceCall.requestTransition(DefaultState.FAILED);
                                    serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.VERIFICATION_FAILED_WRONG_FIRMWAREVERSION).format());
                                }
                            }
                        } else {
                            scheduleVerification(deviceInFirmwareCampaign, firmwareUploadTime.plusSeconds(firmwareCampaign.getValidationTimeout().getSeconds()));
                        }
                    }
                } else if (comTaskExecution.getComTask().getId() == firmwareCampaign.getValidationComTaskId()) {
                    serviceCallService.lockServiceCall(serviceCall.getId());
                    serviceCall.requestTransition(DefaultState.FAILED);
                    serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.TASK_FOR_VALIDATION_LOST_ACTION).format());
                }
            }
        }
    }

    /**
     * See com.energyict.mdc.device.data.tasks.history.CompletionCode.ProtocolError
     * The protocol errors are marked as com.energyict.mdc.upl.meterdata.ResultType.DataIncomplete
     * which will set the connection method result to SUCCESS!
     *
     * So here we check if this is the case, and handle the result as a failed one.
     */
    private boolean sessionHasProtocolError(CompletionCode completionCode) {
        return  (completionCode.equals(CompletionCode.ProtocolError) || completionCode.hasPriorityOver(CompletionCode.ProtocolError));
    }

    private void onComTaskStarted(ComTaskExecution comTaskExecution) {
        if (comTaskExecution.isFirmware()) {
            Device device = comTaskExecution.getDevice();
            logger.info("[FWC] onComTaskStarted " + device.getName() + " / "+ comTaskExecution.getComTask().getName() + " -> " + comTaskExecution.getStatusDisplayName());
            firmwareCampaignService.findActiveFirmwareItemByDevice(device).ifPresent(firmwareItem -> {
                FirmwareVersion firmwareVersion = firmwareItem.getFirmwareCampaign().getFirmwareVersion();
                ServiceCall serviceCall = firmwareItem.getServiceCall();
                if (device.getMessages().stream()
                        .filter(deviceMessage -> deviceMessage.getSpecification().getCategory().getId() == 9)
                        .filter(deviceMessage -> deviceMessage.getAttributes().stream()
                                .map(DeviceMessageAttribute::getValue)
                                .filter(Objects::nonNull)
                                .anyMatch(val -> val.equals(firmwareVersion)))
                        .anyMatch(deviceMessage -> deviceMessage.getStatus().equals(DeviceMessageStatus.PENDING))) {
                    serviceCall.transitionWithLockIfPossible(DefaultState.ONGOING);
                    serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.FIRMWARE_INSTALLATION_STARTED).format());
                }
            });
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
