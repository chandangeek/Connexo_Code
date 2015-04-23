package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DeviceFirmwareVersionInfoFactory {
    private final Thesaurus thesaurus;
    private final Provider<DeviceFirmwareVersionUtils> utilProvider;

    private final Map<ProtocolSupportedFirmwareOptions, List<FirmwareUpgradeState>> states;

    @Inject
    public DeviceFirmwareVersionInfoFactory(Thesaurus thesaurus, Provider<DeviceFirmwareVersionUtils> utilProvider) {
        this.thesaurus = thesaurus;
        this.utilProvider = utilProvider;
        states = new HashMap<>();

        initStatesForActivateOnDate();
        initStatesForActivate();
        initStatesForInstall();
    }

    private final void initStatesForActivateOnDate() {
        List<FirmwareUpgradeState> activateOnDate = new ArrayList<>();
        activateOnDate.add(new FirmwareUploadedButNotVerifiedYetState());
        activateOnDate.add(new FailedFirmwareUploadState());
        activateOnDate.add(new FirmwareUploadedScheduledActivationState());
        activateOnDate.add(new OngoingFirmwareUploadWithActivationDateState());
        activateOnDate.add(new PendingFirmwareUploadWithActivationDateState());
        states.put(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE, activateOnDate);
    }

    private final void initStatesForActivate() {
        List<FirmwareUpgradeState> activate = new ArrayList<>();
        activate.add(new FirmwareUploadedButNotVerifiedYetState());
        activate.add(new FailedFirmwareUploadState());
        activate.add(new OngoingFirmwareUploadProcessState());
        activate.add(new PendingFirmwareUploadState());
        states.put(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE, activate);
    }

    private final void initStatesForInstall() {
        List<FirmwareUpgradeState> install = new ArrayList<>();
        install.add(new FirmwareUploadedButNotVerifiedYetState());
        install.add(new FailedFirmwareActivationState());
        install.add(new OngoingFirmwareActivationProcessState());
        install.add(new FirmwareUploadedButNotActivatedYetState());
        install.add(new FailedFirmwareUploadState());
        install.add(new OngoingFirmwareUploadProcessState());
        install.add(new PendingFirmwareUploadState());
        states.put(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER, install);
    }

    public DeviceFirmwareVersionInfos from(Device device, Optional<ActivatedFirmwareVersion>... activatedVersions){
        DeviceFirmwareVersionInfos info = new DeviceFirmwareVersionInfos(thesaurus);
        Arrays.asList(activatedVersions).stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(info::addActiveVersion);
        DeviceFirmwareVersionUtils versionUtils = utilProvider.get().onDevice(device);
        if (versionUtils.getComTaskExecution() != null) {
            for (DeviceMessage<Device> message : versionUtils.getFirmwareMessages()) {
                from(info, message, versionUtils);
            }
        }
        return info;
    }

    private void from(DeviceFirmwareVersionInfos info, DeviceMessage<Device> message, DeviceFirmwareVersionUtils versionUtils) {
        Optional<ProtocolSupportedFirmwareOptions> uploadOption = versionUtils.getUploadOptionFromMessage(message);
        Optional<FirmwareVersion> firmwareVersion = versionUtils.getFirmwareVersionFromMessage(message);
        if (uploadOption.isPresent() && firmwareVersion.isPresent()){
            List<FirmwareUpgradeState> possibleStates = states.get(uploadOption.get());
            if (possibleStates != null){
                possibleStates.stream()
                        .filter(state -> state.validateMessage(message, versionUtils))
                        .findFirst().ifPresent(upgradeState -> {
                    info.addUpgradeVersion(upgradeState.getFirmwareVersionName(),
                            upgradeState.getFirmwareUpgradeProperties(message, versionUtils),
                            firmwareVersion.get());
                });
            }
        }
    }

    public interface FirmwareUpgradeState {
        boolean validateMessage(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper);
        String getFirmwareVersionName();
        Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper);
    }

    public static abstract class AbstractFirmwareUpgradeState implements FirmwareUpgradeState {
        protected final String FIRMWARE_DEVICE_MESSAGE_ID = "firmwareDeviceMessageId";
        protected final String FIRMWARE_PLANNED_ACTIVATION_DATE = "plannedActivationDate";
        protected final String FIRMWARE_UPGRADE_OPTION = "firmwareUpgradeOption";
        protected final String FIRMWARE_VERSION = "firmwareVersion";

        @Override
        public boolean validateMessage(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            return helper.getUploadOptionFromMessage(message).isPresent();
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(FIRMWARE_DEVICE_MESSAGE_ID, message.getId());
            ProtocolSupportedFirmwareOptions uploadOption = helper.getUploadOptionFromMessage(message).get();
            properties.put(FIRMWARE_UPGRADE_OPTION, new UpgradeOptionInfo(uploadOption.getId(), helper.translate(uploadOption.getId())));
            Optional<FirmwareVersion> firmwareVersion = helper.getFirmwareVersionFromMessage(message);
            if (firmwareVersion.isPresent()){
                properties.put(FIRMWARE_VERSION, firmwareVersion.get().getFirmwareVersion());
            }
            return properties;
        }
    }

    public static class PendingFirmwareUploadState extends AbstractFirmwareUpgradeState {
        protected final String FIRMWARE_PLANNED_DATE = "plannedDate";

        @Override
        public boolean validateMessage(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            return super.validateMessage(message, helper)
                    && !helper.taskIsBusy()
                    && DeviceFirmwareVersionUtils.PENDING_STATUSES.contains(message.getStatus());
        }

        @Override
        public String getFirmwareVersionName() {
            return "pendingVersion";
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            Map<String, Object> properties = super.getFirmwareUpgradeProperties(message, helper);
            properties.put(FIRMWARE_PLANNED_DATE, message.getReleaseDate());
            return properties;
        }
    }

    public static class PendingFirmwareUploadWithActivationDateState extends PendingFirmwareUploadState {
        @Override
        public boolean validateMessage(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            return super.validateMessage(message, helper)
                    && ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.equals(helper.getUploadOptionFromMessage(message).get());
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            Map<String, Object> properties = super.getFirmwareUpgradeProperties(message, helper);
            Optional<Instant> activationDate = helper.getActivationDateFromMessage(message);
            if (activationDate.isPresent()){
                properties.put(FIRMWARE_PLANNED_ACTIVATION_DATE, activationDate.get().toEpochMilli());
            }
            return properties;
        }
    }

    public static class OngoingFirmwareUploadProcessState extends AbstractFirmwareUpgradeState {
        protected final String UPLOAD_START_DATE = "uploadStartDate";

        @Override
        public boolean validateMessage(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            return super.validateMessage(message, helper)
                    && helper.taskIsBusy()
                    && DeviceMessageStatus.PENDING.equals(message.getStatus());
        }

        @Override
        public String getFirmwareVersionName() {
            return "ongoingVersion";
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            Map<String, Object> properties = super.getFirmwareUpgradeProperties(message, helper);
            Instant startedTimestamp = helper.getComTaskExecution().getExecutionStartedTimestamp();
            if (startedTimestamp != null) {
                properties.put(UPLOAD_START_DATE, startedTimestamp.toEpochMilli());
            }
            return properties;
        }
    }

    public static class OngoingFirmwareUploadWithActivationDateState extends OngoingFirmwareUploadProcessState {
        @Override
        public boolean validateMessage(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            return super.validateMessage(message, helper)
                    && ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.equals(helper.getUploadOptionFromMessage(message).get());
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            Map<String, Object> properties = super.getFirmwareUpgradeProperties(message, helper);
            Optional<Instant> activationDate = helper.getActivationDateFromMessage(message);
            if (activationDate.isPresent()){
                properties.put(FIRMWARE_PLANNED_ACTIVATION_DATE, activationDate.get().toEpochMilli());
            }
            return properties;
        }
    }

    public static class FailedFirmwareUploadState extends AbstractFirmwareUpgradeState {
        protected static final String FIRMWARE_COM_TASK_ID = "firmwareComTaskId";
        protected static final String FIRMWARE_COM_TASK_SESSION_ID = "firmwareComTaskSessionId";

        @Override
        public boolean validateMessage(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            return super.validateMessage(message, helper)
                    && (helper.taskIsFailed() &&(DeviceMessageStatus.PENDING.equals(message.getStatus()) || DeviceMessageStatus.FAILED.equals(message.getStatus()))
                        || !helper.taskIsFailed() && DeviceMessageStatus.FAILED.equals(message.getStatus()));
        }

        @Override
        public String getFirmwareVersionName() {
            return "failedVersion";
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            Map<String, Object> properties = super.getFirmwareUpgradeProperties(message, helper);
            properties.put(FIRMWARE_COM_TASK_ID, helper.getComTaskExecution().getComTask().getId());
            Optional<ComTaskExecutionSession> lastSession = helper.getComTaskExecution().getLastSession();
            if (lastSession.isPresent()) {
                properties.put(FIRMWARE_COM_TASK_SESSION_ID, lastSession.get().getId());
            }
            return properties;
        }
    }

    public static class FirmwareUploadedButNotVerifiedYetState extends AbstractFirmwareUpgradeState {
        protected static final String CHECK_DATE = "checkDate";

        @Override
        public boolean validateMessage(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            return super.validateMessage(message, helper)
                    && DeviceMessageStatus.CONFIRMED.equals(message.getStatus())
                    && (needVerificationAfterImmediatelyActivation(message, helper)
                        || needVerificationAfterScheduledActivation(message, helper)
                        || needVerificationAfterManualActivation(message, helper));
        }

        private boolean needVerificationAfterImmediatelyActivation(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper){
            return ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.equals(helper.getUploadOptionFromMessage(message).get())
                    && helper.isTaskLastSuccessLessOrEqualTo(message.getModTime());
        }

        private boolean needVerificationAfterScheduledActivation(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper){
            Optional<Instant> activationDate = helper.getActivationDateFromMessage(message);
            return ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.equals(helper.getUploadOptionFromMessage(message).get())
                    && helper.isTaskLastSuccessLessOrEqualTo(message.getModTime())
                    && activationDate.isPresent()
                    && !activationDate.get().isAfter(Instant.now());
        }

        private boolean needVerificationAfterManualActivation(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper){
            Optional<DeviceMessage<Device>> activationMessage = helper.getLastFirmwareActivationMessage(message);
            return activationMessage.isPresent()
                    && DeviceMessageStatus.CONFIRMED.equals(activationMessage.get().getStatus())
                    && helper.isTaskLastSuccessLessOrEqualTo(activationMessage.get().getModTime());
        }

        @Override
        public String getFirmwareVersionName() {
            return "needVerificationVersion";
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            Map<String, Object> properties = super.getFirmwareUpgradeProperties(message, helper);
            Optional<ComTaskExecution> basicCheckExecution = helper.getBasicCheckExecution();
            if (basicCheckExecution.isPresent() && basicCheckExecution.get().getNextExecutionTimestamp() != null){
                properties.put(CHECK_DATE, basicCheckExecution.get().getNextExecutionTimestamp().toEpochMilli());
            }
            return properties;
        }
    }

    public static class FirmwareUploadedScheduledActivationState extends AbstractFirmwareUpgradeState {

        @Override
        public boolean validateMessage(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            return super.validateMessage(message, helper)
                    && DeviceMessageStatus.CONFIRMED.equals(message.getStatus())
                    && helper.isTaskLastSuccessLessOrEqualTo(message.getModTime())
                    && ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.equals(helper.getUploadOptionFromMessage(message).get());
        }

        @Override
        public String getFirmwareVersionName() {
            return "activatingVersion";
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            Map<String, Object> properties = super.getFirmwareUpgradeProperties(message, helper);
            Optional<Instant> activationDate = helper.getActivationDateFromMessage(message);
            if (activationDate.isPresent()){
                properties.put(FIRMWARE_PLANNED_ACTIVATION_DATE, activationDate.get().toEpochMilli());
            }
            return properties;
        }
    }

    public static class FailedFirmwareActivationState extends FailedFirmwareUploadState {
        @Override
        public boolean validateMessage(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            return DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(message.getDeviceMessageId())
                    && helper.getLastSuccessfulUploadMessageFor(message).isPresent()
                    && (helper.taskIsFailed()
                    && (DeviceMessageStatus.PENDING.equals(message.getStatus()) || DeviceMessageStatus.FAILED.equals(message.getStatus()))
                        || !helper.taskIsFailed() && DeviceMessageStatus.FAILED.equals(message.getStatus()));
        }

        @Override
        public String getFirmwareVersionName() {
            return "failedActivatingVersion";
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            Optional<DeviceMessage<Device>> uploadMessage = helper.getLastSuccessfulUploadMessageFor(message);
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
        public boolean validateMessage(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            return DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(message.getDeviceMessageId())
                    && helper.getLastSuccessfulUploadMessageFor(message).isPresent()
                    && helper.taskIsBusy()
                    && DeviceMessageStatus.PENDING.equals(message.getStatus());
        }

        @Override
        public String getFirmwareVersionName() {
            return "ongoingActivatingVersion";
        }

        @Override
        public Map<String, Object> getFirmwareUpgradeProperties(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            Optional<DeviceMessage<Device>> uploadMessage = helper.getLastSuccessfulUploadMessageFor(message);
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
        public boolean validateMessage(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper) {
            return super.validateMessage(message, helper)
                    && DeviceMessageStatus.CONFIRMED.equals(message.getStatus())
                    && helper.isTaskLastSuccessLessOrEqualTo(message.getModTime())
                    && ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.equals(helper.getUploadOptionFromMessage(message).get())
                    && thereIsNoActivationMessageYet(message, helper);
        }

        private boolean thereIsNoActivationMessageYet(DeviceMessage<Device> message, DeviceFirmwareVersionUtils helper){
            return !helper.getFirmwareMessages().stream()
                    .filter(msg -> DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(msg.getDeviceMessageId()))
                    .filter(msg -> msg.getModTime().isAfter(message.getModTime()))
                    .findFirst()
                    .isPresent();
        }

        @Override
        public String getFirmwareVersionName() {
            return "needActivationVersion";
        }
    }
}
