package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.firmware.*;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
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
    private final TaskService taskService;

    private Device device;
    private Optional<ComTaskExecution> firmwareComTaskExecution;
    private List<DeviceMessage<Device>> firmwareMessages;
    private Map<DeviceMessageId, Optional<ProtocolSupportedFirmwareOptions>> uploadOptionsCache;

    @Inject
    public FirmwareManagementDeviceUtilsImpl(Thesaurus thesaurus, DeviceMessageSpecificationService deviceMessageSpecificationService, FirmwareService firmwareService, TaskService taskService) {
        this.thesaurus = thesaurus;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.firmwareService = firmwareService;
        this.taskService = taskService;
        this.uploadOptionsCache = new HashMap<>();
        this.firmwareMessages = new ArrayList<>();
    }

    public FirmwareManagementDeviceUtils initFor(Device device) {
        this.device = device;
        initFirmwareComTaskExecution();
        initFirmwareMessages();
        return this;
    }

    private void initFirmwareMessages() {
        Map<FirmwareType, DeviceMessage<Device>> uploadMessages = new HashMap<>();
        Map<String, DeviceMessage<Device>> activationMessages = new HashMap<>();
        // only firmware upgrade, no revoked messages and only one message for each firmware type
        this.device.getMessages().stream().filter(candidate -> candidate.getSpecification().getCategory().getId() == this.deviceMessageSpecificationService.getFirmwareCategory().getId()
                && !DeviceMessageStatus.REVOKED.equals(candidate.getStatus())).forEach(candidate -> {
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

    private void initFirmwareComTaskExecution() {
        this.firmwareComTaskExecution = this.taskService
                .findFirmwareComTask()
                .map(ct -> this.device.getComTaskExecutions().stream().filter(cte -> cte.executesComTask(ct)).findFirst())
                .orElse(Optional.<ComTaskExecution>empty());
    }

    private void compareAndSwapUploadMessage(Map<FirmwareType, DeviceMessage<Device>> uploadMessages, DeviceMessage<Device> candidate) {
        Optional<FirmwareVersion> version = getFirmwareVersionFromMessage(candidate);
        if (version.isPresent()) {
            FirmwareType key = version.get().getFirmwareType();
            DeviceMessage<Device> oldMessage = uploadMessages.get(key);
            if (oldMessage == null || !oldMessage.getReleaseDate().isAfter(candidate.getReleaseDate())) {
                uploadMessages.put(key, candidate);
            }
        }
    }

    @Override
    public Optional<DeviceMessage<Device>> getUploadMessageForActivationMessage(DeviceMessage<Device> activationMessage) {
        return getFirmwareMessages().stream()
                .filter(candidate -> DeviceMessageStatus.CONFIRMED.equals(candidate.getStatus()))
                .filter(candidate -> String.valueOf(candidate.getId()).equals(activationMessage.getTrackingId()))
                .findFirst();
    }

    @Override
    public Optional<DeviceMessage<Device>> getActivationMessageForUploadMessage(DeviceMessage<Device> uploadMessage) {
        return getFirmwareMessages().stream()
                .filter(candidate -> DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(candidate.getDeviceMessageId()))
                .filter(candidate -> String.valueOf(uploadMessage.getId()).equals(candidate.getTrackingId()))
                .findFirst();
    }

    @Override
    public Optional<ProtocolSupportedFirmwareOptions> getUploadOptionFromMessage(DeviceMessage<Device> message) {
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
    public Optional<Instant> getActivationDateFromMessage(DeviceMessage<Device> message) {
        Optional<DeviceMessageAttribute> activationDateMessageAttr = message.getAttributes().stream()
                .filter(attr -> DeviceMessageConstants.firmwareUpdateActivationDateAttributeName.equals(attr.getName()))
                .findFirst();
        return activationDateMessageAttr.isPresent() ?
                Optional.of(((Date) activationDateMessageAttr.get().getValue()).toInstant()) :
                Optional.<Instant>empty();
    }

    @Override
    public Optional<FirmwareVersion> getFirmwareVersionFromMessage(DeviceMessage<Device> message) {
        Optional<DeviceMessageAttribute> firmwareVersionMessageAttr = message.getAttributes().stream()
                .filter(attr -> DeviceMessageConstants.firmwareUpdateFileAttributeName.equals(attr.getName()))
                .findFirst();
        if (!firmwareVersionMessageAttr.isPresent() && DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(message.getDeviceMessageId())) {
            Optional<DeviceMessage<Device>> uploadMessage = getUploadMessageForActivationMessage(message);
            if (uploadMessage.isPresent()) {
                return getFirmwareVersionFromMessage(uploadMessage.get());
            }
        }
        return firmwareVersionMessageAttr.isPresent() ?
                Optional.of((FirmwareVersion) firmwareVersionMessageAttr.get().getValue()) :
                Optional.empty();
    }

    @Override
    public boolean messageContainsActiveFirmwareVersion(DeviceMessage<Device> message) {
        Optional<FirmwareVersion> versionFromMessage = getFirmwareVersionFromMessage(message);
        if (versionFromMessage.isPresent()) {
            Optional<ActivatedFirmwareVersion> activeFirmwareVersion = firmwareService.getActiveFirmwareVersion(this.device, versionFromMessage.get().getFirmwareType());
            return activeFirmwareVersion.isPresent()
                    && activeFirmwareVersion.get().getFirmwareVersion().getFirmwareVersion().equals(versionFromMessage.get().getFirmwareVersion());
        }
        return false;
    }

    @Override
    public boolean firmwareUploadTaskIsBusy() {
        return this.firmwareComTaskExecution
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
    public boolean firmwareUploadTaskIsFailed() {
        return this.firmwareComTaskExecution
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
        return this.firmwareComTaskExecution;
    }

    @Override
    public Optional<ComTask> getFirmwareTask() {
        return taskService.findFirmwareComTask();
    }

    @Override
    public Optional<ComTaskExecution> getComTaskExecutionToCheckTheFirmwareVersion() {
        return getComTaskExecutionToCheckTheFirmwareVersion(this.device);
    }

    @Override
    public Optional<ComTaskEnablement> getComTaskEnablementToCheckTheFirmwareVersion() {
        return getComTaskEnablementToCheckTheFirmwareVersion(this.device);
    }

    @Override
    public Optional<ComTask> getFirmwareCheckTask() {
        return getFirmwareCheckTask(this.device);
    }

    @Override
    public List<DeviceMessage<Device>> getFirmwareMessages() {
        return this.firmwareMessages;
    }

    @Override
    public List<DeviceMessage<Device>> getPendingFirmwareMessages() {
        return getFirmwareMessages().stream().filter(message -> FirmwareManagementDeviceUtilsImpl.PENDING_STATUSES.contains(message.getStatus())).collect(Collectors.toList());
    }

    @Override
    public Instant getCurrentInstant() {
        return Instant.now();
    }

    @Override
    public boolean cancelPendingFirmwareUpdates(FirmwareType firmwareType) {
        boolean someUpdatesAreOngoing = false;
        for (DeviceMessage<Device> firmwareMessage : getFirmwareMessages()) {
            Optional<FirmwareVersion> targetFirmwareVersion = getFirmwareVersionFromMessage(firmwareMessage);
            if (targetFirmwareVersion.isPresent()
                    && firmwareType.equals(targetFirmwareVersion.get().getFirmwareType())
                    && FirmwareManagementDeviceUtilsImpl.PENDING_STATUSES.contains(firmwareMessage.getStatus())) {
                if (!DeviceMessageStatus.WAITING.equals(firmwareMessage.getStatus()) && firmwareUploadTaskIsBusy()) {
                    someUpdatesAreOngoing = true;
                } else {
                    firmwareMessage.revoke();
                    firmwareMessage.save();
                }
            }
        }
        return !someUpdatesAreOngoing;
    }

    @Override
    public boolean isPendingMessage(DeviceMessage<Device> upgradeMessage) {
        return FirmwareManagementDeviceUtilsImpl.PENDING_STATUSES.contains(upgradeMessage.getStatus());
    }

    private Optional<ComTaskExecution> getComTaskExecutionToCheckTheFirmwareVersion(Device device) {
        return device.getComTaskExecutions().stream()
                .filter(ComTaskExecution::isConfiguredToReadStatusInformation)
                .findFirst();
    }

    private Optional<ComTaskEnablement> getComTaskEnablementToCheckTheFirmwareVersion(Device device) {
        return device.getDeviceConfiguration().getComTaskEnablements()
                .stream()
                .filter(candidate -> candidate.getComTask().getProtocolTasks().stream().anyMatch(action -> action instanceof StatusInformationTask))
                .findFirst();
    }

    private Optional<ComTask> getFirmwareCheckTask(Device device) {
        return getComTaskEnablementToCheckTheFirmwareVersion(device).map(ComTaskEnablement::getComTask);
    }
}
