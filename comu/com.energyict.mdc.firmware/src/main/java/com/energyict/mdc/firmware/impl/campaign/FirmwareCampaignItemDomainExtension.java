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
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.device.config.ComTaskEnablement;
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
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;
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

    public void setParentServiceCallId(ServiceCall parent) {
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
        this.setParentServiceCallId((ServiceCall) propertyValues.getProperty(FieldNames.PARENT.javaName()));
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
        if (!doesDeviceTypeAllowFirmwareManagement()
                || !doesDeviceConfigurationSupportFirmwareManagement()
                || !cancelPendingFirmwareUpdates()
                || !firmwareMessageId.isPresent()
                || !doesConnectionWindowOverlap()
                || doesAnyFirmwareRankingCheckFail(getDevice(), getFirmwareCampaign().getFirmwareVersion())) {
            serviceCall.requestTransition(DefaultState.REJECTED);
        } else {
            if (deviceAlreadyHasTheSameVersion()) {
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else {
                FirmwareCampaign firmwareCampaign = getFirmwareCampaign();
                prepareCommunicationTask(getDevice(), firmwareCampaign.getProperties(), firmwareCampaign.getFirmwareMessageSpec().get());
                createFirmwareMessage(firmwareMessageId.get(), retry);
                scheduleFirmwareTask();
            }
        }
    }

    private void prepareCommunicationTask(Device device, Map<String, Object> convertedProperties, DeviceMessageSpec firmwareMessageSpec) {
        FirmwareManagementDeviceUtils helper = this.firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        Optional<ComTaskExecution> fuComTaskExecutionRef = helper.getFirmwareComTaskExecution();
        if (!fuComTaskExecutionRef.isPresent()) {
            createFirmwareComTaskExecution(device);
        } else {
            cancelOldFirmwareUpdates(helper, convertedProperties, firmwareMessageSpec);
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

    private void cancelOldFirmwareUpdates(FirmwareManagementDeviceUtils helper, Map<String, Object> convertedProperties, DeviceMessageSpec firmwareMessageSpec) {
        Optional<PropertySpec> firmwareVersionPropertySpec = firmwareMessageSpec.getPropertySpecs()
                .stream()
                .filter(propertySpec -> propertySpec.getValueFactory().getValueType().equals(BaseFirmwareVersion.class))
                .findAny();
        if (firmwareVersionPropertySpec.isPresent()) {
            FirmwareVersion requestedFirmwareVersion = (FirmwareVersion) convertedProperties.get(firmwareVersionPropertySpec.get().getName());
            if (requestedFirmwareVersion != null) {
                helper.cancelPendingFirmwareUpdates(requestedFirmwareVersion.getFirmwareType());
            }
        }
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

    private boolean deviceAlreadyHasTheSameVersion() {
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
    }

    private void scheduleFirmwareTask() {
        ComTaskExecution firmwareComTaskExec = getFirmwareComTaskExec();
        Instant appliedStartDate = parent.get().getExtension(FirmwareCampaignDomainExtension.class).get().getUploadPeriodStart();
        if (firmwareComTaskExec.getNextExecutionTimestamp() == null ||
                firmwareComTaskExec.getNextExecutionTimestamp().isAfter(appliedStartDate)) {
            firmwareComTaskExec.schedule(appliedStartDate);
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