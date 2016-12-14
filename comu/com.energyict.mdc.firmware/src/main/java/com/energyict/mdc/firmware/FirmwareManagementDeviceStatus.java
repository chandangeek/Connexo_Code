package com.energyict.mdc.firmware;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.firmware.impl.FirmwareManagementDeviceUtilsImpl;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public enum FirmwareManagementDeviceStatus {
    /**
     * <ul>Upload pending:
     * <li>The message is an upload message</li>
     * <li>The message has one of pending statuses (waiting, pending or sent)</li>
     * <li>Release date of the message is in future or in past, but task is not launched yet</li>
     * </ul>
     */
    UPLOAD_PENDING(Constants.PENDING) {
        @Override
        public boolean validateMessage(DeviceMessage message, FirmwareManagementDeviceUtils helper) {
            return isUploadMessage(message)
                    && FirmwareManagementDeviceUtilsImpl.PENDING_STATUSES.contains(message.getStatus())
                    && checkReleaseDate(message, helper)
                    && helper.firmwareTaskIsScheduled();
        }

        @Override
        public boolean canTransitToStatus(FirmwareManagementDeviceStatus newStatus) {
            return Arrays.asList(Constants.CANCELLED, Constants.ONGOING).contains(newStatus.key());
        }

        private boolean checkReleaseDate(DeviceMessage message, FirmwareManagementDeviceUtils helper) {
            boolean firmwareUploadInFuture = message.getReleaseDate().isAfter(helper.getCurrentInstant());
            return firmwareUploadInFuture || !helper.firmwareUploadTaskIsBusy();
        }
    },

    /**
     * <ul>Upload ongoing:
     * <li>The message is an upload message</li>
     * <li>The message has the pending status</li>
     * <li>Release date of the message is in past</li>
     * <li>Firmware management task is currently busy</li>
     * </ul>
     */
    UPLOAD_ONGOING(Constants.ONGOING) {
        @Override
        public boolean validateMessage(DeviceMessage message, FirmwareManagementDeviceUtils helper) {
            return isUploadMessage(message)
                    && DeviceMessageStatus.PENDING.equals(message.getStatus())
                    && releaseDateInPast(message, helper)
                    && helper.firmwareUploadTaskIsBusy();
        }

        @Override
        public boolean canTransitToStatus(FirmwareManagementDeviceStatus newStatus) {
            return Arrays.asList(Constants.CANCELLED, Constants.SUCCESS, Constants.FAILED).contains(newStatus.key());
        }
    },

    /**
     * <ul>Upload failed:
     * <li>The message is an upload message</li>
     * <li>Release date of the message is in past</li>
     * <li>Task was failed and the message has one of pending statuses OR the message has the failed status</li>
     * </ul>
     */
    UPLOAD_FAILED(Constants.FAILED) {
        @Override
        public boolean validateMessage(DeviceMessage message, FirmwareManagementDeviceUtils helper) {
            return isUploadMessage(message)
                    && releaseDateInPast(message, helper)
                    && (taskFailedButMessageNot(message, helper) || messageFailed(message, helper));
        }

        @Override
        public boolean canTransitToStatus(FirmwareManagementDeviceStatus newStatus) {
            //retry
            return newStatus.key().equals(Constants.PENDING);
        }

        private boolean taskFailedButMessageNot(DeviceMessage message, FirmwareManagementDeviceUtils helper){
            Optional<ComTaskExecution> firmwareExecution = helper.getFirmwareComTaskExecution();
            return firmwareExecution.isPresent()
                    && firmwareExecution.get().isLastExecutionFailed()
                    && FirmwareManagementDeviceUtilsImpl.PENDING_STATUSES.contains(message.getStatus());
        }

        private boolean messageFailed(DeviceMessage message, FirmwareManagementDeviceUtils helper){
            return DeviceMessageStatus.FAILED.equals(message.getStatus());
        }
    },

    /**
     * <ul>Upload success:
     * <li>The message is an upload message</li>
     * <li>The message has the confirmed status</li>
     * <li>The message hasn't the 'activateOnDate' option</li>
     * <li>The message with the 'install' option has no activation message</li>
     * </ul>
     */
    UPLOAD_SUCCESS(Constants.SUCCESS) {
        @Override
        public boolean validateMessage(DeviceMessage message, FirmwareManagementDeviceUtils helper) {
            return isUploadMessage(message)
                    && DeviceMessageStatus.CONFIRMED.equals(message.getStatus())
                    && messageWithoutActivateOnDateOption(message, helper)
                    && messageWithInstallOptionHasNoActivateMessageYet(message, helper);

        }

        @Override
        public boolean canTransitToStatus(FirmwareManagementDeviceStatus newStatus) {
            // final state
            return false;
        }

        private boolean messageWithoutActivateOnDateOption(DeviceMessage message, FirmwareManagementDeviceUtils helper){
            Optional<ProtocolSupportedFirmwareOptions> firmwareOption = helper.getUploadOptionFromMessage(message);
            return !firmwareOption.isPresent()
                    || !ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.equals(firmwareOption.get());
        }

        private boolean messageWithInstallOptionHasNoActivateMessageYet(DeviceMessage message, FirmwareManagementDeviceUtils helper){
            Optional<ProtocolSupportedFirmwareOptions> firmwareOption = helper.getUploadOptionFromMessage(message);
            return firmwareOption.isPresent()
                    && (!ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.equals(firmwareOption.get())
                    || !helper.getActivationMessageForUploadMessage(message).isPresent());
        }
    },

    /**
     * <ul>Activation pending:
     * <li>The message is an activation message</li>
     * <li>An upload message for the message has the confirmed status</li>
     * <li>Release date of the message is in future or in past, but task is not launched yet</li>
     * </ul>
     */
    ACTIVATION_PENDING(Constants.SUCCESS) {
        @Override
        public boolean validateMessage(DeviceMessage message, FirmwareManagementDeviceUtils helper) {
            return !isUploadMessage(message)
                    && uploadMessageHasConfirmedStatus(message, helper)
                    && releaseDateInPast(message, helper);

        }

        @Override
        public boolean canTransitToStatus(FirmwareManagementDeviceStatus newStatus) {
            return false;
        }
    },

    /**
     * <ul>Activation ongoing:
     * <li>
     *     <ul>Case 1: message has 'install' option
     *          <li>The message is an activation message</li>
     *          <li>An upload message for the message has the confirmed status</li>
     *          <li>Release date of the message is in past</li>
     *          <li>Firmware management task is currently busy</li>
     *     </ul>
     * </li>
     * <li>
     *     <ul>Case 2: message has 'activate' option
     *          <li>impossible</li>
     *     </ul>
     * </li>
     * <li>
     *     <ul>Case 3: message has 'activateOnDate' option
     *          <li>The message is an upload message</li>
     *          <li>The message has the confirmed status</li>
     *          <li>Activation date from the message is in future</li>
     *     </ul>
     * </li>
     * </ul>
     */
    ACTIVATION_ONGOING(Constants.SUCCESS) {
        @Override
        public boolean validateMessage(DeviceMessage message, FirmwareManagementDeviceUtils helper) {
            return helper.getUploadOptionFromMessage(message).isPresent()
                    && (messageWithInstallOption(message, helper) || messageWithActivateOnDateOption(message, helper));

        }

        @Override
        public boolean canTransitToStatus(FirmwareManagementDeviceStatus newStatus) {
            return false;
        }

        private boolean messageWithInstallOption(DeviceMessage message, FirmwareManagementDeviceUtils helper){
            ProtocolSupportedFirmwareOptions uploadOption = helper.getUploadOptionFromMessage(message).get();
            return ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.equals(uploadOption)
                    && !isUploadMessage(message)
                    && uploadMessageHasConfirmedStatus(message, helper)
                    && releaseDateInPast(message, helper)
                    && helper.firmwareUploadTaskIsBusy();
        }

        private boolean messageWithActivateOnDateOption(DeviceMessage message, FirmwareManagementDeviceUtils helper){
            ProtocolSupportedFirmwareOptions uploadOption = helper.getUploadOptionFromMessage(message).get();
            return ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.equals(uploadOption)
                    && isUploadMessage(message)
                    && DeviceMessageStatus.CONFIRMED.equals(message.getStatus())
                    && !activationDateIsInPast(message, helper);
        }
    },

    /**
     * <ul>Activation failed:
     * <li>The message is an activation message</li>
     * <li>The message has the 'install' option</li>
     * <li>An upload message for the message has the confirmed status</li>
     * <li>Release date of the message is in past</li>
     * <li>Task was failed and the message has one of pending statuses OR the message has the failed status</li>
     * </ul>
     */
    ACTIVATION_FAILED(Constants.SUCCESS) {
        @Override
        public boolean validateMessage(DeviceMessage message, FirmwareManagementDeviceUtils helper) {
            return !isUploadMessage(message)
                    && uploadMessageHasConfirmedStatus(message, helper)
                    && releaseDateInPast(message, helper)
                    && (taskFailedButMessageNot(message, helper) || messageFailed(message, helper));

        }

        @Override
        public boolean canTransitToStatus(FirmwareManagementDeviceStatus newStatus) {
            return false;
        }

        private boolean taskFailedButMessageNot(DeviceMessage message, FirmwareManagementDeviceUtils helper){
            Optional<ComTaskExecution> firmwareExecution = helper.getFirmwareComTaskExecution();
            return firmwareExecution.isPresent()
                    && firmwareExecution.get().isLastExecutionFailed()
                    && FirmwareManagementDeviceUtilsImpl.PENDING_STATUSES.contains(message.getStatus());
        }

        private boolean messageFailed(DeviceMessage message, FirmwareManagementDeviceUtils helper){
            return DeviceMessageStatus.FAILED.equals(message.getStatus());
        }
    },

    /**
     * <ul>Activation success:
     * <li>
     *     <ul>Case 1: message has 'install' option
     *          <li>The message is an activation message</li>
     *          <li>The message has the confirmed status</li>
     *     </ul>
     * </li>
     * <li>
     *     <ul>Case 2: message has 'activate' option
     *          <li>impossible</li>
     *     </ul>
     * </li>
     * <li>
     *     <ul>Case 3: message has 'activateOnDate' option
     *          <li>The message is an upload message</li>
     *          <li>The message has the confirmed status</li>
     *          <li>Activation date from the message is in past</li>
     *     </ul>
     * </li>
     * </ul>
     */
    ACTIVATION_SUCCESS(Constants.SUCCESS) {
        @Override
        public boolean validateMessage(DeviceMessage message, FirmwareManagementDeviceUtils helper) {
            return helper.getUploadOptionFromMessage(message).isPresent()
                    && (messageWithInstallOption(message, helper) || messageWithActivateOnDateOption(message, helper));

        }

        @Override
        public boolean canTransitToStatus(FirmwareManagementDeviceStatus newStatus) {
            return false;
        }

        private boolean messageWithInstallOption(DeviceMessage message, FirmwareManagementDeviceUtils helper){
            ProtocolSupportedFirmwareOptions uploadOption = helper.getUploadOptionFromMessage(message).get();
            return ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.equals(uploadOption)
                    && !isUploadMessage(message)
                    && DeviceMessageStatus.CONFIRMED.equals(message.getStatus());
         }

        private boolean messageWithActivateOnDateOption(DeviceMessage message, FirmwareManagementDeviceUtils helper){
            ProtocolSupportedFirmwareOptions uploadOption = helper.getUploadOptionFromMessage(message).get();
            return ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.equals(uploadOption)
                    && isUploadMessage(message)
                    && DeviceMessageStatus.CONFIRMED.equals(message.getStatus())
                    && activationDateIsInPast(message, helper);
        }
    },

    /**
     * <ul>Verification ongoing:
     * <li>The message is an upload message</li>
     * <li>The message has the confirmed status</li>
     * <li>Prerequisites:
     *     <ul>Case 1: message has 'install' option
     *          <li>An activation message has the confirmed status</li>
     *     </ul>
     *     <ul>Case 2: message has 'activate' option
     *          <li>nothing</li>
     *     </ul>
     *     <ul>Case 3: message has 'activateOnDate' option
     *          <li>Activation date from the message is in past</li>
     *     </ul>
     * </li>
     * <li>Status information task is currently busy</li>
     * </ul>
     */
    VERIFICATION_ONGOING(Constants.SUCCESS) {
        @Override
        public boolean validateMessage(DeviceMessage message, FirmwareManagementDeviceUtils helper) {
            Optional<ComTaskExecution> statusInformationTask = helper.getComTaskExecutionToCheckTheFirmwareVersion();
            return isOneOfVerificationStatuses(message, helper)
                    && statusInformationTask.isPresent()
                    && FirmwareManagementDeviceUtilsImpl.BUSY_TASK_STATUSES.contains(statusInformationTask.get().getStatus());
        }

        @Override
        public boolean canTransitToStatus(FirmwareManagementDeviceStatus newStatus) {
            return false;
        }
    },

    /**
     * <ul>Verification task failed:
     * <li>The message is an upload message</li>
     * <li>The message has the confirmed status</li>
     * <li>Prerequisites:
     *     <ul>Case 1: message has 'install' option
     *          <li>An activation message has the confirmed status</li>
     *     </ul>
     *     <ul>Case 2: message has 'activate' option
     *          <li>nothing</li>
     *     </ul>
     *     <ul>Case 3: message has 'activateOnDate' option
     *          <li>Activation date from the message is in past</li>
     *     </ul>
     * </li>
     * <li>Status information task was failed</li>
     * <li>A firmware version from the message differs from the active firmware version</li>
     * </ul>
     */
    VERIFICATION_TASK_FAILED(Constants.SUCCESS) {
        @Override
        public boolean validateMessage(DeviceMessage message, FirmwareManagementDeviceUtils helper) {
            Optional<ComTaskExecution> statusInformationTask = helper.getComTaskExecutionToCheckTheFirmwareVersion();
            return isOneOfVerificationStatuses(message, helper)
                    && statusInformationTask.isPresent()
                    && statusInformationTask.get().isLastExecutionFailed()
                    && !helper.messageContainsActiveFirmwareVersion(message);
        }

        @Override
        public boolean canTransitToStatus(FirmwareManagementDeviceStatus newStatus) {
            return false;
        }
    },

    /**
     * <ul>Verification failed:
     * <li>The message is an upload message</li>
     * <li>The message has the confirmed status</li>
     * <li>Prerequisites:
     *     <ul>Case 1: message has 'install' option
     *          <li>An activation message has the confirmed status</li>
     *     </ul>
     *     <ul>Case 2: message has 'activate' option
     *          <li>nothing</li>
     *     </ul>
     *     <ul>Case 3: message has 'activateOnDate' option
     *          <li>Activation date from the message is in past</li>
     *     </ul>
     * </li>
     * <li>Status information task was successful</li>
     * <li>A firmware version from the message differs from the active firmware version</li>
     * </ul>
     */
    VERIFICATION_FAILED(Constants.SUCCESS) {
        @Override
        public boolean validateMessage(DeviceMessage message, FirmwareManagementDeviceUtils helper) {
            Optional<ComTaskExecution> statusInformationTask = helper.getComTaskExecutionToCheckTheFirmwareVersion();
            return isOneOfVerificationStatuses(message, helper)
                    && statusInformationTask.isPresent()
                    && !statusInformationTask.get().isLastExecutionFailed()
                    && !helper.messageContainsActiveFirmwareVersion(message);
        }

        @Override
        public boolean canTransitToStatus(FirmwareManagementDeviceStatus newStatus) {
            return false;
        }
    },

    /**
     * <ul>Verification success:
     * <li>The message is an upload message</li>
     * <li>The message has the confirmed status</li>
     * <li>Prerequisites:
     *     <ul>Case 1: message has 'install' option
     *          <li>An activation message has the confirmed status</li>
     *     </ul>
     *     <ul>Case 2: message has 'activate' option
     *          <li>nothing</li>
     *     </ul>
     *     <ul>Case 3: message has 'activateOnDate' option
     *          <li>Activation date from the message is in past</li>
     *     </ul>
     * </li>
     * <li>A firmware version from the message is the same as the active firmware version</li>
     * </ul>
     */
    VERIFICATION_SUCCESS(Constants.SUCCESS) {
        @Override
        public boolean validateMessage(DeviceMessage message, FirmwareManagementDeviceUtils helper) {
            Optional<ComTaskExecution> statusInformationTask = helper.getComTaskExecutionToCheckTheFirmwareVersion();
            return isOneOfVerificationStatuses(message, helper)
                    && statusInformationTask.isPresent()
                    && helper.messageContainsActiveFirmwareVersion(message);
        }

        @Override
        public boolean canTransitToStatus(FirmwareManagementDeviceStatus newStatus) {
            return false;
        }
    },

    CONFIGURATION_ERROR(Constants.CONFIGURATION_ERROR){
        @Override
        public boolean canTransitToStatus(FirmwareManagementDeviceStatus newStatus) {
            //retry
            return newStatus.key().equals(Constants.PENDING);
        }
    },
    CANCELLED(Constants.CANCELLED){
            @Override
            public boolean canTransitToStatus(FirmwareManagementDeviceStatus newStatus) {
                //retry
                return newStatus.key().equals(Constants.PENDING);
            }

        }
    ;

    private String deviceInCampaignStatusKey;

    FirmwareManagementDeviceStatus(String deviceInCampaignStatusKey) {
        this.deviceInCampaignStatusKey = deviceInCampaignStatusKey;
    }

    public String key(){
        return this.deviceInCampaignStatusKey;
    }

    public boolean validateMessage(DeviceMessage message, FirmwareManagementDeviceUtils helper) {
        return false;
    }

    public abstract boolean canTransitToStatus(FirmwareManagementDeviceStatus newStatus);

    protected boolean isUploadMessage(DeviceMessage message) {
        return !DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.equals(message.getDeviceMessageId());
    }

    protected boolean releaseDateInPast(DeviceMessage message, FirmwareManagementDeviceUtils helper){
        return !helper.getCurrentInstant().isBefore(message.getReleaseDate())
                && helper.getFirmwareComTaskExecution().isPresent()
                && helper.getFirmwareComTaskExecution().get().getLastExecutionStartTimestamp() != null
                && !helper.getFirmwareComTaskExecution().get().getLastExecutionStartTimestamp().isBefore(message.getReleaseDate());
    }

    protected boolean uploadMessageHasConfirmedStatus(DeviceMessage message, FirmwareManagementDeviceUtils helper){
        Optional<DeviceMessage> uploadMessage = helper.getUploadMessageForActivationMessage(message);
        return uploadMessage.isPresent()
                && DeviceMessageStatus.CONFIRMED.equals(uploadMessage.get().getStatus());
    }

    protected boolean activationDateIsInPast(DeviceMessage message, FirmwareManagementDeviceUtils helper){
        Optional<Instant> activationDate = helper.getActivationDateFromMessage(message);
        return activationDate.isPresent()
                && !helper.getCurrentInstant().isBefore(activationDate.get());
    }

    protected boolean isOneOfVerificationStatuses(DeviceMessage message, FirmwareManagementDeviceUtils helper){
        return isUploadMessage(message)
                && DeviceMessageStatus.CONFIRMED.equals(message.getStatus())
                && (verificationPrerequisitesForMessageWithInstallOption(message, helper) || verificationPrerequisitesForMessageWithActivateOnDateOption(message, helper));
    }

    private boolean verificationPrerequisitesForMessageWithInstallOption(DeviceMessage message, FirmwareManagementDeviceUtils helper){
        Optional<ProtocolSupportedFirmwareOptions> uploadOption = helper.getUploadOptionFromMessage(message);
        Optional<DeviceMessage> activationMessage = helper.getActivationMessageForUploadMessage(message);
        return uploadOption.isPresent()
                && ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.equals(uploadOption.get())
                && activationMessage.isPresent()
                && DeviceMessageStatus.CONFIRMED.equals(activationMessage.get().getStatus());

    }

    private boolean verificationPrerequisitesForMessageWithActivateOnDateOption(DeviceMessage message, FirmwareManagementDeviceUtils helper){
        Optional<ProtocolSupportedFirmwareOptions> uploadOption = helper.getUploadOptionFromMessage(message);
        return uploadOption.isPresent()
                && ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.equals(uploadOption.get())
                && activationDateIsInPast(message, helper);
    }

    public enum Group {
        INSTALL(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER, Arrays.asList(
                VERIFICATION_SUCCESS,
                VERIFICATION_FAILED,
                VERIFICATION_TASK_FAILED,
                VERIFICATION_ONGOING,

                ACTIVATION_SUCCESS,
                ACTIVATION_FAILED,
                ACTIVATION_ONGOING,
                ACTIVATION_PENDING,

                UPLOAD_SUCCESS,
                UPLOAD_FAILED,
                UPLOAD_ONGOING,
                UPLOAD_PENDING
        )),
        ACTIVATE(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE, Arrays.asList(
                VERIFICATION_SUCCESS,
                VERIFICATION_FAILED,
                VERIFICATION_TASK_FAILED,
                VERIFICATION_ONGOING,

                UPLOAD_SUCCESS,
                UPLOAD_FAILED,
                UPLOAD_ONGOING,
                UPLOAD_PENDING
        )),
        ACTIVATE_ON_DATE(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE, Arrays.asList(
                VERIFICATION_SUCCESS,
                VERIFICATION_FAILED,
                VERIFICATION_TASK_FAILED,
                VERIFICATION_ONGOING,

                ACTIVATION_SUCCESS,
                ACTIVATION_ONGOING,

                UPLOAD_FAILED,
                UPLOAD_ONGOING,
                UPLOAD_PENDING
        )),
        ;

        private ProtocolSupportedFirmwareOptions managementOption;
        private List<FirmwareManagementDeviceStatus> possibleStatuses;

        Group(ProtocolSupportedFirmwareOptions managementOption, List<FirmwareManagementDeviceStatus> statuses) {
            this.managementOption = managementOption;
            this.possibleStatuses = statuses;
        }


        public Optional<FirmwareManagementDeviceStatus> getStatusBasedOnMessage(DeviceMessage message, FirmwareManagementDeviceUtils helper) {
            return this.possibleStatuses
                    .stream()
                    .filter(status -> status.validateMessage(message, helper))
                    .findFirst();
        }

        public static Optional<Group> getStatusGroupFor(ProtocolSupportedFirmwareOptions managementOption) {
            return Arrays.asList(Group.values())
                    .stream()
                    .filter(candidate -> candidate.managementOption.equals(managementOption))
                    .findFirst();
        }

    }

    public static class Constants {
        public static final String SUCCESS = "success";
        public static final String FAILED = "failed";
        public static final String ONGOING = "ongoing";
        public static final String PENDING = "pending";
        public static final String CONFIGURATION_ERROR = "configurationError";
        public static final String CANCELLED = "cancelled";
    }
}
