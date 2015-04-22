package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ComTask;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DeviceFirmwareVersionUtils {
    public static final Set<DeviceMessageStatus> PENDING_STATUSES = EnumSet.of(DeviceMessageStatus.WAITING, DeviceMessageStatus.PENDING, DeviceMessageStatus.SENT);
    public static final Set<TaskStatus> BUSY_TASK_STATUSES = EnumSet.of(TaskStatus.Busy, TaskStatus.Retrying);

    private final Thesaurus thesaurus;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;

    private Device device;
    private FirmwareComTaskExecution comTaskExecution;
    private List<DeviceMessage<Device>> firmwareMessages;
    private Map<DeviceMessageId, Optional<ProtocolSupportedFirmwareOptions>> uploadOptionsCache;

    @Inject
    public DeviceFirmwareVersionUtils(Thesaurus thesaurus, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.thesaurus = thesaurus;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    public DeviceFirmwareVersionUtils onDevice(Device device){
        this.device = device;
        this.comTaskExecution = (FirmwareComTaskExecution) device.getComTaskExecutions()
                .stream()
                .filter(comTaskExecution -> comTaskExecution instanceof FirmwareComTaskExecution)
                .findFirst()
                .orElse(null);
        this.firmwareMessages = device.getMessages()
                .stream()
                .filter(candidate -> candidate.getSpecification().getCategory().getId() == deviceMessageSpecificationService.getFirmwareCategory().getId())
                .filter(candidate -> !DeviceMessageStatus.REVOKED.equals(candidate.getStatus()))
                .collect(Collectors.toList());
        this.uploadOptionsCache = new HashMap<>();
        return this;
    }

    public boolean taskIsBusy(){
        return BUSY_TASK_STATUSES.contains(this.comTaskExecution.getStatus());
    }

    public boolean taskIsFailed(){
        return TaskStatus.Failed.equals(this.comTaskExecution.getStatus());
    }

    public boolean isTaskLastSuccessLessOrEqualTo(Instant instant){
        Instant lastSuccessfulCompletionTimestamp = getComTaskExecution().getLastSuccessfulCompletionTimestamp();
        return lastSuccessfulCompletionTimestamp != null && !lastSuccessfulCompletionTimestamp.isAfter(instant);
    }

    public Optional<FirmwareVersion> getFirmwareVersionFromMessage(DeviceMessage<Device> message){
        Optional<DeviceMessageAttribute> firmwareVersionMessageAttr = message.getAttributes().stream()
                .filter(attr -> DeviceMessageConstants.firmwareUpdateFileAttributeName.equals(attr.getName()))
                .findFirst();
        return firmwareVersionMessageAttr.isPresent() ?
                Optional.of((FirmwareVersion) firmwareVersionMessageAttr.get().getValue()):
                Optional.empty();
    }

    public Optional<ProtocolSupportedFirmwareOptions> getUploadOptionFromMessage(DeviceMessage<Device> message){
        DeviceMessageId deviceMessageId = message.getDeviceMessageId();
        if (this.uploadOptionsCache.containsKey(deviceMessageId)){
            return this.uploadOptionsCache.get(deviceMessageId);
        }
        Optional<ProtocolSupportedFirmwareOptions> uploadOption = deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(deviceMessageId);
        this.uploadOptionsCache.put(deviceMessageId, uploadOption);
        return uploadOption;
    }

    public Optional<Instant> getActivationDateFromMessage(DeviceMessage<Device> message){
        Optional<DeviceMessageAttribute> activationDateMessageAttr = message.getAttributes().stream()
                .filter(attr -> DeviceMessageConstants.firmwareUpdateActivationDateAttributeName.equals(attr.getName()))
                .findFirst();
        return activationDateMessageAttr.isPresent() ?
                Optional.of(((Date) activationDateMessageAttr.get().getValue()).toInstant()):
                Optional.<Instant>empty();
    }

    public Optional<DeviceMessage<Device>> getLastFirmwareActivationMessage(DeviceMessage<Device> message){
        return this.firmwareMessages.stream()
                .filter(candidate -> DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(candidate.getDeviceMessageId()))
                .filter(candidate -> candidate.getModTime().isAfter(message.getModTime()))
                .sorted((m1, m2) -> -m1.getReleaseDate().compareTo(m2.getReleaseDate()))
                .findFirst();
    }

    public Optional<DeviceMessage<Device>> getLastSuccessfulUploadMessageFor(DeviceMessage<Device> activationMessage){
        return this.getFirmwareMessages().stream()
                .filter(message -> DeviceMessageStatus.CONFIRMED.equals(message))
                .filter(message -> activationMessage.getModTime().isAfter(message.getModTime()))
                .filter(message -> {
                    Optional<ProtocolSupportedFirmwareOptions> uploadOption = getUploadOptionFromMessage(message);
                    return uploadOption.isPresent() && ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.equals(uploadOption.get());
                })
                .filter(message -> isTaskLastSuccessLessOrEqualTo(message.getModTime()))
                .findFirst();
    }

    public String translate(String key){
        return this.thesaurus.getString(key, key);
    }

    public FirmwareComTaskExecution getComTaskExecution() {
        return comTaskExecution;
    }

    public Optional<ComTaskExecution> getBasicCheckExecution(){
        return device.getComTaskExecutions().stream()
                .filter(execution -> execution.getComTasks().stream().filter(getComTaskHasBasicCheckAction()).findAny().isPresent())
                .findFirst();
    }

    private Predicate<ComTask> getComTaskHasBasicCheckAction() {
        return task -> task.getProtocolTasks().stream().filter(action -> action instanceof BasicCheckTask).findAny().isPresent();
    }

    public List<DeviceMessage<Device>> getFirmwareMessages() {
        return firmwareMessages;
    }
}
