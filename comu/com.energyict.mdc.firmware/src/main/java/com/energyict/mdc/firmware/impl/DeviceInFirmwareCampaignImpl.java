package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareManagementDeviceStatus;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
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

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;
    private final FirmwareService firmwareService;
    private final TaskService taskService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final Provider<FirmwareManagementDeviceUtils.Factory> helperProvider;
    private final Clock clock;

    @Inject
    public DeviceInFirmwareCampaignImpl(DataModel dataModel, FirmwareService firmwareService, TaskService taskService, DeviceMessageSpecificationService deviceMessageSpecificationService, Provider<FirmwareManagementDeviceUtils.Factory> helperProvider, Clock clock) {
        this.dataModel = dataModel;
        this.firmwareService = firmwareService;
        this.taskService = taskService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
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

    public FirmwareCampaign getFirmwareCampaign() {
        return this.campaign.get();
    }

    @Override
    public void startFirmwareProcess() {
        this.startedOn = clock.instant();
        if (!checkDeviceType() || !checkDeviceConfiguration() || !cancelPendingFirmwareUpdates()) {
            setStatus(FirmwareManagementDeviceStatus.CONFIGURATION_ERROR);
            return;
        }
        Optional<DeviceMessageId> firmwareMessageId = getFirmwareMessageId();
        if (!firmwareMessageId.isPresent()) {
            setStatus(FirmwareManagementDeviceStatus.CONFIGURATION_ERROR);
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

    @Override
    public FirmwareManagementDeviceStatus updateStatus() {
        FirmwareManagementDeviceStatus currentStatus = getStatus();
        if (currentStatus != null || NON_FINAL_STATUSES.contains(currentStatus.key())) {
            FirmwareManagementDeviceUtils helper = helperProvider.get().onDevice(getDevice());
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

    private void createFirmwareMessage(Optional<DeviceMessageId> firmwareMessageId) {
        Device.DeviceMessageBuilder deviceMessageBuilder = getDevice().newDeviceMessage(firmwareMessageId.get()).setReleaseDate(getFirmwareCampaign().getPlannedDate());
        for (Map.Entry<String, Object> property : getFirmwareCampaign().getProperties().entrySet()) {
            deviceMessageBuilder.addProperty(property.getKey(), property.getValue());
        }
        DeviceMessage<Device> firmwareMessage = deviceMessageBuilder.add();
        this.firmwareMessageId = firmwareMessage.getId();
    }

    private void scheduleFirmwareTask() {
        ComTaskExecution firmwareComTaskExec = getFirmwareComTaskExec();
        if (firmwareComTaskExec.getNextExecutionTimestamp() == null ||
                firmwareComTaskExec.getNextExecutionTimestamp().isAfter(getFirmwareCampaign().getPlannedDate())) {
            firmwareComTaskExec.schedule(getFirmwareCampaign().getPlannedDate());
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
                    return getDevice().newFirmwareComTaskExecution(comTaskEnablement).add();
                });
    }

    private Optional<DeviceMessageId> getFirmwareMessageId() {
        return getDevice().getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages()
                .stream()
                .filter(firmwareMessageCandidate -> {
                    Optional<ProtocolSupportedFirmwareOptions> firmwareOptionForCandidate = deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(firmwareMessageCandidate);
                    return firmwareOptionForCandidate.isPresent() && getFirmwareCampaign().getFirmwareManagementOption().equals(firmwareOptionForCandidate.get());
                })
                .findFirst();
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
    }
}
