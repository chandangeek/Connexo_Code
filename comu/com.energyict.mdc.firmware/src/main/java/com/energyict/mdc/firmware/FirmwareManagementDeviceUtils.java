package com.energyict.mdc.firmware;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.device.data.tasks.TaskStatus;
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
 * This is stateful utilities class. You MUST create a new instance for every device.
 */
public class FirmwareManagementDeviceUtils {
    public static final Set<DeviceMessageStatus> PENDING_STATUSES = EnumSet.of(DeviceMessageStatus.WAITING, DeviceMessageStatus.PENDING, DeviceMessageStatus.SENT);
    public static final Set<TaskStatus> BUSY_TASK_STATUSES = EnumSet.of(TaskStatus.Busy, TaskStatus.Retrying);

    private final Thesaurus thesaurus;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final FirmwareService firmwareService;
    private final TaskService taskService;

    private final Device device;
    private Optional<ComTaskExecution> firmwareExecution;
    private List<DeviceMessage<Device>> firmwareMessages;
    private Map<DeviceMessageId, Optional<ProtocolSupportedFirmwareOptions>> uploadOptionsCache;

    public static class Factory {
        private final Thesaurus thesaurus;
        private final DeviceMessageSpecificationService deviceMessageSpecificationService;
        private final FirmwareService firmwareService;
        private final TaskService taskService;

        @Inject
        public Factory(Thesaurus thesaurus, DeviceMessageSpecificationService deviceMessageSpecificationService, FirmwareService firmwareService, TaskService taskService) {
            this.thesaurus = thesaurus;
            this.deviceMessageSpecificationService = deviceMessageSpecificationService;
            this.firmwareService = firmwareService;
            this.taskService = taskService;
        }

        public FirmwareManagementDeviceUtils onDevice(Device device) {
            FirmwareManagementDeviceUtils utils = new FirmwareManagementDeviceUtils(thesaurus, deviceMessageSpecificationService, firmwareService, taskService, device);
            utils.firmwareExecution =
                    this.taskService
                            .findFirmwareComTask()
                            .map(ct -> device.getComTaskExecutions().stream().filter(cte -> cte.executesComTask(ct)).findFirst())
                            .orElse(Optional.<ComTaskExecution>empty());
            return utils;
        }

        public FirmwareManagementDeviceUtils onDevice(Device device, FirmwareComTaskExecution comTaskExecution) {
            FirmwareManagementDeviceUtils utils = this.onDevice(device);
            utils.firmwareExecution = Optional.ofNullable(comTaskExecution);
            return utils;
        }
    }

