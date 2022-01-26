/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is stateful utilities class.
 */
public class FirmwareManagementDeviceUtilsImpl implements FirmwareManagementDeviceUtils {
    public static final Set<DeviceMessageStatus> PENDING_STATUSES = EnumSet.of(DeviceMessageStatus.WAITING, DeviceMessageStatus.PENDING, DeviceMessageStatus.SENT);
    public static final Set<TaskStatus> BUSY_TASK_STATUSES = EnumSet.of(TaskStatus.Busy, TaskStatus.Retrying);

    private final Thesaurus thesaurus;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final FirmwareService firmwareService;
    private final DeviceMessageService deviceMessageService;
    private final TaskService taskService;
    private final ConnectionTaskService connectionTaskService;
    private final CommunicationTaskService communicationTaskService;

    private Device device;
    private ComTaskExecution firmwareComTaskExecution;
    private final List<DeviceMessage> firmwareMessages;
    private final Map<DeviceMessageId, Optional<ProtocolSupportedFirmwareOptions>> uploadOptionsCache;

    @Inject
    public FirmwareManagementDeviceUtilsImpl(Thesaurus thesaurus,
                                             DeviceMessageSpecificationService deviceMessageSpecificationService,
                                             FirmwareService firmwareService,
                                             TaskService taskService,
                                             DeviceMessageService deviceMessageService,
                                             ConnectionTaskService connectionTaskService,
                                             CommunicationTaskService communicationTaskService) {
        this.thesaurus = thesaurus;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.firmwareService = firmwareService;
        this.taskService = taskService;
        this.deviceMessageService = deviceMessageService;
        this.uploadOptionsCache = new HashMap<>();
        this.firmwareMessages = new ArrayList<>();
        this.connectionTaskService = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
    }

    public FirmwareManagementDeviceUtils initFor(Device device, boolean onlyLastMessagePerFirmwareType) {
        this.device = device;
        initFirmwareComTaskExecution();
        if (onlyLastMessagePerFirmwareType) {
            // only firmware upgrade, no revoked messages and only one message for each firmware type
            initFirmwareMessagesUnique();
        } else {
            // Firmware history, all messages for each firmware type
            initFirmwareMessages();
        }
        return this;
    }

