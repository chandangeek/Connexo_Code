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
import com.elster.jupiter.util.concurrent.LockUtils;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.firmware.impl.MessageSeeds;
import com.energyict.mdc.firmware.impl.TranslationKeys;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.upl.messages.DeviceMessageAttribute;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import javax.inject.Inject;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
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
    private final DeviceMessageService deviceMessageService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final FirmwareServiceImpl firmwareService;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Inject
    public FirmwareCampaignHandler(FirmwareServiceImpl firmwareService, Clock clock, ServiceCallService serviceCallService,
                                   Thesaurus thesaurus, ThreadPrincipalService threadPrincipalService, TransactionService transactionService,
                                   DeviceMessageService deviceMessageService, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        super(LocalEvent.class);
        this.firmwareCampaignService = firmwareService.getFirmwareCampaignService();
        this.serviceCallService = serviceCallService;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.threadPrincipalService = threadPrincipalService;
        this.transactionService = transactionService;
        this.deviceMessageService = deviceMessageService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.firmwareService = firmwareService;
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

        logger.info("[FWC] onComTaskFailed " + device.getName() + " / "
                + comTaskExecution.getComTask().getName() + " -> " + comTaskExecution.getStatusDisplayName());

        Optional<? extends DeviceInFirmwareCampaign> deviceInFirmwareCampaignOptional = firmwareCampaignService.findActiveFirmwareItemByDevice(device);
        if (deviceInFirmwareCampaignOptional.isPresent()) {
            DeviceInFirmwareCampaign deviceInFirmwareCampaign = deviceInFirmwareCampaignOptional.get();
            ServiceCall serviceCall = deviceInFirmwareCampaign.getServiceCall();
            if (serviceCall.getState().equals(DefaultState.ONGOING)) {
                FirmwareCampaign firmwareCampaign = serviceCall.getParent().get().getExtension(FirmwareCampaignDomainExtension.class).get();
                if (comTaskExecution.isFirmware()) {
                    Optional<DeviceMessage> deviceMessageOptional = deviceInFirmwareCampaign.getDeviceMessage();
                    if (deviceMessageOptional.isPresent() && deviceMessageOptional.get().getReleaseDate().isBefore(clock.instant())) {
                        if (serviceCall.transitionWithLockIfPossible(DefaultState.FAILED)) {
                            LockUtils.lockWithDoubleCheck(deviceMessageOptional.get(),
                                            deviceMessageService::findAndLockDeviceMessageById,
                                            message -> message.getStatus().isPredecessorOf(DeviceMessageStatus.FAILED))
                                    .ifPresent(message -> message.updateDeviceMessageStatus(DeviceMessageStatus.FAILED));
                            serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.FIRMWARE_INSTALLATION_FAILED).format());
                        }
                    }
                } else if (comTaskExecution.getComTask().getProtocolTasks().stream()
                        .anyMatch(StatusInformationTask.class::isInstance)) {
                    if (firmwareCampaign.isWithVerification()) {
                        Optional<Instant> activationDate = getActivationTime(deviceInFirmwareCampaign);
                        if (activationDate.isPresent()) { // means the message is present and sent
                            if (activationDate.get().plusMillis(firmwareCampaign.getValidationTimeout().getMilliSeconds()).isBefore(clock.instant())) {
                                MessageSeed message;
                                switch (deviceInFirmwareCampaign.getDeviceMessage().get().getStatus()) {
                                    case CONFIRMED:
                                        message = MessageSeeds.VERIFICATION_FAILED;
                                        break;
                                    case FAILED:
                                    case INDOUBT:
                                        message = MessageSeeds.FIRMWARE_INSTALLATION_FAILED;
                                        break;
                                    case CANCELED:
                                        message = MessageSeeds.FIRMWARE_INSTALLATION_HAS_BEEN_CANCELLED;
                                        break;
                                    default:
                                        // nothing to do, wait for final state
                                        return;
                                }
                                if (serviceCall.transitionWithLockIfPossible(DefaultState.FAILED)) {
                                    serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(message).format());
                                }
                            } else {
                                // the time is not out yet before verification
                                scheduleVerification(deviceInFirmwareCampaign, activationDate.get().plusMillis(firmwareCampaign.getValidationTimeout().getMilliSeconds()));
                            }
                        }
                    }
                } else if (comTaskExecution.getComTask().getId() == firmwareCampaign.getValidationComTaskId()) {
                    if (serviceCall.transitionWithLockIfPossible(DefaultState.FAILED)) {
                        serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.TASK_FOR_VALIDATION_LOST_ACTION).format());
                    }
                }
            }
        }
    }

    private void onComTaskCompleted(ComTaskExecution comTaskExecution) {
        Device device = comTaskExecution.getDevice();

        logger.info("[FWC] onComTaskCompleted " + device.getName() + " / "
                + comTaskExecution.getComTask().getName() + " -> " + comTaskExecution.getStatusDisplayName());

        Optional<? extends DeviceInFirmwareCampaign> deviceInFirmwareCampaignOptional = firmwareCampaignService.findActiveFirmwareItemByDevice(device);
        if (deviceInFirmwareCampaignOptional.isPresent()) {
            DeviceInFirmwareCampaign deviceInFirmwareCampaign = deviceInFirmwareCampaignOptional.get();
            ServiceCall serviceCall = deviceInFirmwareCampaign.getServiceCall();
            if (serviceCall.getState().equals(DefaultState.ONGOING)) {
                FirmwareCampaign firmwareCampaign = serviceCall.getParent().get().getExtension(FirmwareCampaignDomainExtension.class).get();
                if (comTaskExecution.isFirmware()) {
                    if (deviceInFirmwareCampaign.getDeviceMessage().isPresent()) {
                        switch (deviceInFirmwareCampaign.getDeviceMessage().get().getStatus()) {
                            case CONFIRMED:
                                serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.FIRMWARE_INSTALLATION_COMPLETED).format());
                                if (firmwareCampaign.isWithVerification()) {
                                    Instant activationTime = getActivationTime(deviceInFirmwareCampaign).orElseGet(clock::instant);
                                    scheduleVerification(deviceInFirmwareCampaign, activationTime.plusMillis(firmwareCampaign.getValidationTimeout().getMilliSeconds()));
                                    serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.VERIFICATION_SCHEDULED).format());
                                } else {
                                    serviceCall.transitionWithLockIfPossible(DefaultState.SUCCESSFUL);
                                }
                                break;
                            case CANCELED:
                                if (serviceCall.transitionWithLockIfPossible(DefaultState.CANCELLED)) {
                                    serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.FIRMWARE_INSTALLATION_HAS_BEEN_CANCELLED).format());
                                }
                                break;
                            case FAILED:
                            case INDOUBT:
                                if (serviceCall.transitionWithLockIfPossible(DefaultState.FAILED)) {
                                    serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.FIRMWARE_INSTALLATION_FAILED).format());
                                }
                                break;
                            default:
                                // nothing to do, wait for final state
                        }
                    }
                } else if (comTaskExecution.getComTask().getProtocolTasks().stream()
                        .anyMatch(StatusInformationTask.class::isInstance)) {
                    if (firmwareCampaign.isWithVerification()) {
                        Optional<Instant> activationDate = getActivationTime(deviceInFirmwareCampaign);
                        if (activationDate.isPresent()) { // means the message is present and sent
                            if (activationDate.get().plusMillis(firmwareCampaign.getValidationTimeout().getMilliSeconds()).isBefore(clock.instant())) {
                                switch (deviceInFirmwareCampaign.getDeviceMessage().get().getStatus()) {
                                    case CONFIRMED:
                                        if (deviceInFirmwareCampaign.doesDeviceAlreadyHaveTheSameVersion()) {
                                            if (serviceCall.transitionWithLockIfPossible(DefaultState.SUCCESSFUL)) {
                                                serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.VERIFICATION_COMPLETED).format());
                                                firmwareCampaignService.getFirmwareService().cancelFirmwareUploadForDevice(deviceInFirmwareCampaign.getDevice()); // TODO: why here?
                                            }
                                        } else {
                                            if (serviceCall.transitionWithLockIfPossible(DefaultState.FAILED)) {
                                                String currentFirmware = firmwareService.getActiveFirmwareVersion(device, firmwareCampaign.getFirmwareType())
                                                        .map(ActivatedFirmwareVersion::getFirmwareVersion)
                                                        .map(FirmwareVersion::getFirmwareVersion)
                                                        .orElse("n/a");
                                                String warningText = thesaurus.getFormat(MessageSeeds.VERIFICATION_FAILED_WRONG_FIRMWAREVERSION)
                                                        .format(firmwareCampaign.getFirmwareVersion().getFirmwareVersion(), currentFirmware);
                                                serviceCall.log(LogLevel.WARNING, warningText);
                                            }
                                        }
                                        break;
                                    case CANCELED:
                                        if (serviceCall.transitionWithLockIfPossible(DefaultState.CANCELLED)) {
                                            serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.FIRMWARE_INSTALLATION_HAS_BEEN_CANCELLED).format());
                                        }
                                        break;
                                    case FAILED:
                                    case INDOUBT:
                                        if (serviceCall.transitionWithLockIfPossible(DefaultState.FAILED)) {
                                            serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.FIRMWARE_INSTALLATION_FAILED).format());
                                        }
                                        break;
                                    default:
                                        // nothing to do, wait for final state
                                }
                            } else {
                                // the time is not out yet before verification
                                scheduleVerification(deviceInFirmwareCampaign, activationDate.get().plusMillis(firmwareCampaign.getValidationTimeout().getMilliSeconds()));
                            }
                        }
                    }
                } else if (comTaskExecution.getComTask().getId() == firmwareCampaign.getValidationComTaskId()) {
                    if (serviceCall.transitionWithLockIfPossible(DefaultState.FAILED)) {
                        serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.TASK_FOR_VALIDATION_LOST_ACTION).format());
                    }
                }
            }
        }
    }

    private void onComTaskStarted(ComTaskExecution comTaskExecution) {
        if (comTaskExecution.isFirmware()) {
            Device device = comTaskExecution.getDevice();
            logger.info("[FWC] onComTaskStarted " + device.getName() + " / "
                    + comTaskExecution.getComTask().getName() + " -> " + comTaskExecution.getStatusDisplayName());
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
                    if (serviceCall.transitionWithLockIfPossible(DefaultState.ONGOING)) {
                        serviceCall.log(LogLevel.INFO, thesaurus.getFormat(MessageSeeds.FIRMWARE_INSTALLATION_STARTED).format());
                    }
                }
            });
        }
    }

    private Optional<Instant> getActivationTime(DeviceInFirmwareCampaign deviceInFirmwareCampaign) {
        return deviceInFirmwareCampaign.getDeviceMessage()
                .filter(message -> message.getSentDate().isPresent())
                .map(message -> {
                    Instant messageExecutionTime = message.getModTime();
                    Optional<ProtocolSupportedFirmwareOptions> uploadOption = deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(message.getDeviceMessageId());
                    if (uploadOption.isPresent()
                            && uploadOption.get() == ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE) {
                        return retrieveDate(message)
                                .map(activationTime -> max(activationTime, messageExecutionTime))
                                .orElse(messageExecutionTime);
                    }
                    return messageExecutionTime;
                });
    }

    private static Instant max(Instant one, Instant two) {
        return one.isAfter(two) ? one : two;
    }

    private Optional<Instant> retrieveDate(DeviceMessage deviceMessage) {
        return deviceMessage.getSpecification().getPropertySpecs().stream()
                .filter(propertySpec -> Date.class == propertySpec.getValueFactory().getValueType())
                .findAny()
                .flatMap(propertySpec -> deviceMessage.getAttributes().stream()
                        .filter(attribute -> attribute.getName().equals(propertySpec.getName()))
                        .findAny())
                .map(DeviceMessageAttribute::getValue)
                .filter(Date.class::isInstance)
                .map(Date.class::cast)
                .map(Date::toInstant);
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
                    if (comTaskExecution.getConnectionTask().get().isActive()
                            && !campaign.getValidationConnectionStrategy().filter(cs -> cs != connectionStrategy).isPresent()) {
                        comTaskExecution.schedule(when);
                    } else {
                        if (serviceCall.transitionWithLockIfPossible(DefaultState.FAILED)) {
                            String strategyName = thesaurus.getFormat(TranslationKeys.valueOf(campaign.getValidationConnectionStrategy().get().name())).format();
                            String warningText = thesaurus.getSimpleFormat(MessageSeeds.CONNECTION_METHOD_DOESNT_MEET_THE_REQUIREMENT)
                                    .format(strategyName, comTaskExecution.getComTask().getName());
                            serviceCall.log(LogLevel.WARNING, warningText);
                        }
                    }
                } else {
                    if (serviceCall.transitionWithLockIfPossible(DefaultState.FAILED)) {
                        serviceCall.log(LogLevel.WARNING,
                                thesaurus.getSimpleFormat(MessageSeeds.CONNECTION_METHOD_MISSING_ON_COMTASK).format(comTaskExecution.getComTask().getName()));
                    }
                }
            } else {
                if (serviceCall.transitionWithLockIfPossible(DefaultState.FAILED)) {
                    serviceCall.log(LogLevel.SEVERE, thesaurus.getSimpleFormat(MessageSeeds.TASK_FOR_VALIDATION_IS_MISSING).format());
                }
            }
        }
    }
}