    private FirmwareManagementDeviceUtils(Thesaurus thesaurus, DeviceMessageSpecificationService deviceMessageSpecificationService, FirmwareService firmwareService, TaskService taskService, Device device) {
        this.thesaurus = thesaurus;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.firmwareService = firmwareService;
        this.taskService = taskService;
        this.device = device;
        this.uploadOptionsCache = new HashMap<>();
        this.firmwareMessages = new ArrayList<>();
        Map<FirmwareType, DeviceMessage<Device>> uploadMessages = new HashMap<>();
        Map<String , DeviceMessage<Device>> activationMessages = new HashMap<>();
        for (DeviceMessage<Device> candidate : device.getMessages()) {
            // only firmware upgrade, no revoked messages and only one message for each firmware type
            if (   candidate.getSpecification().getCategory().getId() == deviceMessageSpecificationService.getFirmwareCategory().getId()
                && !DeviceMessageStatus.REVOKED.equals(candidate.getStatus())) {
                if (!DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(candidate.getDeviceMessageId())) {
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

    public Optional<Instant> getActivationDateFromMessage(DeviceMessage<Device> message){
        Optional<DeviceMessageAttribute> activationDateMessageAttr = message.getAttributes().stream()
                .filter(attr -> DeviceMessageConstants.firmwareUpdateActivationDateAttributeName.equals(attr.getName()))
                .findFirst();
        return activationDateMessageAttr.isPresent() ?
                Optional.of(((Date) activationDateMessageAttr.get().getValue()).toInstant()):
                Optional.<Instant>empty();
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

    public boolean messageContainsActiveFirmwareVersion(DeviceMessage<Device> message){
        Optional<FirmwareVersion> versionFromMessage = getFirmwareVersionFromMessage(message);
        if (versionFromMessage.isPresent()){
            Optional<ActivatedFirmwareVersion> activeFirmwareVersion = firmwareService.getActiveFirmwareVersion(this.device, versionFromMessage.get().getFirmwareType());
            return activeFirmwareVersion.isPresent()
                    && activeFirmwareVersion.get().getFirmwareVersion().getFirmwareVersion().equals(versionFromMessage.get().getFirmwareVersion());
        }
        return false;
    }

    public boolean taskIsBusy() {
        return this.firmwareExecution
                .map(ComTaskExecution::getStatus)
                .map(BUSY_TASK_STATUSES::contains)
                .orElse(false);
    }

    public boolean checkTaskIsBusy() {
        return this.getFirmwareCheckExecution()
                .map(ComTaskExecution::getStatus)
                .map(BUSY_TASK_STATUSES::contains)
                .orElse(false);
    }

    public boolean taskIsFailed(){
        return this.firmwareExecution
                .map(ComTaskExecution::isLastExecutionFailed)
                .orElse(false);
    }

    public boolean checkTaskIsFailed(){
        return this.getFirmwareCheckExecution()
                .map(ComTaskExecution::isLastExecutionFailed)
                .orElse(false);
    }

    public String translate(String key){
        return this.thesaurus.getString(key, key);
    }

    public Optional<ComTaskExecution> getFirmwareExecution() {
        return this.firmwareExecution;
    }

    public Optional<ComTask> getFirmwareTask(){
        return taskService.findFirmwareComTask();
    }

    public Optional<ComTaskExecution> getFirmwareCheckExecution(){
        return getFirmwareCheckExecution(this.device);
    }

    public Optional<ComTaskEnablement> getFirmwareCheckEnablement(){
        return getFirmwareCheckEnablement(this.device);
    }

    public Optional<ComTask> getFirmwareCheckTask(){
        return getFirmwareCheckTask(this.device);
    }

    public List<DeviceMessage<Device>> getFirmwareMessages() {
        return this.firmwareMessages;
    }

    public Instant getCurrentInstant(){
        return Instant.now();
    }

    public boolean cancelPendingFirmwareUpdates(FirmwareType firmwareType){
        boolean someUpdatesAreOngoing = false;
        for (DeviceMessage<Device> firmwareMessage : getFirmwareMessages()) {
            Optional<FirmwareVersion> targetFirmwareVersion = getFirmwareVersionFromMessage(firmwareMessage);
            if (targetFirmwareVersion.isPresent()
                    && firmwareType.equals(targetFirmwareVersion.get().getFirmwareType())
                    && FirmwareManagementDeviceUtils.PENDING_STATUSES.contains(firmwareMessage.getStatus())){
                if (!DeviceMessageStatus.WAITING.equals(firmwareMessage.getStatus()) && taskIsBusy()){
                    someUpdatesAreOngoing = true;
                } else {
                    firmwareMessage.revoke();
                    firmwareMessage.save();
                }
            }
        }
        return !someUpdatesAreOngoing;
    }

    public static Optional<ComTaskExecution> getFirmwareCheckExecution(Device device){
        return device.getComTaskExecutions().stream()
                .filter(ComTaskExecution::isConfiguredToReadStatusInformation)
                .findFirst();
    }

    public static Optional<ComTaskEnablement> getFirmwareCheckEnablement(Device device) {
        return device.getDeviceConfiguration().getComTaskEnablements()
                .stream()
                .filter(candidate -> candidate.getComTask().getProtocolTasks().stream().anyMatch(action -> action instanceof StatusInformationTask))
                .findFirst();
    }

    public static Optional<ComTask> getFirmwareCheckTask(Device device){
        return getFirmwareCheckEnablement(device).map(ComTaskEnablement::getComTask);
    }
}
