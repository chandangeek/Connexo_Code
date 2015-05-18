package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareManagementDeviceStatus;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DeviceInFirmwareCampaignImpl implements DeviceInFirmwareCampaign {

    public enum Fields {
        CAMPAIGN("campaign"),
        DEVICE("device"),
        STATUS("status"),
        MESSAGE_ID("firmwareMessageId"),;

        private String name;

        Fields(String name) {
            this.name = name;
        }

        public String fieldName() {
            return this.name;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<FirmwareCampaign> campaign = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
    private FirmwareManagementDeviceStatus status;
    private long firmwareMessageId;

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

    @Inject
    public DeviceInFirmwareCampaignImpl(DataModel dataModel, FirmwareService firmwareService, TaskService taskService, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.dataModel = dataModel;
        this.firmwareService = firmwareService;
        this.taskService = taskService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
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
        this.status = status;
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
        if (!checkDeviceType() || !checkDeviceConfiguration() || !checkDevice()) {
            setStatus(FirmwareManagementDeviceStatus.CONFIGURATION_ERROR);
            return;
        }
        Optional<DeviceMessageId> firmwareMessageId = getFirmwareMessageId();
        if (!firmwareMessageId.isPresent()){
            setStatus(FirmwareManagementDeviceStatus.CONFIGURATION_ERROR);
            return;
        }
        createFirmwareMessage(firmwareMessageId);
        scheduleFirmwareTask();
        setStatus(FirmwareManagementDeviceStatus.PENDING);
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
                firmwareComTaskExec.getNextExecutionTimestamp().isAfter(getFirmwareCampaign().getPlannedDate())){
            firmwareComTaskExec.schedule(getFirmwareCampaign().getPlannedDate());
        }
    }

    private boolean checkDeviceType() {
        Set<ProtocolSupportedFirmwareOptions> deviceTypeAllowedOptions = firmwareService.getAllowedFirmwareManagementOptionsFor(getDevice().getDeviceType());
        return !deviceTypeAllowedOptions.isEmpty() && deviceTypeAllowedOptions.contains(getFirmwareCampaign().getUpgradeOption());
    }

    private boolean checkDeviceConfiguration() {
        Optional<ComTask> firmwareComTask = taskService.findFirmwareComTask();
        return firmwareComTask.isPresent() && getDevice().getDeviceConfiguration().getComTaskEnablementFor(firmwareComTask.get()).isPresent();
    }

    private boolean checkDevice(){
        int firmwareCategoryId = deviceMessageSpecificationService.getFirmwareCategory().getId();
        Set<DeviceMessageStatus> pendingStatuses = EnumSet.of(DeviceMessageStatus.WAITING, DeviceMessageStatus.PENDING, DeviceMessageStatus.SENT);
        return getDevice().getMessages().stream()
                .filter(candidate -> candidate.getSpecification() != null)
                .filter(candidate -> candidate.getSpecification().getCategory() != null)
                .filter(candidate -> candidate.getSpecification().getCategory().getId() == firmwareCategoryId)
                .filter(candidate -> pendingStatuses.contains(candidate.getStatus()))
                .findAny()
                .isPresent();
    }

    private ComTaskExecution getFirmwareComTaskExec() {
        return getDevice().getComTaskExecutions().stream()
                .filter(comTaskExecution -> comTaskExecution instanceof FirmwareComTaskExecution)
                .findFirst()
                .orElseGet(() -> createFirmwareComTaskExec());
    }

    private ComTaskExecution createFirmwareComTaskExec() {
        ComTask comTask = taskService.findFirmwareComTask().get();
        ComTaskEnablement comTaskEnablement = getDevice().getDeviceConfiguration().getComTaskEnablementFor(comTask).get();
        return getDevice().newFirmwareComTaskExecution(comTaskEnablement).add();
    }

    private Optional<DeviceMessageId> getFirmwareMessageId(){
        return getDevice().getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages()
                .stream()
                .filter(firmwareMessageCandidate -> {
                    Optional<ProtocolSupportedFirmwareOptions> firmwareOptionForCandidate = deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(firmwareMessageCandidate);
                    return firmwareOptionForCandidate.isPresent() && getFirmwareCampaign().getUpgradeOption().equals(firmwareOptionForCandidate.get());
                })
                .findFirst();
    }
}
