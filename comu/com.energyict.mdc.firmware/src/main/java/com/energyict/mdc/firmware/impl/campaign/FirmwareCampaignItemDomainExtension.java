/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.firmware.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Calendar;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Predicate;

public class FirmwareCampaignItemDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall>, DeviceInFirmwareCampaign {

    private static final String PROPERTY_NAME_RESUME = "FirmwareDeviceMessage.upgrade.resume";

    public enum FieldNames {
        DOMAIN("serviceCall", "service_call"),
        PARENT("parent", "parent"),
        DEVICE("device", "device"),
        DEVICE_MESSAGE("deviceMessage", "device_message_id");

        FieldNames(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        private final String javaName;
        private final String databaseName;

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }
    }

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final ServiceCallService serviceCallService;
    private final FirmwareServiceImpl firmwareService;
    private final TaskService taskService;
    private final Clock clock;
    private final FirmwareCampaignServiceImpl firmwareCampaignService;

    private Reference<ServiceCall> serviceCall = Reference.empty();

    @IsPresent
    private Reference<Device> device = Reference.empty();
    private Reference<ServiceCall> parent = Reference.empty();
    private Reference<DeviceMessage> deviceMessage = Reference.empty();


    @Inject
    public FirmwareCampaignItemDomainExtension(FirmwareServiceImpl firmwareService, Thesaurus thesaurus, Clock clock) {
        super();
        this.firmwareService = firmwareService;
        this.dataModel = firmwareService.getDataModel();
        this.thesaurus = thesaurus;
        this.serviceCallService = dataModel.getInstance(ServiceCallService.class);
        this.taskService = dataModel.getInstance(TaskService.class);
        this.clock = clock;
        this.firmwareCampaignService = firmwareService.getFirmwareCampaignService();
    }

    @Override
    public Device getDevice() {
        return device.get();
    }

    @Override
    public Optional<DeviceMessage> getDeviceMessage() {
        return deviceMessage.getOptional();
    }

