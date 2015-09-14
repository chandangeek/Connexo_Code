package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.Functions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.firmware.*;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DeviceFirmwareVersionInfoFactory {
    private final Thesaurus thesaurus;
    private final FirmwareService firmwareService;

    private final Map<ProtocolSupportedFirmwareOptions, List<FirmwareUpgradeState>> states;

    @Inject
    public DeviceFirmwareVersionInfoFactory(Thesaurus thesaurus, FirmwareService firmwareService) {
        this.thesaurus = thesaurus;
        this.firmwareService = firmwareService;
        states = new HashMap<>();

        initStatesForActivateOnDate();
        initStatesForActivate();
        initStatesForInstall();
    }

    private void initStatesForActivateOnDate() {
        List<FirmwareUpgradeState> activateOnDate = new ArrayList<>();
        activateOnDate.add(new UpgradeFirmwareSuccessfulFinishedState());
        activateOnDate.add(new WrongVersionVerificationFirmwareState());
        activateOnDate.add(new FailedVersionVerificationFirmwareState());
        activateOnDate.add(new OngoingVersionVerificationFirmwareState());

        activateOnDate.add(new FirmwareUploadedButNotVerifiedYetState());
        activateOnDate.add(new FailedFirmwareUploadState());
        activateOnDate.add(new FirmwareUploadedScheduledActivationState());

        activateOnDate.add(new OngoingFirmwareUploadWithActivationDateState());
        activateOnDate.add(new PendingFirmwareUploadWithActivationDateState());
        states.put(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE, activateOnDate);
    }

    private void initStatesForActivate() {
        List<FirmwareUpgradeState> activate = new ArrayList<>();
        activate.add(new UpgradeFirmwareSuccessfulFinishedState());
        activate.add(new WrongVersionVerificationFirmwareState());
        activate.add(new FailedVersionVerificationFirmwareState());
        activate.add(new OngoingVersionVerificationFirmwareState());

        activate.add(new FirmwareUploadedButNotVerifiedYetState());
        activate.add(new FailedFirmwareUploadState());
        activate.add(new OngoingFirmwareUploadProcessState());
        activate.add(new PendingFirmwareUploadState());
        states.put(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE, activate);
    }

    private void initStatesForInstall() {
        List<FirmwareUpgradeState> install = new ArrayList<>();
        install.add(new UpgradeFirmwareSuccessfulFinishedState());
        install.add(new WrongVersionVerificationFirmwareState());
        install.add(new FailedVersionVerificationFirmwareState());
        install.add(new OngoingVersionVerificationFirmwareState());

        install.add(new FirmwareUploadedButNotVerifiedYetState());
        install.add(new FailedFirmwareActivationState());
        install.add(new OngoingFirmwareActivationProcessState());

        install.add(new FirmwareUploadedButNotActivatedYetState());
        install.add(new FailedFirmwareUploadState());
        install.add(new OngoingFirmwareUploadProcessState());
        install.add(new PendingFirmwareUploadState());
        states.put(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER, install);
    }

    public DeviceFirmwareVersionInfos from(Device device) {
        Set<FirmwareType> supportedFirmwareTypes = getSupportedFirmwareTypesFor(device);
        DeviceFirmwareVersionInfos info = new DeviceFirmwareVersionInfos(thesaurus, supportedFirmwareTypes);
        supportedFirmwareTypes
                .stream()
                .map(firmwareType -> firmwareService.getActiveFirmwareVersion(device, firmwareType))
                .flatMap(Functions.asStream())
                .forEach(info::addActiveVersion);
        FirmwareManagementDeviceUtils versionUtils = this.firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        for (DeviceMessage<Device> message : versionUtils.getFirmwareMessages()) {
            from(info, message, versionUtils);
        }
        return info;
    }

    private Set<FirmwareType> getSupportedFirmwareTypesFor(Device device) {
        EnumSet<FirmwareType> firmwareTypes = EnumSet.of(FirmwareType.METER);
        if (device.getDeviceProtocolPluggableClass().getDeviceProtocol().supportsCommunicationFirmwareVersion()) {
            firmwareTypes.add(FirmwareType.COMMUNICATION);
        }
        return firmwareTypes;
    }

    private void from(DeviceFirmwareVersionInfos info, DeviceMessage<Device> message, FirmwareManagementDeviceUtils versionUtils) {
        Optional<ProtocolSupportedFirmwareOptions> uploadOption = versionUtils.getUploadOptionFromMessage(message);
        Optional<FirmwareVersion> firmwareVersion = versionUtils.getFirmwareVersionFromMessage(message);
        if (uploadOption.isPresent() && firmwareVersion.isPresent()) {
            List<FirmwareUpgradeState> possibleStates = states.get(uploadOption.get());
            if (possibleStates != null) {
                possibleStates.stream()
                        .filter(state -> state.validateMessage(message, versionUtils))
                        .findFirst()
                        .ifPresent(upgradeState ->
                                info.addUpgradeVersion(
                                        upgradeState.getFirmwareVersionName(),
                                        upgradeState.getFirmwareUpgradeProperties(message, versionUtils),
                                        firmwareVersion.get()));
            }
        }
    }

    public interface FirmwareUpgradeState {
        boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper);

        String getFirmwareVersionName();

        Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper);
    }

    public static abstract class AbstractFirmwareUpgradeState implements FirmwareUpgradeState {
        protected static final String FIRMWARE_DEVICE_MESSAGE_ID = "firmwareDeviceMessageId";
        protected static final String FIRMWARE_PLANNED_ACTIVATION_DATE = "plannedActivationDate";
        protected static final String FIRMWARE_MANAGEMENT_OPTION = "firmwareManagementOption";
        protected static final String FIRMWARE_VERSION = "firmwareVersion";
        protected static final String FIRMWARE_VERSION_ID = "firmwareVersionId";
        protected static final String FIRMWARE_COM_TASK_ID = "firmwareComTaskId";
        protected static final String FIRMWARE_COM_TASK_SESSION_ID = "firmwareComTaskSessionId";
        protected final static String FIRMWARE_PLANNED_DATE = "plannedDate";

        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return helper.getUploadOptionFromMessage(message).isPresent();
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(FIRMWARE_DEVICE_MESSAGE_ID, message.getId());
            ProtocolSupportedFirmwareOptions uploadOption = helper.getUploadOptionFromMessage(message).get();
            properties.put(FIRMWARE_MANAGEMENT_OPTION, new ManagementOptionInfo(uploadOption.getId(), helper.translate(uploadOption.getId())) {
            });
            Optional<FirmwareVersion> firmwareVersion = helper.getFirmwareVersionFromMessage(message);
            if (firmwareVersion.isPresent()) {
                properties.put(FIRMWARE_VERSION, firmwareVersion.get().getFirmwareVersion());
                properties.put(FIRMWARE_VERSION_ID, firmwareVersion.get().getId());
            }
            properties.put(FIRMWARE_PLANNED_DATE, message.getReleaseDate());
            return properties;
        }
    }

    public static class PendingFirmwareUploadState extends AbstractFirmwareUpgradeState {

        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return super.validateMessage(message, helper)
                    && !helper.firmwareUploadTaskIsBusy()
                    && helper.isPendingMessage(message);
        }

        @Override
        public String getFirmwareVersionName() {
            return "pendingVersion";
        }
    }

    public static class PendingFirmwareUploadWithActivationDateState extends PendingFirmwareUploadState {
        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return super.validateMessage(message, helper)
                    && ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.equals(helper.getUploadOptionFromMessage(message).get());
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            Map<String, Object> properties = super.getFirmwareUpgradeProperties(message, helper);
            Optional<Instant> activationDate = helper.getActivationDateFromMessage(message);
            if (activationDate.isPresent()) {
                properties.put(FIRMWARE_PLANNED_ACTIVATION_DATE, activationDate.get().toEpochMilli());
            }
            return properties;
        }
    }

    public static class OngoingFirmwareUploadProcessState extends AbstractFirmwareUpgradeState {
        protected final String UPLOAD_START_DATE = "uploadStartDate";

        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return super.validateMessage(message, helper)
                && helper.firmwareUploadTaskIsBusy()
                && DeviceMessageStatus.PENDING.equals(message.getStatus());
        }

        @Override
        public String getFirmwareVersionName() {
            return "ongoingVersion";
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            Map<String, Object> properties = super.getFirmwareUpgradeProperties(message, helper);
            helper
                .getFirmwareComTaskExecution()
                .map(ComTaskExecution::getExecutionStartedTimestamp)
                .ifPresent(startedTimestamp -> properties.put(UPLOAD_START_DATE, startedTimestamp.toEpochMilli()));
            return properties;
        }
    }

    public static class OngoingFirmwareUploadWithActivationDateState extends OngoingFirmwareUploadProcessState {
        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return super.validateMessage(message, helper)
                && ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.equals(helper.getUploadOptionFromMessage(message).get());
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            Map<String, Object> properties = super.getFirmwareUpgradeProperties(message, helper);
            Optional<Instant> activationDate = helper.getActivationDateFromMessage(message);
            if (activationDate.isPresent()) {
                properties.put(FIRMWARE_PLANNED_ACTIVATION_DATE, activationDate.get().toEpochMilli());
            }
            return properties;
        }
    }

    public static class FailedFirmwareUploadState extends AbstractFirmwareUpgradeState {
        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return super.validateMessage(message, helper)
                    && releaseDateInPast(message, helper)
                    && (helper.firmwareUploadTaskIsFailed() && (DeviceMessageStatus.PENDING.equals(message.getStatus()) || DeviceMessageStatus.FAILED.equals(message.getStatus()))
                    || !helper.firmwareUploadTaskIsFailed() && DeviceMessageStatus.FAILED.equals(message.getStatus()));
        }

        private boolean releaseDateInPast(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper){
            return !helper.getCurrentInstant().isBefore(message.getReleaseDate())
                    && helper.getFirmwareComTaskExecution().isPresent()
                    && helper.getFirmwareComTaskExecution().get().getLastExecutionStartTimestamp() != null
                    && !helper.getFirmwareComTaskExecution().get().getLastExecutionStartTimestamp().isBefore(message.getReleaseDate());
        }

        @Override
        public String getFirmwareVersionName() {
            return "failedVersion";
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            Map<String, Object> properties = super.getFirmwareUpgradeProperties(message, helper);
            properties.put(FIRMWARE_COM_TASK_ID, helper.getFirmwareTask().get().getId());
            Optional<ComTaskExecutionSession> lastSession = helper.getFirmwareComTaskExecution().get().getLastSession();
            if (lastSession.isPresent()) {
                properties.put(FIRMWARE_COM_TASK_SESSION_ID, lastSession.get().getId());
            }
            return properties;
        }
    }

    public static class FirmwareUploadedButNotVerifiedYetState extends AbstractFirmwareUpgradeState {
        protected static final String CHECK_DATE = "lastCheckedDate";
        protected static final String UPGRADE_FINISHED_DATE = "plannedDate";

        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return super.validateMessage(message, helper)
                && DeviceMessageStatus.CONFIRMED.equals(message.getStatus())
                && (   needVerificationAfterImmediatelyActivation(message, helper)
                    || needVerificationAfterScheduledActivation(message, helper)
                    || needVerificationAfterManualActivation(message, helper));
        }

        private boolean needVerificationAfterImmediatelyActivation(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.equals(helper.getUploadOptionFromMessage(message).get());
        }

        private boolean needVerificationAfterScheduledActivation(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            Optional<Instant> activationDate = helper.getActivationDateFromMessage(message);
            return ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.equals(helper.getUploadOptionFromMessage(message).get())
                    && activationDate.isPresent()
                    && !activationDate.get().isAfter(Instant.now());
        }

        private boolean needVerificationAfterManualActivation(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            Optional<DeviceMessage<Device>> activationMessage = helper.getActivationMessageForUploadMessage(message);
            return activationMessage.isPresent()
                    && DeviceMessageStatus.CONFIRMED.equals(activationMessage.get().getStatus());
        }

        @Override
        public String getFirmwareVersionName() {
            return "needVerificationVersion";
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            Map<String, Object> properties = super.getFirmwareUpgradeProperties(message, helper);
            Optional<ComTaskExecution> statusCheckExecution = helper.getComTaskExecutionToCheckTheFirmwareVersion();
            if (statusCheckExecution.isPresent() && statusCheckExecution.get().getNextExecutionTimestamp() != null) {
                properties.put(CHECK_DATE, statusCheckExecution.get().getNextExecutionTimestamp().toEpochMilli());
            }
            addUpgradeFinishedDate(message, helper, properties);
            return properties;
        }

        private void addUpgradeFinishedDate(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper, Map<String, Object> properties) {
            properties.put(UPGRADE_FINISHED_DATE, message.getModTime().toEpochMilli());
            Optional<Instant> activationDate = helper.getActivationDateFromMessage(message);
            if (activationDate.isPresent()) {
                if (activationDate.get().isAfter(message.getModTime())) {
                    properties.put(UPGRADE_FINISHED_DATE, activationDate.get().toEpochMilli());
                }
            }
            Optional<DeviceMessage<Device>> activationMessage = helper.getActivationMessageForUploadMessage(message);
            if (activationMessage.isPresent()) {
                properties.put(UPGRADE_FINISHED_DATE, activationMessage.get().getModTime().toEpochMilli());
            }
        }
    }

    public static class FirmwareUploadedScheduledActivationState extends AbstractFirmwareUpgradeState {

        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return super.validateMessage(message, helper)
                    && DeviceMessageStatus.CONFIRMED.equals(message.getStatus())
                    && ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.equals(helper.getUploadOptionFromMessage(message).get());
        }

        @Override
        public String getFirmwareVersionName() {
            return "activatingVersion";
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            Map<String, Object> properties = super.getFirmwareUpgradeProperties(message, helper);
            Optional<Instant> activationDate = helper.getActivationDateFromMessage(message);
            if (activationDate.isPresent()) {
                properties.put(FIRMWARE_PLANNED_ACTIVATION_DATE, activationDate.get().toEpochMilli());
            }
            return properties;
        }
    }

    public static class FailedFirmwareActivationState extends FailedFirmwareUploadState {
        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(message.getDeviceMessageId())
                    && helper.getUploadMessageForActivationMessage(message).isPresent()
                    && (helper.firmwareUploadTaskIsFailed()
                    && (DeviceMessageStatus.PENDING.equals(message.getStatus()) || DeviceMessageStatus.FAILED.equals(message.getStatus()))
                    || !helper.firmwareUploadTaskIsFailed() && DeviceMessageStatus.FAILED.equals(message.getStatus()));
        }

        @Override
        public String getFirmwareVersionName() {
            return "failedActivatingVersion";
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            Optional<DeviceMessage<Device>> uploadMessage = helper.getUploadMessageForActivationMessage(message);
            Map<String, Object> properties = new HashMap<>();
            if (uploadMessage.isPresent()) {
                properties = super.getFirmwareUpgradeProperties(uploadMessage.get(), helper);
            }
            properties.put(FIRMWARE_DEVICE_MESSAGE_ID, message.getId());
            return properties;
        }
    }

    public static class OngoingFirmwareActivationProcessState extends AbstractFirmwareUpgradeState {
        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(message.getDeviceMessageId())
                    && helper.isPendingMessage(message)
                    && helper.getUploadMessageForActivationMessage(message).isPresent();
        }

        @Override
        public String getFirmwareVersionName() {
            return "ongoingActivatingVersion";
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            Optional<DeviceMessage<Device>> uploadMessage = helper.getUploadMessageForActivationMessage(message);
            Map<String, Object> properties = new HashMap<>();
            if (uploadMessage.isPresent()) {
                properties = super.getFirmwareUpgradeProperties(uploadMessage.get(), helper);
            }
            properties.put(FIRMWARE_DEVICE_MESSAGE_ID, message.getId());
            return properties;
        }
    }

    public static class FirmwareUploadedButNotActivatedYetState extends AbstractFirmwareUpgradeState {
        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return super.validateMessage(message, helper)
                    && !DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(message.getDeviceMessageId())
                    && DeviceMessageStatus.CONFIRMED.equals(message.getStatus())
                    && ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.equals(helper.getUploadOptionFromMessage(message).get())
                    && !helper.getActivationMessageForUploadMessage(message).isPresent();
        }

        @Override
        public String getFirmwareVersionName() {
            return "needActivationVersion";
        }
    }

    public abstract static class StatusCheckFirmwareUpgradeState extends AbstractFirmwareUpgradeState {
        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            /*
            - current message is an upload message
            - current message has the confirmed status
            - based on upload option:
              * activate - no additional checks
              * activateOnDate - time for activation passed
              * install - we have a confirmed activation message
            - current message and, if present, activation message have a release dates which are before the last firmware check
             */
            return !DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(message.getDeviceMessageId())
                    && DeviceMessageStatus.CONFIRMED.equals(message.getStatus())
                    && allPrerequisitesAreDone(message, helper)
                    && messageFinishedBeforeLastStatusCheck(message, helper);
        }

        protected boolean allPrerequisitesAreDone(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            Optional<ProtocolSupportedFirmwareOptions> uploadOption = helper.getUploadOptionFromMessage(message);
            if (uploadOption.isPresent()) {
                switch (uploadOption.get()) {
                    case UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE:
                        return true;
                    case UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE:
                        Optional<Instant> activationDate = helper.getActivationDateFromMessage(message);
                        Optional<ComTaskExecution> statusCheckExecution = helper.getComTaskExecutionToCheckTheFirmwareVersion();
                        if (statusCheckExecution.isPresent()) {
                            ComTaskExecution execution = statusCheckExecution.get();
                            if (execution.getLastExecutionStartTimestamp() != null) {
                                return activationDate.isPresent() && !activationDate.get().isAfter(execution.getLastExecutionStartTimestamp());
                            }
                        }
                        return false;
                    case UPLOAD_FIRMWARE_AND_ACTIVATE_LATER:
                        Optional<DeviceMessage<Device>> activationMessage = helper.getActivationMessageForUploadMessage(message);
                        return activationMessage.isPresent() && DeviceMessageStatus.CONFIRMED.equals(activationMessage.get().getStatus());
                }
            }
            return false;
        }

        protected boolean messageFinishedBeforeLastStatusCheck(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            Optional<ComTaskExecution> statusCheckExecution = helper.getComTaskExecutionToCheckTheFirmwareVersion();
            if (statusCheckExecution.isPresent()) {
                ComTaskExecution execution = statusCheckExecution.get();
                if (execution.getLastExecutionStartTimestamp() != null) {
                    Optional<DeviceMessage<Device>> activationMessage = helper.getActivationMessageForUploadMessage(message);
                    return !message.getReleaseDate().isAfter(execution.getLastExecutionStartTimestamp())
                            && (!activationMessage.isPresent() || !activationMessage.get().getReleaseDate().isAfter(execution.getLastExecutionStartTimestamp()));
                }
            }
            return false;
        }

        protected boolean lastStatusCheckWasSuccessful(FirmwareManagementDeviceUtils helper) {
            Optional<ComTaskExecution> check = helper.getComTaskExecutionToCheckTheFirmwareVersion();
            return check.isPresent()
                    && check.get().getLastSuccessfulCompletionTimestamp() != null
                    && !check.get().getLastSuccessfulCompletionTimestamp().isBefore(check.get().getLastExecutionStartTimestamp());
        }
    }

    public static class OngoingVersionVerificationFirmwareState extends StatusCheckFirmwareUpgradeState {
        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return super.validateMessage(message, helper)
                    && TaskStatus.Busy.equals(helper.getComTaskExecutionToCheckTheFirmwareVersion().get().getStatus());
        }

        @Override
        public String getFirmwareVersionName() {
            return "ongoingVerificationVersion";
        }
    }

    public static class FailedVersionVerificationFirmwareState extends StatusCheckFirmwareUpgradeState {
        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return super.validateMessage(message, helper)
                    && !helper.verifyFirmwareVersionTaskIsBusy()
                    && helper.verifyFirmwareVersionTaskIsFailed();
        }

        @Override
        public String getFirmwareVersionName() {
            return "failedVerificationVersion";
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            Map<String, Object> properties = super.getFirmwareUpgradeProperties(message, helper);
            properties.put(FIRMWARE_COM_TASK_ID, helper.getFirmwareCheckTask().get().getId());
            Optional<ComTaskExecutionSession> lastSession = helper.getFirmwareComTaskExecution().get().getLastSession();
            if (lastSession.isPresent()) {
                properties.put(FIRMWARE_COM_TASK_SESSION_ID, lastSession.get().getId());
            }
            return properties;
        }
    }

    public static class WrongVersionVerificationFirmwareState extends StatusCheckFirmwareUpgradeState {
        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return super.validateMessage(message, helper)
                    && lastStatusCheckWasSuccessful(helper)
                    && !helper.messageContainsActiveFirmwareVersion(message);
        }

        @Override
        public String getFirmwareVersionName() {
            return "wrongVerificationVersion";
        }
    }

    public static class UpgradeFirmwareSuccessfulFinishedState extends StatusCheckFirmwareUpgradeState {
        @Override
        public boolean validateMessage(DeviceMessage<Device> message, FirmwareManagementDeviceUtils helper) {
            return super.validateMessage(message, helper)
                    && lastStatusCheckWasSuccessful(helper)
                    && helper.messageContainsActiveFirmwareVersion(message);
        }

        @Override
        public String getFirmwareVersionName() {
            return null; // do not append to response
        }
    }
}