    private void initFirmwareMessages() {
        Map<FirmwareType, List<DeviceMessage>> uploadMessages = new HashMap<>();
        Map<String, DeviceMessage> activationMessages = new HashMap<>();
        this.device.getFirmwareMessages().stream().forEach(candidate -> {
            if (!DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(candidate.getDeviceMessageId())) {
                gatherAllUploadMessages(uploadMessages, candidate);
            } else {
                activationMessages.put(candidate.getTrackingId(), candidate);
            }
        });
        for (List<DeviceMessage> messages : uploadMessages.values())
            this.firmwareMessages.addAll(messages);
        }
        this.firmwareMessages.addAll(this.firmwareMessages.stream()
                .map(message -> activationMessages.get(String.valueOf(message.getId())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    private void initFirmwareComTaskExecution() {
        firmwareComTaskExecution = taskService.findFirmwareComTask()
                .flatMap(ct -> this.device.getComTaskExecutions().stream().filter(cte -> cte.executesComTask(ct)).findFirst())
                .orElse(null);
    }

    private void gatherAllUploadMessages(Map<FirmwareType, List<DeviceMessage>> uploadMessages, DeviceMessage candidate) {
        Optional<FirmwareVersion> version = getFirmwareVersionFromMessage(candidate);
        if (version.isPresent()) {
            FirmwareType key = version.get().getFirmwareType();
            if (uploadMessages.containsKey(key)) {
                uploadMessages.get(key).add(candidate);
            } else {
                uploadMessages.put(key, new ArrayList<>(Arrays.asList(candidate)));
            }
        }
    }

    private void initFirmwareMessagesUnique() {
        Map<FirmwareType, DeviceMessage> uploadMessages = new HashMap<>();
        Map<String, DeviceMessage> activationMessages = new HashMap<>();
        // only firmware upgrade, no revoked messages and only one message for each firmware type
        this.device.getMessages().stream().filter(candidate -> candidate.getSpecification() != null)
                .filter(candidate -> candidate.getSpecification().getCategory().getId() == this.deviceMessageSpecificationService.getFirmwareCategory().getId()
                        && !DeviceMessageStatus.CANCELED.equals(candidate.getStatus())).forEach(candidate -> {
                    if (!DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(candidate.getDeviceMessageId())) {
                        compareAndSwapUploadMessage(uploadMessages, candidate);
                    } else {
                        activationMessages.put(candidate.getTrackingId(), candidate);
                    }
                });
        this.firmwareMessages.addAll(uploadMessages.values());
        this.firmwareMessages.addAll(this.firmwareMessages.stream()
                .map(message -> activationMessages.get(String.valueOf(message.getId())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    private void compareAndSwapUploadMessage(Map<FirmwareType, DeviceMessage> uploadMessages, DeviceMessage candidate) {
        Optional<FirmwareVersion> version = getFirmwareVersionFromMessage(candidate);
        if (version.isPresent()) {
            FirmwareType key = version.get().getFirmwareType();
            DeviceMessage oldMessage = uploadMessages.get(key);
            if (oldMessage == null || !oldMessage.getReleaseDate().isAfter(candidate.getReleaseDate())) {
                uploadMessages.put(key, candidate);
            }
        }
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public Optional<DeviceMessage> getUploadMessageForActivationMessage(DeviceMessage activationMessage) {
        return getFirmwareMessages().stream()
                .filter(candidate -> DeviceMessageStatus.CONFIRMED.equals(candidate.getStatus()))
                .filter(candidate -> String.valueOf(candidate.getId()).equals(activationMessage.getTrackingId()))
                .findFirst();
    }

    @Override
    public Optional<DeviceMessage> getActivationMessageForUploadMessage(DeviceMessage uploadMessage) {
        return getFirmwareMessages().stream()
                .filter(candidate -> DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(candidate.getDeviceMessageId()))
                .filter(candidate -> String.valueOf(uploadMessage.getId()).equals(candidate.getTrackingId()))
                .findFirst();
    }

    @Override
    public Optional<ProtocolSupportedFirmwareOptions> getUploadOptionFromMessage(DeviceMessage message) {
        DeviceMessageId deviceMessageId = message.getDeviceMessageId();
        if (this.uploadOptionsCache.containsKey(deviceMessageId)) {
            return this.uploadOptionsCache.get(deviceMessageId);
        }
        Optional<ProtocolSupportedFirmwareOptions> uploadOption = deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(deviceMessageId);
        if (!uploadOption.isPresent() && DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(message.getDeviceMessageId())) {
            uploadOption = Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
        }
        this.uploadOptionsCache.put(deviceMessageId, uploadOption);
        return uploadOption;
    }

    @Override
    public Optional<Instant> getActivationDateFromMessage(DeviceMessage message) {
        Optional<DeviceMessageAttribute> activationDateMessageAttr = message.getAttributes().stream()
                .map(DeviceMessageAttribute.class::cast)        //Downcast to Connexo DeviceMessageAttribute
                .filter(deviceMessageAttribute -> {
                    if (deviceMessageAttribute.getSpecification() != null) {
                        return deviceMessageAttribute.getSpecification().getValueFactory().getValueType().equals(Date.class);
                    }
                    return false;
                })
                .findFirst();
        return activationDateMessageAttr.map(deviceMessageAttribute -> ((Date) deviceMessageAttribute.getValue()).toInstant());
    }

    @Override
    public Optional<FirmwareVersion> getFirmwareVersionFromMessage(DeviceMessage message) {
        Optional<PropertySpec> firmwareVersionPropertySpec = message.getSpecification().getPropertySpecs()
                .stream()
                .filter(propertySpec -> propertySpec.getValueFactory().getValueType().equals(BaseFirmwareVersion.class))
                .findFirst();

        if (firmwareVersionPropertySpec.isPresent()) {
            String name = firmwareVersionPropertySpec.get().getName();
            return message.getAttributes().stream()
                    .map(DeviceMessageAttribute.class::cast)        //Downcast to Connexo DeviceMessageAttribute
                    .filter(attr -> name.equals(attr.getName()))
                    .findFirst()
                    .map(deviceMessageAttribute -> (FirmwareVersion) deviceMessageAttribute.getValue());
        } else {
            if (DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(message.getDeviceMessageId())) {
                Optional<DeviceMessage> uploadMessage = getUploadMessageForActivationMessage(message);
                if (uploadMessage.isPresent()) {
                    return getFirmwareVersionFromMessage(uploadMessage.get());
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean messageContainsActiveFirmwareVersion(DeviceMessage message) {
        Optional<FirmwareVersion> versionFromMessage = getFirmwareVersionFromMessage(message);
        if (versionFromMessage.isPresent()) {
            Optional<ActivatedFirmwareVersion> activeFirmwareVersion = firmwareService.getActiveFirmwareVersion(this.device, versionFromMessage.get().getFirmwareType());
            return activeFirmwareVersion.isPresent()
                    && activeFirmwareVersion.get().getFirmwareVersion().getFirmwareVersion().equals(versionFromMessage.get().getFirmwareVersion());
        }
        return false;
    }

    @Override
    public boolean isFirmwareUploadTaskBusy() {
        return getFirmwareComTaskExecution()
                .map(ComTaskExecution::getStatus)
                .map(BUSY_TASK_STATUSES::contains)
                .orElse(false);
    }

    @Override
    public boolean verifyFirmwareVersionTaskIsBusy() {
        return this.getComTaskExecutionToCheckTheFirmwareVersion()
                .map(ComTaskExecution::getStatus)
                .map(BUSY_TASK_STATUSES::contains)
                .orElse(false);
    }

    @Override
    public boolean isFirmwareUploadTaskFailed() {
        return getFirmwareComTaskExecution()
                .map(ComTaskExecution::isLastExecutionFailed)
                .orElse(false);
    }

    @Override
    public boolean verifyFirmwareVersionTaskIsFailed() {
        return this.getComTaskExecutionToCheckTheFirmwareVersion()
                .map(ComTaskExecution::isLastExecutionFailed)
                .orElse(false);
    }

    @Override
    public String translate(String key) {
        return this.thesaurus.getString(key, key);
    }

    @Override
    public Optional<ComTaskExecution> getFirmwareComTaskExecution() {
        return Optional.ofNullable(firmwareComTaskExecution);
    }

    @Override
    public Optional<ComTaskExecution> lockFirmwareComTaskExecution() {
        if (firmwareComTaskExecution != null) {
            connectionTaskService.findAndLockConnectionTaskById(this.firmwareComTaskExecution.getConnectionTaskId());
            firmwareComTaskExecution = communicationTaskService.findAndLockComTaskExecutionById(firmwareComTaskExecution.getId()).orElse(null);
        }
        return getFirmwareComTaskExecution();
    }

    @Override
    public Optional<ComTask> getFirmwareTask() {
        return taskService.findFirmwareComTask();
    }

    @Override
    public Optional<ComTaskExecution> getComTaskExecutionToCheckTheFirmwareVersion() {
        return device.getComTaskExecutions().stream()
                .filter(ComTaskExecution::isConfiguredToReadStatusInformation)
                .findFirst();
    }

    @Override
    public Optional<ComTaskEnablement> getComTaskEnablementToCheckTheFirmwareVersion() {
        return device.getDeviceConfiguration().getComTaskEnablements()
                .stream()
                .filter(candidate -> candidate.getComTask().getProtocolTasks().stream()
                        .anyMatch(action -> action instanceof StatusInformationTask))
                .findFirst();
    }

    @Override
    public Optional<ComTask> getFirmwareCheckTask() {
        return getComTaskEnablementToCheckTheFirmwareVersion().map(ComTaskEnablement::getComTask);
    }

    @Override
    public List<DeviceMessage> getFirmwareMessages() {
        return firmwareMessages;
    }

    @Override
    public List<DeviceMessage> getPendingFirmwareMessages() {
        return getFirmwareMessages().stream().filter(message -> FirmwareManagementDeviceUtilsImpl.PENDING_STATUSES.contains(message.getStatus())).collect(Collectors.toList());
    }

    @Override
    public Instant getCurrentInstant() {
        return Instant.now();
    }

    @Override
    public boolean cancelPendingFirmwareUpdates(FirmwareType firmwareType) {
        boolean noUpdatesAreOngoing = true;
        for (DeviceMessage firmwareMessage : getFirmwareMessages()) {
            Optional<FirmwareVersion> targetFirmwareVersion = getFirmwareVersionFromMessage(firmwareMessage);
            if (targetFirmwareVersion.isPresent()
                    && firmwareType.equals(targetFirmwareVersion.get().getFirmwareType())
                    && FirmwareManagementDeviceUtilsImpl.PENDING_STATUSES.contains(firmwareMessage.getStatus())) {
                if (!DeviceMessageStatus.WAITING.equals(firmwareMessage.getStatus()) && isFirmwareUploadTaskBusy()) {
                    noUpdatesAreOngoing = false;
                } else {
                    firmwareMessage.revoke();
                }
            }
        }
        return noUpdatesAreOngoing;
    }

    @Override
    public boolean isPendingMessage(DeviceMessage upgradeMessage) {
        return FirmwareManagementDeviceUtilsImpl.PENDING_STATUSES.contains(upgradeMessage.getStatus());
    }

    @Override
    public boolean firmwareTaskIsScheduled() {
        Optional<ComTaskExecution> firmwareComTaskExecution = getFirmwareComTaskExecution();
        return firmwareComTaskExecution.isPresent() && firmwareComTaskExecution.get().getNextExecutionTimestamp() != null;
    }

    @Override
    public boolean isReadOutAfterLastFirmwareUpgrade() {
        Set<DeviceMessageStatus> badStatuses = EnumSet.of(DeviceMessageStatus.CANCELED, DeviceMessageStatus.FAILED);
        Optional<Instant> lastUpgrade = getFirmwareMessages().stream()
                .filter(message -> !badStatuses.contains(message.getStatus()))
                .map(DeviceMessage::getReleaseDate)
                .max(Comparator.naturalOrder());
        Optional<Instant> lastReadOut = device.getComTaskExecutions().stream()
                .filter(ComTaskExecution::isConfiguredToReadStatusInformation)
                .map(ComTaskExecution::getLastSuccessfulCompletionTimestamp)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());
        return lastReadOut
                .filter(readOut -> !lastUpgrade.isPresent() || readOut.isAfter(lastUpgrade.get()))
                .isPresent();
    }
}