    @Override
    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }

    @Override
    public ServiceCall cancel() {
        ServiceCall serviceCall = getServiceCall();
        if (serviceCall.canTransitionTo(DefaultState.CANCELLED)) {
            serviceCall.requestTransition(DefaultState.CANCELLED);
        }
        return serviceCallService.getServiceCall(serviceCall.getId()).get();
    }

    @Override
    public ServiceCall retry() {
        ServiceCall serviceCall = getServiceCall();
        if (serviceCall.canTransitionTo(DefaultState.PENDING)) {
            serviceCall.log(LogLevel.INFO, thesaurus.getSimpleFormat(MessageSeeds.RETRIED_BY_USER).format());
            serviceCall.requestTransition(DefaultState.PENDING);
        }
        return serviceCallService.getServiceCall(serviceCall.getId()).get();
    }

    @Override
    public ServiceCall getParent() {
        return parent.get();
    }

    @Override
    public Instant getStartedOn() {
        return getServiceCall().getCreationTime();
    }

    @Override
    public Instant getFinishedOn() {
        ServiceCall serviceCall = getServiceCall();
        return (serviceCall.getState().equals(DefaultState.CANCELLED)
                || serviceCall.getState().equals(DefaultState.SUCCESSFUL)) ? serviceCall.getLastModificationTime() : null;
    }

    @Override
    public long getId() {
        return getServiceCall().getId();
    }

    public void setParent(ServiceCall parent) {
        this.parent.set(parent);
    }

    public void setDevice(Device device) {
        this.device.set(device);
    }

    public void setDeviceMessage(DeviceMessage deviceMessage) {
        this.deviceMessage.set(deviceMessage);
    }


    @Override
    public void copyFrom(ServiceCall domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(domainInstance);
        this.setDevice((Device) propertyValues.getProperty(FieldNames.DEVICE.javaName()));
        this.setParent((ServiceCall) propertyValues.getProperty(FieldNames.PARENT.javaName()));
        this.setDeviceMessage((DeviceMessage) propertyValues.getProperty(FieldNames.DEVICE_MESSAGE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.DEVICE.javaName(), this.getDevice());
        propertySetValues.setProperty(FieldNames.PARENT.javaName(), this.getParent());
        propertySetValues.setProperty(FieldNames.DEVICE_MESSAGE.javaName(), this.getDeviceMessage().orElse(null));
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }

    public void retryFirmwareProcess() {
        startFirmwareProcess(true);
    }

    public void startFirmwareProcess() {
        startFirmwareProcess(false);
    }

    public void startFirmwareProcess(boolean retry) {
        ServiceCall serviceCall = getServiceCall();
        Optional<DeviceMessageId> firmwareMessageId = getFirmwareCampaign().getFirmwareMessageId();
        if (checksFailed(firmwareMessageId)) {
            serviceCall.requestTransition(DefaultState.REJECTED);
        } else {
            if (deviceAlreadyHasTheSameVersion()) {
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else {
                try {
                    prepareCommunicationTask(getDevice());
                } catch (FirmwareCheck.FirmwareCheckException e) {
                    serviceCall.log(LogLevel.WARNING, e.getLocalizedMessage());
                    serviceCall.requestTransition(DefaultState.REJECTED);
                }
                firmwareService.cancelFirmwareUploadForDevice(getDevice());
                createFirmwareMessage(firmwareMessageId.get(), retry);
                scheduleFirmwareTask();
            }
        }
    }

    private boolean checksFailed(Optional<DeviceMessageId> firmwareMessageId) {
        boolean failed = false;
        if (!doesDeviceTypeAllowFirmwareManagement()) {
            getServiceCall().log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.DEVICE_TYPE_DOES_NOT_ALLOW_FIRMWARE_MANAGEMENT)
                    .format(getDevice().getName(), getDevice().getDeviceType().getName()));
            failed = true;
        }
        if (!doesDeviceConfigurationSupportFirmwareManagement()) {
            getServiceCall().log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.DEVICE_CONFIGURATION_DOES_NOT_SUPPORT_FIRMWARE_MANAGEMENT)
                    .format(getDevice().getName(), getDevice().getDeviceConfiguration().getName()));
            failed = true;
        }
        if (!cancelPendingFirmwareUpdates()) {
            getServiceCall().log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.FIRMWARE_UPLOAD_CURRENTLY_ONGOING)
                    .format(getDevice().getName()));
            failed = true;
        }
        if (!firmwareMessageId.isPresent()) {
            getServiceCall().log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.PROTOCOL_DOES_NOT_SUPPORT_UPLOADING_FIRMWARE)
                    .format(getDevice().getName(), getDevice().getDeviceType().getName()));
            failed = true;
        }
        if (!doesConnectionWindowOverlap()) {
            getServiceCall().log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.CONNECTION_WINDOW_OUTSIDE_OF_CAMPAIGN_TIME_BOUNDARY)
                    .format(getDevice().getName()));
            failed = true;
        }
        if (doesAnyFirmwareRankingCheckFail(getDevice(), getFirmwareCampaign().getFirmwareVersion())) {
            failed = true;
        }
        return failed;
    }

    private void prepareCommunicationTask(Device device) {
        if (!firmwareService.getFirmwareManagementDeviceUtilsFor(device).getFirmwareComTaskExecution().isPresent()) {
            createFirmwareComTaskExecution(device);
        }
    }

    private ComTaskExecution createFirmwareComTaskExecution(Device device) {
        // Check firmware upgrade task
        ComTask comTask = taskService.findFirmwareComTask()
                .orElseThrow(() -> new FirmwareCheck.FirmwareCheckException(thesaurus, MessageSeeds.DEFAULT_FIRMWARE_MANAGEMENT_TASK_CAN_NOT_BE_FOUND));
        ComTaskEnablement comTaskEnablement = device.getDeviceConfiguration().getComTaskEnablementFor(comTask)
                .orElseThrow(() -> new FirmwareCheck.FirmwareCheckException(thesaurus, MessageSeeds.DEFAULT_FIRMWARE_MANAGEMENT_TASK_IS_NOT_ACTIVE));
        ComTaskExecution firmwareComTaskExecution = device.newFirmwareComTaskExecution(comTaskEnablement).add();
        device.save();
        return firmwareComTaskExecution;
    }

    private FirmwareCampaignDomainExtension getFirmwareCampaign() {
        return getParent().getExtension(FirmwareCampaignDomainExtension.class).get();
    }

    private boolean doesDeviceTypeAllowFirmwareManagement() {
        Set<ProtocolSupportedFirmwareOptions> deviceTypeAllowedOptions = firmwareService.getAllowedFirmwareManagementOptionsFor(getDevice().getDeviceType());
        return deviceTypeAllowedOptions.contains(getFirmwareCampaign().getFirmwareManagementOption());
    }

    private boolean doesDeviceConfigurationSupportFirmwareManagement() {
        Optional<ComTask> firmwareComTask = taskService.findFirmwareComTask();
        return firmwareComTask.isPresent() && getDevice().getDeviceConfiguration().getComTaskEnablementFor(firmwareComTask.get()).isPresent();
    }

    private boolean cancelPendingFirmwareUpdates() {
        FirmwareManagementDeviceUtils helper = this.firmwareService.getFirmwareManagementDeviceUtilsFor(getDevice());
        return helper.cancelPendingFirmwareUpdates(getFirmwareCampaign().getFirmwareType());
    }

    private boolean doesAnyFirmwareRankingCheckFail(Device device, FirmwareVersion firmwareVersion) {
        if (firmwareVersion.getFirmwareType() == FirmwareType.CA_CONFIG_IMAGE) {
            return false;
        }
        FirmwareManagementDeviceUtils utils = firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        return firmwareService.getFirmwareChecks()
                .map(check -> {
                    try {
                        check.execute(utils, firmwareVersion);
                        return false;
                    } catch (FirmwareCheck.FirmwareCheckException e) {
                        getServiceCall().log(LogLevel.WARNING, "Unable to upgrade firmware version on device " + device.getName() + " due to check fail: " + e.getLocalizedMessage());
                        return true;
                    }
                })
                // need to execute all checks to log all the relevant errors, so short circuit operation is not suitable here
                .reduce(Boolean::logicalOr)
                .orElse(false);
    }

    private boolean doesConnectionWindowOverlap() {
        Optional<ConnectionTask<?, ?>> connectionTask = getFirmwareComTaskExec().getConnectionTask();
        if (connectionTask.isPresent() && connectionTask.get() instanceof ScheduledConnectionTask) {
            ComWindow connectionTaskComWindow = ((ScheduledConnectionTask) connectionTask.get()).getCommunicationWindow();
            if (connectionTaskComWindow != null) {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(this.clock.getZone()));
                connectionTaskComWindow.getStart().copyTo(calendar);
                FirmwareCampaign firmwareCampaign = getFirmwareCampaign();
                ComWindow comWindow = firmwareCampaign.getComWindow();
                return comWindow.includes(calendar);
            }
        }
        return true;
    }

    boolean deviceAlreadyHasTheSameVersion() {
        FirmwareVersion targetFirmwareVersion = getFirmwareCampaign().getFirmwareVersion();
        Optional<ActivatedFirmwareVersion> activeVersion = firmwareService.getActiveFirmwareVersion(getDevice(), getFirmwareCampaign().getFirmwareType());
        return activeVersion.isPresent()
                && targetFirmwareVersion != null
                && activeVersion.get().getFirmwareVersion().getId() == targetFirmwareVersion.getId();
    }

    private void createFirmwareMessage(DeviceMessageId firmwareMessageId, boolean resume) {
        Device.DeviceMessageBuilder deviceMessageBuilder = getDevice()
                .newDeviceMessage(firmwareMessageId)
                .setReleaseDate(parent.get().getCreationTime());
        for (Map.Entry<String, Object> property : getFirmwareCampaign().getProperties().entrySet()) {
            if (resume && property.getKey().equals(PROPERTY_NAME_RESUME)) {
                deviceMessageBuilder.addProperty(property.getKey(), Boolean.TRUE);
                continue;
            }
            deviceMessageBuilder.addProperty(property.getKey(), property.getValue());
        }
        DeviceMessage firmwareMessage = deviceMessageBuilder.add();
        this.deviceMessage.set(firmwareMessage);
        getServiceCall().update(this);
    }

    private void scheduleFirmwareTask() {
        ComTaskExecution firmwareComTaskExec = getFirmwareComTaskExec();
        Instant appliedStartDate = parent.get().getExtension(FirmwareCampaignDomainExtension.class).get().getUploadPeriodStart();
        Optional<? extends FirmwareCampaign> campaign = getServiceCall().getParent().get().getExtension(FirmwareCampaignDomainExtension.class);
        ConnectionStrategy connectionStrategy;
        boolean isFirmwareComTaskStart = false;
        if (campaign.isPresent()) {
            if (firmwareComTaskExec.getNextExecutionTimestamp() == null ||
                    firmwareComTaskExec.getNextExecutionTimestamp().isAfter(appliedStartDate)) {
                connectionStrategy = ((ScheduledConnectionTask) firmwareComTaskExec.getConnectionTask().get()).getConnectionStrategy();
                if (!campaign.get().getFirmwareUploadConnectionStrategy().isPresent() || connectionStrategy == campaign.get().getFirmwareUploadConnectionStrategy().get()){
                    firmwareComTaskExec.schedule(appliedStartDate);
                    isFirmwareComTaskStart = true;
                }else{
                    serviceCallService.lockServiceCall(getServiceCall().getId());
                    getServiceCall().log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.CONNECTION_METHOD_DOESNT_MEET_THE_REQUIREMENT).format(campaign.get().getFirmwareUploadConnectionStrategy().get().name(), firmwareComTaskExec.getComTask().getName()));
                    getServiceCall().requestTransition(DefaultState.REJECTED);
                    return;
                }
            }
        }
        if(!isFirmwareComTaskStart){
            serviceCallService.lockServiceCall(getServiceCall().getId());
            getServiceCall().log(LogLevel.SEVERE, thesaurus.getFormat(MessageSeeds.TASK_FOR_SENDING_FIRMWARE_IS_MISSING).format(firmwareComTaskExec.getComTask().getName()));
            getServiceCall().requestTransition(DefaultState.REJECTED);
        }
    }

    private ComTaskExecution getFirmwareComTaskExec() {
        ComTask firmwareComTask = taskService.findFirmwareComTask().get();
        Predicate<ComTaskExecution> executionContainsFirmwareComTask = exec -> exec.getComTask().getId() == firmwareComTask.getId();
        return getDevice().getComTaskExecutions().stream()
                .filter(executionContainsFirmwareComTask)
                .findFirst()
                .orElseGet(() -> {
                    ComTaskEnablement comTaskEnablement = getDevice().getDeviceConfiguration().getComTaskEnablementFor(firmwareComTask).get();
                    ComTaskExecution firmwareComTaskExecution = getDevice().newFirmwareComTaskExecution(comTaskEnablement).add();
                    getDevice().save();
                    return firmwareComTaskExecution;
                });
    }
}