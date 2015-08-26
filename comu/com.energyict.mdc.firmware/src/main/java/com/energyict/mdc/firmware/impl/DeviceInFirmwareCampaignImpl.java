package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareManagementDeviceStatus;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class DeviceInFirmwareCampaignImpl implements DeviceInFirmwareCampaign {

    public enum Fields {
        CAMPAIGN("campaign"),
        DEVICE("device"),
        STATUS("status"),
        MESSAGE_ID("firmwareMessageId"),
        STARTED_ON("startedOn"),
        FINISHED_ON("finishedOn"),
        ;

        private String name;

        Fields(String name) {
            this.name = name;
        }

        public String fieldName() {
            return this.name;
        }
    }

    private static final List<String> NON_FINAL_STATUSES = Arrays.asList(
            FirmwareManagementDeviceStatus.Constants.ONGOING,
            FirmwareManagementDeviceStatus.Constants.PENDING
    );

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<FirmwareCampaign> campaign = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
    private FirmwareManagementDeviceStatus status;
    private long firmwareMessageId;
    private Instant startedOn;
    private Instant finishedOn;

    private FirmwareManagementDeviceStatus oldStatus;
    private final DataModel dataModel;
    private final FirmwareService firmwareService;
    private final TaskService taskService;
    private final EventService eventService;
    private final Provider<FirmwareManagementDeviceUtils.Factory> helperProvider;
    private final Clock clock;

    @Inject
    public DeviceInFirmwareCampaignImpl(DataModel dataModel, FirmwareService firmwareService, TaskService taskService, EventService eventService, Provider<FirmwareManagementDeviceUtils.Factory> helperProvider, Clock clock) {
        this.dataModel = dataModel;
        this.firmwareService = firmwareService;
        this.taskService = taskService;
        this.eventService = eventService;
        this.helperProvider = helperProvider;
        this.clock = clock;
    }

    DeviceInFirmwareCampaign init(FirmwareCampaign campaign, Device device) {
        this.campaign.set(campaign);
        this.device.set(device);
        return this;
    }

    @Override
    public FirmwareManagementDeviceStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(FirmwareManagementDeviceStatus status) {
        if (status != null) {
            this.oldStatus = this.status;
            this.status = status;
            if (!NON_FINAL_STATUSES.contains(this.status.key()) && this.finishedOn == null){
                this.finishedOn = clock.instant();
            }
        }
    }

    @Override
    public Device getDevice() {
        return this.device.get();
    }

    public FirmwareCampaignImpl getFirmwareCampaign() {
        return (FirmwareCampaignImpl) this.campaign.get();
    }

    public void startFirmwareProcess() {
        this.startedOn = clock.instant();
        if (!checkDeviceType() || !checkDeviceConfiguration() || !cancelPendingFirmwareUpdates()) {
            setStatus(FirmwareManagementDeviceStatus.CONFIGURATION_ERROR);
            save();
            return;
        }
        Optional<DeviceMessageId> firmwareMessageId = getFirmwareCampaign().getFirmwareMessageId();
        if (!firmwareMessageId.isPresent()) {
            setStatus(FirmwareManagementDeviceStatus.CONFIGURATION_ERROR);
            save();
            return;
        }
        if (deviceAlreadyHasTheSameVersion()) {
            setStatus(FirmwareManagementDeviceStatus.VERIFICATION_SUCCESS);
        } else {
            createFirmwareMessage(firmwareMessageId);
            setStatus(FirmwareManagementDeviceStatus.UPLOAD_PENDING);
            scheduleFirmwareTask();
        }
        save();
    }

    public FirmwareManagementDeviceStatus updateStatus(FirmwareComTaskExecution comTaskExecution) {
        FirmwareManagementDeviceStatus currentStatus = getStatus();
        if (currentStatus == null || NON_FINAL_STATUSES.contains(currentStatus.key())) {
            FirmwareManagementDeviceUtils helper = helperProvider.get().onDevice(comTaskExecution.getDevice(), comTaskExecution);
            Optional<DeviceMessage<Device>> firmwareMessage = helper.getFirmwareMessages()
                    .stream()
                    .filter(candidate -> candidate.getId() == firmwareMessageId)
                    .findFirst();
            if (firmwareMessage.isPresent()) {
                helper.getUploadOptionFromMessage(firmwareMessage.get()).ifPresent(uploadOption -> {
                    FirmwareManagementDeviceStatus.Group.getStatusGroupFor(uploadOption)
                            .get()
                            .getStatusBasedOnMessage(firmwareMessage.get(), helper)
                            .ifPresent(newStatus -> setStatus(newStatus));
                });
            } else {
                setStatus(FirmwareManagementDeviceStatus.CANCELLED);
            }
            save();
        }
        return getStatus();
    }

    @Override
    public Instant getStartedOn() {
        return this.startedOn;
    }

    @Override
    public Instant getFinishedOn() {
        return this.finishedOn;
    }

    @Override
    public void cancel() {
        setStatus(FirmwareManagementDeviceStatus.CANCELLED);
        save();
    }

    private void createFirmwareMessage(Optional<DeviceMessageId> firmwareMessageId) {
        Device.DeviceMessageBuilder deviceMessageBuilder = getDevice()
                .newDeviceMessage(firmwareMessageId.get())
                .setReleaseDate(getFirmwareCampaign().getStartedOn());
        for (Map.Entry<String, Object> property : getFirmwareCampaign().getProperties().entrySet()) {
            deviceMessageBuilder.addProperty(property.getKey(), property.getValue());
        }
        DeviceMessage<Device> firmwareMessage = deviceMessageBuilder.add();
        this.firmwareMessageId = firmwareMessage.getId();
    }

    private void scheduleFirmwareTask() {
        ComTaskExecution firmwareComTaskExec = getFirmwareComTaskExec();
        if (firmwareComTaskExec.getNextExecutionTimestamp() == null ||
                firmwareComTaskExec.getNextExecutionTimestamp().isAfter(getFirmwareCampaign().getStartedOn())) {
            firmwareComTaskExec.schedule(getFirmwareCampaign().getStartedOn());
        }
    }

    private boolean checkDeviceType() {
        Set<ProtocolSupportedFirmwareOptions> deviceTypeAllowedOptions = firmwareService.getAllowedFirmwareManagementOptionsFor(getDevice().getDeviceType());
        return !deviceTypeAllowedOptions.isEmpty() && deviceTypeAllowedOptions.contains(getFirmwareCampaign().getFirmwareManagementOption());
    }

    private boolean checkDeviceConfiguration() {
        Optional<ComTask> firmwareComTask = taskService.findFirmwareComTask();
        return firmwareComTask.isPresent() && getDevice().getDeviceConfiguration().getComTaskEnablementFor(firmwareComTask.get()).isPresent();
    }

    private boolean cancelPendingFirmwareUpdates() {
        FirmwareManagementDeviceUtils helper = helperProvider.get().onDevice(getDevice());
        return helper.cancelPendingFirmwareUpdates(getFirmwareCampaign().getFirmwareType());
    }

    private ComTaskExecution getFirmwareComTaskExec() {
        ComTask firmwareComTask = taskService.findFirmwareComTask().get();
        Predicate<ComTask> comTaskIsFirmwareComTask = comTask -> comTask.getId() == firmwareComTask.getId();
        Predicate<ComTaskExecution> executionContainsFirmwareComTask = exec -> exec.getComTasks().stream().filter(comTaskIsFirmwareComTask).count() > 0;
        return getDevice().getComTaskExecutions().stream()
                .filter(executionContainsFirmwareComTask)
                .findFirst()
                .orElseGet(() -> {
                    ComTaskEnablement comTaskEnablement = getDevice().getDeviceConfiguration().getComTaskEnablementFor(firmwareComTask).get();
                    FirmwareComTaskExecution firmwareComTaskExecution = getDevice().newFirmwareComTaskExecution(comTaskEnablement).add();
                    getDevice().save();
                    return firmwareComTaskExecution;
                });
    }

    private boolean deviceAlreadyHasTheSameVersion() {
        FirmwareVersion targetFirmwareVersion = getFirmwareCampaign().getFirmwareVersion();
        Optional<ActivatedFirmwareVersion> activeVersion = firmwareService.getActiveFirmwareVersion(getDevice(), getFirmwareCampaign().getFirmwareType());
        return activeVersion.isPresent()
                && targetFirmwareVersion != null
                && activeVersion.get().getFirmwareVersion().getId() == targetFirmwareVersion.getId();
    }

    private void save() {
        dataModel.update(this);
        if (this.status != this.oldStatus){
            eventService.postEvent(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_UPDATED.topic(), getFirmwareCampaign());
        }
    }
}
