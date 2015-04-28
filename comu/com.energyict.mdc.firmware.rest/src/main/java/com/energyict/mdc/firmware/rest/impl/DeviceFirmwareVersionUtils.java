package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareType;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DeviceFirmwareVersionUtils {
    public static final Set<DeviceMessageStatus> PENDING_STATUSES = EnumSet.of(DeviceMessageStatus.WAITING, DeviceMessageStatus.PENDING, DeviceMessageStatus.SENT);
    public static final Set<TaskStatus> BUSY_TASK_STATUSES = EnumSet.of(TaskStatus.Busy, TaskStatus.Retrying);

    private final Thesaurus thesaurus;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final FirmwareService firmwareService;

    private Device device;
    private FirmwareComTaskExecution comTaskExecution;
    private List<DeviceMessage<Device>> firmwareMessages;
    private Map<DeviceMessageId, Optional<ProtocolSupportedFirmwareOptions>> uploadOptionsCache;

    @Inject
    public DeviceFirmwareVersionUtils(Thesaurus thesaurus, DeviceMessageSpecificationService deviceMessageSpecificationService, FirmwareService firmwareService) {
        this.thesaurus = thesaurus;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.firmwareService = firmwareService;
    }

    public DeviceFirmwareVersionUtils onDevice(Device device){
        this.device = device;
        this.comTaskExecution = (FirmwareComTaskExecution) device.getComTaskExecutions()
                .stream()
                .filter(comTaskExecution -> comTaskExecution instanceof FirmwareComTaskExecution)
                .findFirst()
                .orElse(null);
        this.firmwareMessages = new ArrayList<>();
        this.uploadOptionsCache = new HashMap<>();
        Map<FirmwareType, DeviceMessage<Device>> uploadMessages = new HashMap<>();
        Map<String , DeviceMessage<Device>> activationMessages = new HashMap<>();
        for (DeviceMessage<Device> candidate : device.getMessages()) {
            // only firmware upgrade, no revoked messages and only one message for each firmware type
            if (candidate.getSpecification() != null
                    && candidate.getSpecification().getCategory() != null
                    && candidate.getSpecification().getCategory().getId() == deviceMessageSpecificationService.getFirmwareCategory().getId()
                    && !DeviceMessageStatus.REVOKED.equals(candidate.getStatus())){
                if (!DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(candidate.getDeviceMessageId())){
                    compareAndSwapUploadMessage(uploadMessages, candidate);
                } else {
                    activationMessages.put(candidate.getTrackingId(), candidate);
                }
            }
        }
        this.firmwareMessages.addAll(uploadMessages.values());
        this.firmwareMessages.addAll(this.firmwareMessages.stream()
                .map(message -> activationMessages.get(String.valueOf(message.getId())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return this;
    }

    private void compareAndSwapUploadMessage(Map<FirmwareType, DeviceMessage<Device>> uploadMessages, DeviceMessage<Device> candidate) {
        Optional<FirmwareVersion> version = getFirmwareVersionFromMessage(candidate);
        if (version.isPresent()){
            FirmwareType key = version.get().getFirmwareType();
            DeviceMessage<Device> oldMessage = uploadMessages.get(key);
            if (oldMessage == null || !oldMessage.getReleaseDate().isAfter(candidate.getReleaseDate())){
                uploadMessages.put(key, candidate);
            }
        }
    }

    public boolean taskIsBusy(){
        return BUSY_TASK_STATUSES.contains(this.comTaskExecution.getStatus());
    }

    public boolean taskIsFailed(){
        return getComTaskExecution().isLastExecutionFailed();
    }

    public Optional<DeviceMessage<Device>> getUploadMessageForActivationMessage(DeviceMessage<Device> activationMessage){
        return getFirmwareMessages().stream()
                .filter(candidate -> DeviceMessageStatus.CONFIRMED.equals(candidate.getStatus()))
                .filter(candidate -> String.valueOf(candidate.getId()).equals(activationMessage.getTrackingId()))
                .findFirst();
    }

    public Optional<DeviceMessage<Device>> getActivationMessageForUploadMessage(DeviceMessage<Device> uploadMessage){
        return getFirmwareMessages().stream()
                .filter(candidate -> DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(candidate.getDeviceMessageId()))
                .filter(candidate -> String.valueOf(uploadMessage.getId()).equals(candidate.getTrackingId()))
                .findFirst();
    }

    public Optional<ProtocolSupportedFirmwareOptions> getUploadOptionFromMessage(DeviceMessage<Device> message){
        DeviceMessageId deviceMessageId = message.getDeviceMessageId();
        if (this.uploadOptionsCache.containsKey(deviceMessageId)){
            return this.uploadOptionsCache.get(deviceMessageId);
        }
        Optional<ProtocolSupportedFirmwareOptions> uploadOption = deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(deviceMessageId);
        if (!uploadOption.isPresent() && DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(message.getDeviceMessageId())){
            uploadOption = Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
        }
        this.uploadOptionsCache.put(deviceMessageId, uploadOption);
        return uploadOption;
    }

    public Optional<FirmwareVersion> getFirmwareVersionFromMessage(DeviceMessage<Device> message){
        Optional<DeviceMessageAttribute> firmwareVersionMessageAttr = message.getAttributes().stream()
                .filter(attr -> DeviceMessageConstants.firmwareUpdateFileAttributeName.equals(attr.getName()))
                .findFirst();
        if (!firmwareVersionMessageAttr.isPresent() && DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(message.getDeviceMessageId())){
            Optional<DeviceMessage<Device>> uploadMessage = getUploadMessageForActivationMessage(message);
            if(uploadMessage.isPresent()){
                return getFirmwareVersionFromMessage(uploadMessage.get());
            }
        }
        return firmwareVersionMessageAttr.isPresent() ?
                Optional.of((FirmwareVersion) firmwareVersionMessageAttr.get().getValue()):
                Optional.empty();
    }

    public Optional<Instant> getActivationDateFromMessage(DeviceMessage<Device> message){
        Optional<DeviceMessageAttribute> activationDateMessageAttr = message.getAttributes().stream()
                .filter(attr -> DeviceMessageConstants.firmwareUpdateActivationDateAttributeName.equals(attr.getName()))
                .findFirst();
        return activationDateMessageAttr.isPresent() ?
                Optional.of(((Date) activationDateMessageAttr.get().getValue()).toInstant()):
                Optional.<Instant>empty();
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
        return this.firmwareMessages;
    }

    public boolean messageContainsActiveFirmwareVersion(DeviceMessage<Device> message){
        Optional<FirmwareVersion> versionFromMessage = getFirmwareVersionFromMessage(message);
        if (versionFromMessage.isPresent()){
            List<Optional<ActivatedFirmwareVersion>> activeVersions = new ArrayList<>();
            activeVersions.add(firmwareService.getCurrentMeterFirmwareVersionFor(this.device));
            activeVersions.add(firmwareService.getCurrentCommunicationFirmwareVersionFor(this.device));
            return activeVersions.stream()
                    .filter(Optional::isPresent)
                    .map(version -> version.get().getFirmwareVersion())
                    .anyMatch(active -> active.getFirmwareType().equals(versionFromMessage.get().getFirmwareType()) &&
                            active.getFirmwareVersion().equals(versionFromMessage.get().getFirmwareVersion()));
        }
        return false;
    }
}
