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
import com.elster.jupiter.util.concurrent.LockUtils;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareCheckManagementOptions;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.firmware.impl.MessageSeeds;
import com.energyict.mdc.firmware.impl.TranslationKeys;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
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
import java.util.logging.Logger;

public class FirmwareCampaignItemDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall>, DeviceInFirmwareCampaign {
    private static final String PROPERTY_NAME_RESUME = "FirmwareDeviceMessage.upgrade.resume";
    private static final Logger logger = Logger.getLogger(FirmwareCampaignItemDomainExtension.class.getName());

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
    private final DataModel ddcDataModel;
    private final DeviceMessageService deviceMessageService;
    private final ConnectionTaskService connectionTaskService;
    private final CommunicationTaskService communicationTaskService;

    private final Reference<ServiceCall> serviceCall = Reference.empty();

    @IsPresent
    private final Reference<Device> device = Reference.empty();
    private final Reference<ServiceCall> parent = Reference.empty();
    private final Reference<DeviceMessage> deviceMessage = Reference.empty();

    @Inject
    public FirmwareCampaignItemDomainExtension(FirmwareServiceImpl firmwareService, Thesaurus thesaurus, Clock clock) {
        super();
        this.firmwareService = firmwareService;
        this.dataModel = firmwareService.getDataModel();
        this.ddcDataModel = firmwareService.getOrmService().getDataModel(DeviceDataServices.COMPONENT_NAME).get();
        this.thesaurus = thesaurus;
        this.deviceMessageService = dataModel.getInstance(DeviceMessageService.class);
        this.serviceCallService = dataModel.getInstance(ServiceCallService.class);
        this.taskService = dataModel.getInstance(TaskService.class);
        this.clock = clock;
        this.firmwareCampaignService = firmwareService.getFirmwareCampaignService();
        this.connectionTaskService = dataModel.getInstance(ConnectionTaskService.class);
        this.communicationTaskService = dataModel.getInstance(CommunicationTaskService.class);
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
    public void cancel() {
        ServiceCall serviceCall = getServiceCall();
        ServiceCall lockedServiceCall = LockUtils.forceLockWithDoubleCheck(serviceCall,
                serviceCallService::lockServiceCall,
                sc -> sc.canTransitionTo(DefaultState.CANCELLED),
                sc -> cantCancelServiceCallException(serviceCall.getNumber(), sc));
        lockedServiceCall.cancel();
        this.serviceCall.set(lockedServiceCall);
    }

    void beforeCancelling() {
        if (getDeviceMessage().isPresent()){
            boolean hasRunningFirmwareTask = firmwareService.hasRunningFirmwareTask(getDevice());
            logger.info("[FWC] beforeCancelling message " + getDeviceMessage().get().getId() +
                    " with status " + getDeviceMessage().get().getStatus() +
                    " of device " + getDevice().getName() + ", has running firmware task: " + hasRunningFirmwareTask);
        }

        getDeviceMessage()
                // if already cancelled or failed, just cancel also the service call with no errors
                .filter(message -> message.getStatus() != DeviceMessageStatus.CANCELED && message.getStatus() != DeviceMessageStatus.FAILED)
                .ifPresent(message -> LockUtils.forceLockWithDoubleCheck(message,
                                deviceMessageService::findAndLockDeviceMessageById,
                                dm -> dm.getStatus() == DeviceMessageStatus.WAITING || dm.getStatus() == DeviceMessageStatus.PENDING, // pre-check
                                dm -> dm.getStatus() == DeviceMessageStatus.WAITING || dm.getStatus() == DeviceMessageStatus.PENDING // post-check
                                        && !firmwareService.hasRunningFirmwareTask(getDevice()),
                                dm -> cantCancelDeviceMessageException())
                        .revoke()
                );
    }

    private FirmwareCampaignException cantCancelServiceCallException(String serviceCallNumber, ServiceCall serviceCall) {
        return new FirmwareCampaignException(thesaurus, MessageSeeds.CANT_CANCEL_SERVICE_CALL,
                serviceCallNumber, serviceCall == null ? "no state" : serviceCall.getState().getDisplayName(thesaurus));
    }

    private FirmwareCampaignException cantRetryServiceCallException(String serviceCallNumber, ServiceCall serviceCall) {
        return new FirmwareCampaignException(thesaurus, MessageSeeds.CANT_RETRY_SERVICE_CALL,
                serviceCallNumber, serviceCall == null ? "no state" : serviceCall.getState().getDisplayName(thesaurus));
    }

    private FirmwareCampaignException cantCancelDeviceMessageException() {
        return new FirmwareCampaignException(thesaurus, MessageSeeds.FIRMWARE_UPLOAD_HAS_BEEN_STARTED_CANNOT_BE_CANCELED);
    }

    @Override
    public void retry() {
        ServiceCall serviceCall = getServiceCall();
        if (serviceCall.getParent().get().getExtension(FirmwareCampaignDomainExtension.class).get().isManuallyCancelled()) {
            throw new FirmwareCampaignException(thesaurus, MessageSeeds.CAMPAIGN_WITH_DEVICE_CANCELLED);
        }
        ServiceCall lockedServiceCall = LockUtils.forceLockWithDoubleCheck(serviceCall,
                serviceCallService::lockServiceCall,
                sc -> sc.canTransitionTo(DefaultState.PENDING),
                sc -> cantRetryServiceCallException(serviceCall.getNumber(), sc));
        lockedServiceCall.requestTransition(DefaultState.PENDING);
        lockedServiceCall.log(LogLevel.INFO, thesaurus.getSimpleFormat(MessageSeeds.RETRIED_BY_USER).format());
        this.serviceCall.set(lockedServiceCall);
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
        return serviceCall.getState().isOpen() ? null : serviceCall.getLastModificationTime();
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
        DeviceMessageId firmwareMessageId = getFirmwareCampaign().getFirmwareMessageId()
                .orElse(null);
        Device device = getDevice();
        // TODO: avoid initalization of FirmwareManagementDeviceUtils several times in same startFirmwareProcess if possible:
        // inside checksFailed -> cancelPendingFirmwareUpdates, doesAnyFirmwareRankingCheckFail, inside prepareCommunicationTask
        // TODO: avoid cancelling previous messages several times: inside checksFailed -> cancelPendingFirmwareUpdates, inside firmwareService.cancelFirmwareUploadForDevice
        if (checksFailed(firmwareMessageId)) {
            serviceCall.requestTransition(DefaultState.REJECTED);
        } else {
            try {
                prepareCommunicationTask(device);
            } catch (FirmwareCheck.FirmwareCheckException e) {
                serviceCall.log(LogLevel.WARNING, e.getLocalizedMessage());
                serviceCall.requestTransition(DefaultState.REJECTED);
            }
            firmwareService.cancelFirmwareUploadForDevice(device);
            createFirmwareMessage(firmwareMessageId, retry);
            scheduleFirmwareTask();
        }
    }

    private boolean checksFailed(DeviceMessageId firmwareMessageId) {
        boolean failed = false;
        ServiceCall serviceCall = getServiceCall();
        if (!doesDeviceTypeAllowFirmwareManagement()) {
            serviceCall.log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.DEVICE_TYPE_DOES_NOT_ALLOW_FIRMWARE_MANAGEMENT)
                    .format(getDevice().getName(), getDevice().getDeviceType().getName()));
            failed = true;
        }
        if (!doesDeviceConfigurationSupportFirmwareManagement()) {
            serviceCall.log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.DEVICE_CONFIGURATION_DOES_NOT_SUPPORT_FIRMWARE_MANAGEMENT)
                    .format(getDevice().getName(), getDevice().getDeviceConfiguration().getName()));
            failed = true;
        }
        if (!cancelPendingFirmwareUpdates()) {
            serviceCall.log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.FIRMWARE_UPLOAD_CURRENTLY_ONGOING)
                    .format(getDevice().getName()));
            failed = true;
        }
        if (firmwareMessageId == null) {
            serviceCall.log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.PROTOCOL_DOES_NOT_SUPPORT_UPLOADING_FIRMWARE)
                    .format(getDevice().getName(), getDevice().getDeviceType().getName()));
            failed = true;
        }
        Optional<ComTaskExecution> firmwareComTaskExecutionOptional = findOrCreateFirmwareComTaskExecution();
        FirmwareCampaignDomainExtension campaign = getFirmwareCampaign();
        if (firmwareComTaskExecutionOptional.isPresent()) {
            ComTaskExecution firmwareComTaskExecution = firmwareComTaskExecutionOptional.get();
            if (firmwareComTaskExecution.getConnectionTask().isPresent()) {
                ConnectionTask connectionTask = firmwareComTaskExecution.getConnectionTask().get();
                ConnectionStrategy connectionStrategy = ((ScheduledConnectionTask) connectionTask).getConnectionStrategy();
                if (!(connectionTask.isActive() && (!campaign.getFirmwareUploadConnectionStrategy().isPresent() || connectionStrategy == campaign
                        .getFirmwareUploadConnectionStrategy().get()))) {
                    serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.CONNECTION_METHOD_DOESNT_MEET_THE_REQUIREMENT).format(
                            thesaurus.getFormat(TranslationKeys.valueOf(campaign.getFirmwareUploadConnectionStrategy().get().name())).format(),
                            firmwareComTaskExecution.getComTask().getName()));
                    failed = true;
                }
            } else {
                serviceCall.log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.CONNECTION_METHOD_MISSING_ON_COMTASK)
                        .format(firmwareComTaskExecution.getComTask().getName()));
                failed = true;
            }
        } else {
            serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.TASK_FOR_SENDING_FIRMWARE_IS_MISSING).format());
            failed = true;
        }
        if (!doesConnectionWindowOverlap()) {
            serviceCall.log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.CONNECTION_WINDOW_OUTSIDE_OF_CAMPAIGN_TIME_BOUNDARY)
                    .format(getDevice().getName()));
            failed = true;
        }
        if (campaign.isWithVerification()) {
            Optional<ComTaskExecution> verificationComTaskExecutionOptional = findOrCreateVerificationComTaskExecution();
            if (verificationComTaskExecutionOptional.isPresent()) {
                ComTaskExecution verificationComTaskExecution = verificationComTaskExecutionOptional.get();
                if (verificationComTaskExecution.getConnectionTask().isPresent()) {
                    ConnectionTask connectionTask = verificationComTaskExecution.getConnectionTask().get();
                    ConnectionStrategy connectionStrategy = ((ScheduledConnectionTask) connectionTask).getConnectionStrategy();
                    if (!(connectionTask.isActive() && (!campaign.getValidationConnectionStrategy().isPresent() || connectionStrategy == campaign
                            .getValidationConnectionStrategy().get()))) {
                        serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.CONNECTION_METHOD_DOESNT_MEET_THE_REQUIREMENT).format(
                                thesaurus.getFormat(TranslationKeys.valueOf(campaign.getValidationConnectionStrategy().get().name())).format(),
                                verificationComTaskExecution.getComTask().getName()));
                        failed = true;
                    }
                } else {
                    serviceCall.log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.CONNECTION_METHOD_MISSING_ON_COMTASK)
                            .format(verificationComTaskExecution.getComTask().getName()));
                    failed = true;
                }
            } else {
                serviceCall.log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.TASK_FOR_VALIDATION_IS_MISSING).format());
                failed = true;
            }
        }
        if (doesAnyFirmwareRankingCheckFail(campaign, getDevice())) {
            failed = true;
        }
        return failed;
    }

    private void prepareCommunicationTask(Device device) {
        // the task already exists as ensured by the previous code
        firmwareService.getFirmwareManagementDeviceUtilsFor(device, true).lockFirmwareComTaskExecution();
    }

    @Override
    public FirmwareCampaignDomainExtension getFirmwareCampaign() {
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

    private boolean doesAnyFirmwareRankingCheckFail(FirmwareCampaign campaign, Device device) {
        if (campaign.getFirmwareVersion().getFirmwareType() == FirmwareType.CA_CONFIG_IMAGE) {
            return false;
        }
        FirmwareManagementDeviceUtils utils = firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        FirmwareCheckManagementOptions options = firmwareService.findFirmwareCampaignCheckManagementOptions(campaign)
                .map(FirmwareCheckManagementOptions.class::cast)
                .orElseGet(() -> firmwareService.findFirmwareManagementOptions(device.getDeviceType())
                        .map(FirmwareCheckManagementOptions.class::cast)
                        .orElse(FirmwareCheckManagementOptions.EMPTY));

        return firmwareService.getFirmwareChecks()
                .map(check -> {
                    try {
                        check.execute(options, utils, campaign.getFirmwareVersion());
                        return false;
                    } catch (FirmwareCheck.FirmwareCheckException e) {
                        getServiceCall().log(LogLevel.WARNING, "Unable to upgrade firmware version on device " + device.getName() + " due to '" + check.getName() + "' check fail: " + e.getLocalizedMessage());
                        return true;
                    }
                })
                // need to execute all checks to log all the relevant errors, so short circuit operation is not suitable here
                .reduce(Boolean::logicalOr)
                .orElse(false);
    }

    private boolean doesConnectionWindowOverlap() {
        Optional<ComTaskExecution> firmwareComTaskExec = findOrCreateFirmwareComTaskExecution();
        if (firmwareComTaskExec.isPresent()) {
            Optional<ConnectionTask<?, ?>> connectionTask = firmwareComTaskExec.get().getConnectionTask();
            if (connectionTask.isPresent() && connectionTask.get() instanceof ScheduledConnectionTask) {
                ComWindow connectionTaskComWindow = ((ScheduledConnectionTask) connectionTask.get()).getCommunicationWindow();
                if (connectionTaskComWindow != null) {
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    connectionTaskComWindow.getStart().copyTo(calendar);
                    FirmwareCampaign firmwareCampaign = getFirmwareCampaign();
                    ComWindow comWindow = firmwareCampaign.getComWindow();
                    return comWindow.includes(calendar);
                }
            }
        }
        return true;
    }

    @Override
    public boolean doesDeviceAlreadyHaveTheSameVersion() {
        FirmwareVersion targetFirmwareVersion = getFirmwareCampaign().getFirmwareVersion();
        Optional<ActivatedFirmwareVersion> activeVersion = firmwareService.getActiveFirmwareVersion(getDevice(), getFirmwareCampaign().getFirmwareType());
        return activeVersion.isPresent() && activeVersion.get().getFirmwareVersion().equals(targetFirmwareVersion);
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
        FirmwareCampaign campaign = getFirmwareCampaign();
        Optional<ComTaskExecution> optionalFirmwareComTaskExec = findOrCreateFirmwareComTaskExecution();
        if (optionalFirmwareComTaskExec.isPresent()) {
            ComTaskExecution firmwareComTaskExec = optionalFirmwareComTaskExec.get();
            Instant appliedStartDate = getAppliedStartDate(campaign);
            ScheduledConnectionTask connectionTask = (ScheduledConnectionTask) firmwareComTaskExec.getConnectionTask().get();
            if (!firmwareComTaskExec.isOnHold() && connectionTask.isActive()
                    && (!campaign.getFirmwareUploadConnectionStrategy().isPresent()
                    || connectionTask.getConnectionStrategy() == campaign.getFirmwareUploadConnectionStrategy().get())) {
                connectionTaskService.findAndLockConnectionTaskById(connectionTask.getId());
                ComTaskExecution lockedCTE = communicationTaskService.findAndLockComTaskExecutionById(firmwareComTaskExec.getId()).get();
                ScheduledConnectionTask lockedCT = (ScheduledConnectionTask) lockedCTE.getConnectionTask().get();
                if (!lockedCTE.isOnHold() && lockedCT.isActive()
                        && (!campaign.getFirmwareUploadConnectionStrategy().isPresent()
                        || lockedCT.getConnectionStrategy() == campaign.getFirmwareUploadConnectionStrategy().get())) {
                    lockedCTE.schedule(appliedStartDate);
                } else {
                    serviceCallService.lockServiceCall(getServiceCall().getId())
                            .ifPresent(serviceCall::set);
                    getDeviceMessage().ifPresent(DeviceMessage::revoke);
                    getServiceCall().log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.CONNECTION_METHOD_DOESNT_MEET_THE_REQUIREMENT)
                            .format(thesaurus.getFormat(TranslationKeys.valueOf(campaign.getFirmwareUploadConnectionStrategy().get().name())).format(), lockedCTE.getComTask().getName()));
                    getServiceCall().requestTransition(DefaultState.REJECTED);
                }
            } else {
                serviceCallService.lockServiceCall(getServiceCall().getId())
                        .ifPresent(serviceCall::set);
                getDeviceMessage().ifPresent(DeviceMessage::revoke);
                getServiceCall().log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.CONNECTION_METHOD_DOESNT_MEET_THE_REQUIREMENT)
                        .format(thesaurus.getFormat(TranslationKeys.valueOf(campaign.getFirmwareUploadConnectionStrategy().get().name())).format(), firmwareComTaskExec.getComTask().getName()));
                getServiceCall().requestTransition(DefaultState.REJECTED);
            }
        } else {
            serviceCallService.lockServiceCall(getServiceCall().getId())
                    .ifPresent(serviceCall::set);
            getDeviceMessage().ifPresent(DeviceMessage::revoke);
            getServiceCall().log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.TASK_FOR_SENDING_FIRMWARE_IS_MISSING).format());
            getServiceCall().requestTransition(DefaultState.REJECTED);
        }
    }

    private Instant getAppliedStartDate(FirmwareCampaign campaign) {
        Instant now = clock.instant();

        if (campaign.getComWindow().includes(now)){
            return now;
        }

        return campaign.getUploadPeriodStart();
    }

    @Override
    public Optional<ComTaskExecution> findOrCreateFirmwareComTaskExecution() {
        Optional<ComTask> optionalComTask = taskService.findFirmwareComTask();
        if (optionalComTask.isPresent()) {
            ComTask firmwareComTask = optionalComTask.get();
            Predicate<ComTaskExecution> executionContainsFirmwareComTask = exec -> exec.getComTask().getId() == firmwareComTask.getId();
            return Optional.ofNullable(getDevice().getComTaskExecutions().stream()
                    .filter(executionContainsFirmwareComTask)
                    .findFirst()
                    .orElseGet(() -> {
                        ComTaskEnablement comTaskEnablement = getDevice().getDeviceConfiguration().getComTaskEnablementFor(firmwareComTask).get();
                        ComTaskExecution firmwareComTaskExecution = getDevice().newFirmwareComTaskExecution(comTaskEnablement).add();
                        setDevice(ddcDataModel.mapper(Device.class).getOptional(getDevice().getId()).get()); // to re-fetch connection task & com task executions after update
                        return firmwareComTaskExecution;
                    }));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ComTaskExecution> findOrCreateVerificationComTaskExecution() {
        Device device = getDevice();
        return device.getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getId() == getFirmwareCampaign().getValidationComTaskId())
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().isManualSystemTask())
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks().stream()
                        .anyMatch(task -> task instanceof StatusInformationTask))
                .filter(comTaskEnablement -> !comTaskEnablement.isSuspended())
                .filter(comTaskEnablement -> (firmwareCampaignService.findComTaskExecution(device, comTaskEnablement) == null)
                        || (!firmwareCampaignService.findComTaskExecution(device, comTaskEnablement).isOnHold()))
                .findAny()
                .map(comTaskEnablement -> device.getComTaskExecutions().stream()
                        .filter(comTaskExecution -> comTaskExecution.getComTask().equals(comTaskEnablement.getComTask()))
                        .findAny()
                        .orElseGet(() -> {
                            ComTaskExecution comTaskExecution = device.newAdHocComTaskExecution(comTaskEnablement).add();
                            setDevice(ddcDataModel.mapper(Device.class).getOptional(device.getId()).get()); // to re-fetch connection task & com task executions after update
                            return comTaskExecution;
                        }));
    }

    @Override
    public void delete() {
        getServiceCall().delete();
    }

    @Override
    public boolean isPresent() {
        return false;
    }
}
