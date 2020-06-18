/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.firmware.FirmwareService;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    FIELD_IS_REQUIRED(1, Keys.FIELD_IS_REQUIRED, "This field is required", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_NAME_LENGTH(2, Keys.FIELD_SIZE_BETWEEN_1_AND_NAME_LENGTH, "This field''s text length should be between 1 and " + Table.NAME_LENGTH + " symbols", Level.SEVERE),
    NAME_MUST_BE_UNIQUE(3, Keys.NAME_MUST_BE_UNIQUE, "Name must be unique", Level.SEVERE),
    MAX_FILE_SIZE_EXCEEDED(4, Keys.MAX_FILE_SIZE_EXCEEDED, "File size should be less than " + FirmwareService.MAX_FIRMWARE_FILE_SIZE / 1024 / 1024 + " MB", Level.SEVERE),
    STATE_TRANSFER_NOT_ALLOWED(5, Keys.STATE_TRANSFER_NOT_ALLOWED, "Transfer to requested state is not allowed", Level.SEVERE),
    DEVICE_TYPE_SHOULD_SUPPORT_FIRMWARE_UPGRADE(6, Keys.DEVICE_TYPE_SHOULD_SUPPORT_FIRMWARE_UPGRADE, "Device type should support firmware upgrade", Level.SEVERE),
    FILE_IS_EMPTY(8, Keys.FILE_IS_EMPTY, "Firmware file is empty", Level.SEVERE),
    NOT_FOUND_CAMPAIGN_FOR_COMTASK_EXECUTION(9, CampaignForComTaskExecutionExceptions.NO_CAMPAIGN_FOUND_FOR_COMTASKEXECUTION, "No campaign found for comtask {0}", Level.SEVERE),
    NO_CAMPAIGN_UNAMBIGUOUSLY_DETERMINED_FOR_COMTASK_EXECUTION(10, CampaignForComTaskExecutionExceptions.CAMPAIGN_NOT_UNAMBIGOUSLY_DETERMINED_FOR_COMTASKEXECUTION, "Campaign could not be determined unambiguously for comtask {0}", Level.SEVERE),
    FIRMWARE_FILE_IO(13, Keys.FIRMWARE_FILE_IO, "Exception while doing IO on firmware file: {0}", Level.SEVERE),
    FIELD_TOO_LONG(14, Keys.FIELD_TOO_LONG, "Field length must not exceed {max} characters", Level.SEVERE),
    VETO_SECURITY_ACCESSOR_DELETION(15, "securityAccessorStillInUseByDeviceTypes", "The security accessor couldn''t be removed because it is still used for firmware management on the following device type(s): {0}", Level.SEVERE),
    SIGNATURE_VALIDATION_FAILED(16, "SignatureValidationFailed", "Signature validation failed.", Level.SEVERE),
    INVALID_FIRMWARE_VERSIONS_PERMUTATION(17, "InvalidFirmwareVersionsPermutation", "The permutation of firmware versions isn''t valid. Their list may have changed since the page was last updated.", Level.SEVERE),
    WRONG_FIRMWARE_TYPE_FOR_METER_FW_DEPENDENCY(18, "WrongFirmwareTypeForMeterFWDependency", "{0} ''{1}'' can''t be selected as a minimal level meter firmware for ''{2}''.", Level.SEVERE),
    WRONG_FIRMWARE_TYPE_FOR_COM_FW_DEPENDENCY(19, "WrongFirmwareTypeForComFWDependency", "{0} ''{1}'' can''t be selected as a minimal communication firmware for ''{2}''.", Level.SEVERE),
    WRONG_RANK_FOR_METER_FW_DEPENDENCY(20, "WrongRankForMeterFWDependency", "Firmware ''{0}'' can''t have dependency on minimal level meter firmware ''{1}'' with a higher rank.", Level.SEVERE),
    WRONG_RANK_FOR_COM_FW_DEPENDENCY(21, "WrongRankForComFWDependency", "Firmware ''{0}'' can''t have dependency on minimal communication firmware ''{1}'' with a higher rank.", Level.SEVERE),
    TARGET_FIRMWARE_STATUS_NOT_ACCEPTED(22, "TargetFirmwareStatusNotAccepted", "Target firmware isn''t in the allowed status.", Level.WARNING),
    DEVICE_FIRMWARE_NOT_READOUT(23, "DeviceFirmwareNotReadout", "Firmware hasn''t been read out after the last upload.", Level.WARNING),
    MASTER_FIRMWARE_NOT_READOUT(24, "MasterFirmwareNotReadout", "Firmware on the master hasn''t been read out after the last upload.", Level.WARNING),
    UPLOADED_FIRMWARE_RANK_BELOW_CURRENT(27, "UploadedFirmwareRankBelowCurrent", "Target firmware version rank is lower than the current firmware rank.", Level.WARNING),
    MASTER_FIRMWARE_NOT_LATEST(28, "MasterFirmwareNotLatest", "Firmware types on the master don''t have the highest level (among firmware types with the acceptable status).", Level.WARNING),
    DEVICE_HAS_GHOST_FIRMWARE(29, "DeviceHasGhostFirmware", "There is firmware with \"Ghost\" status on the device.", Level.WARNING),
    MASTER_HAS_GHOST_FIRMWARE(30, "MasterHasGhostFirmware", "There is firmware with \"Ghost\" status on the master device.", Level.WARNING),
    WRONG_FIRMWARE_TYPE_FOR_AUX_FW_DEPENDENCY(31, "WrongFirmwareTypeForAuxFWDependency", "{0} ''{1}'' can''t be selected as a minimal auxiliary firmware for ''{2}''.", Level.SEVERE),
    WRONG_RANK_FOR_AUX_FW_DEPENDENCY(32, "WrongRankForAuxFWDependency", "Firmware ''{0}'' can''t have dependency on minimal auxiliary firmware ''{1}'' with a higher rank.", Level.SEVERE),
    FIRMWARES_BELOW_MINIMUM_LEVEL(34, "FirmwareBelowMinimumLevel", "Firmware of the following types is below the minimum level: {0}.", Level.WARNING),


    DEVICES_HAVENT_ADDED_BECAUSE_PART_OTHER_CAMPAIGN(1001, "DevicesHaventAddedBecausePartOtherCampaign", "''{0}'' devices haven''t been added to the campaign because they are part of another ongoing campaign.", Level.INFO),
    DEVICE_WAS_ADDED(1002, "DeviceWasAdded", "Device was added", Level.INFO),
    DEVICES_HAVENT_ADDED_BECAUSE_DIFFERENT_TYPE(1003, "DevicesHaventAddedBecauseDifferentType", "''{0}'' devices haven''t been added to the campaign because they are of a different type.", Level.INFO),
    DEVICES_WITH_GROUP_AND_TYPE_NOT_FOUND(1004, "DevicesWithGroupAndTypeNotFound", "Devices with group ''{0}'' and type ''{1}'' haven''t been found.", Level.INFO),
    CAMPAIGN_WAS_CANCELED_BECAUSE_DIDNT_RECEIVE_DEVICES(1005, "CampaignWasCancelledBecauseDidNotReceiveDevices", "Campaign was cancelled because it didn''t receive devices.", Level.INFO),
    DEVICE_GROUP_ISNT_FOUND(1006, "DeviceGroupIsntFound", "Device group ''{0}'' isn''t found.", Level.WARNING),
    COULDNT_FIND_SERVICE_CALL_TYPE(1007, "CouldntFindServiceCallType", "Couldn''t find a service call type {0} with version {1}.", Level.WARNING),
    DEFAULT_FIRMWARE_MANAGEMENT_TASK_CAN_NOT_BE_FOUND(1008, "DefaultFirmwareManagementTaskCanNotBeFound", "The default firmware management communication task can''t be found", Level.WARNING),
    DEFAULT_FIRMWARE_MANAGEMENT_TASK_IS_NOT_ACTIVE(1009, "DefaultFirmwareManagementTaskIsNotActive", "Firmware version cannot be changed because Firmware management communication task isn''t active on device configuration", Level.WARNING),
    DEVICES_HAVENT_ADDED_BECAUSE_HAVE_THIS_FIRMWARE_VERSION(1010, "DevicesHaventAddedBecauseHaveThisFirmwareVersion", "''{0}'' devices haven''t been added to the campaign because they already have this firmware version.", Level.INFO),
    DEVICE_HASNT_ADDED_BECAUSE_PART_OTHER_CAMPAIGN(1011, "DeviceHasntAddedBecausePartOtherCampaign", "1 device hasn''t been added to the campaign because it is part of another ongoing campaign.", Level.INFO),
    DEVICE_HASNT_ADDED_BECAUSE_DIFFERENT_TYPE(1012, "DeviceHasntAddedBecauseDifferentType", "1 device hasn''t been added to the campaign because it is of a different type.", Level.INFO),
    DEVICE_HASNT_ADDED_BECAUSE_HAVE_THIS_FIRMWARE_VERSION(1013, "DeviceHasntAddedBecauseHaveThisFirmwareVersion", "1 device hasn''t been added to the campaign because it already has this firmware version.", Level.INFO),

    CANCELED_BY_USER(3001, "CancelledByUser", "Cancelled by user.", Level.INFO),
    RETRIED_BY_USER(3002, "RetriedByUser", "Retried by user.", Level.INFO),

    FIRMWARE_INSTALLATION_FAILED(4001, "FirmwareInstallationFailed", "Firmware installation failed.", Level.WARNING),
    VERIFICATION_FAILED(4002, "VerificationFailed", "Verification failed.", Level.WARNING),
    ACTIVE_VERIFICATION_TASK_ISNT_FOUND(4003, "ActiveVerificationTaskIsntFound", "Active verification task isn''t found.", Level.WARNING),
    FIRMWARE_INSTALLATION_STARTED(4004, "FirmwareInstallationStarted", "Firmware installation started.", Level.INFO),
    VERIFICATION_COMPLETED(4005, "VerificationCompleted", "Verification completed", Level.INFO),
    VERIFICATION_SCHEDULED(4006, "VerificationScheduled", "Verification scheduled", Level.INFO),
    FIRMWARE_INSTALLATION_COMPLETED(4007, "FirmwareInstallationCompleted", "Firmware installation completed.", Level.INFO),
    VERIFICATION_FAILED_WRONG_FIRMWAREVERSION(4008, "VerificationFailedWrongFirmwareVersion", "Verification failed : wrong firmware version.", Level.WARNING),
    DEVICE_TYPE_DOES_NOT_ALLOW_FIRMWARE_MANAGEMENT(4009, "DeviceTypeNotAllowFirmwareManagement", "Unable to upgrade firmware version on device ''{0}'' due to check fail: ''Firmware upload is not allowed on the device type ''{1}''", Level.WARNING),
    DEVICE_CONFIGURATION_DOES_NOT_SUPPORT_FIRMWARE_MANAGEMENT(4010, "DeviceConfigurationDoesNotSupportFirmwareManagement", "Unable to upgrade firmware version on device ''{0}'' due to check fail: The firmware management communication task is not present on the device configuration ''{1}''", Level.WARNING),
    FIRMWARE_UPLOAD_CURRENTLY_ONGOING(4011, "FirmwareUploadOfFirmwareIsCurrentlyOngoing", "Unable to upgrade firmware version on device ''{0}'' due to check fail: Firmware upload of firmware is currently ongoing.", Level.WARNING),
    PROTOCOL_DOES_NOT_SUPPORT_UPLOADING_FIRMWARE(4012, "ProtocolOfTheDeviceTypeDoesNotSupportUploadingFirmware", "Unable to upgrade firmware version on device ''{0}'' due to check fail: The protocol of the device type ''{1}'' doesn''t support uploading firmware.", Level.WARNING),
    CONNECTION_WINDOW_OUTSIDE_OF_CAMPAIGN_TIME_BOUNDARY(4013, "ConnectionWindowOutsideOfCampaignTimeBoundary", "Unable to upgrade firmware version on device ''{0}'' due to check fail: Connection window start of the connection method used by the firmware management communication task of the device is outside of the time boundary of the campaign.", Level.WARNING),
    TASK_FOR_VALIDATION_IS_MISSING(4014, "TaskForValidationIsMissing", "Communication task required for validation is missing on the device configuration, doesn''t meet the necessary conditions or is inactive on device/device type level", Level.WARNING),
    TASK_FOR_SENDING_FIRMWARE_IS_MISSING(4015, "TaskForSendingFirmwareIsMissing", "Communication task for sending firmware is missing on the device configuration, doesn''t meet the necessary conditions or is inactive on device/device type level", Level.WARNING),
    CONNECTION_METHOD_DOESNT_MEET_THE_REQUIREMENT(4016, "ConnectionMethodDoesntMeetTheRequirement", "The connection method ''{0}'' set on ''{1}'' doesn''t match the one required on the firmware campaign", Level.WARNING),
    DEVICE_PART_OF_CAMPAIGN(4017, "DeviceIsPartOfAnotherCampaign", "Couldn''t restart service call: the device is a part of another campaign", Level.SEVERE),
    CAMPAIGN_ALREADY_CANCELLED(4019, "CampaignAlreadyCancelled", "The campaign has already been cancelled.", Level.WARNING),
    DEVICE_IS_NOT_PENDING_STATE(4020, "DeviceIsNotPendingState", "The device service call isn''t in pending state.", Level.WARNING),
    CAMPAIGN_WITH_DEVICE_CANCELLED(4021, "CampaignWithDeviceCancelled", "The campaign with this device has already been cancelled.", Level.WARNING),
    CONNECTION_METHOD_MISSING_ON_COMTASK(4022, "ConnectionMissingOnComTask", "Communication task ''{0}'' doesn''t refer to any connection method.", Level.WARNING),
    TASK_FOR_VALIDATION_LOST_ACTION(4023, "TaskForValidationLostAction", "Task for validation has been changed and doesn''t have the necessary action.", Level.WARNING),
    ;


    private final int number;
    private final String key;
    private final String format;
    private final Level level;

    MessageSeeds(int number, String key, String format, Level level) {
        this.number = number;
        this.key = key;
        this.format = format;
        this.level = level;
    }

    @Override
    public String getModule() {
        return FirmwareService.COMPONENTNAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    public String format(Thesaurus thesaurus, Object... args) {
        if (thesaurus == null) {
            throw new IllegalArgumentException("Thesaurus can't be null");
        }
        return thesaurus.getFormat(this).format(args);
    }

    public static class Keys {
        public static final String FIELD_IS_REQUIRED = "FieldIsRequired";
        public static final String FIELD_SIZE_BETWEEN_1_AND_NAME_LENGTH = "FieldSizeBetween1and80";
        public static final String NAME_MUST_BE_UNIQUE = "NameMustBeUnique";
        public static final String MAX_FILE_SIZE_EXCEEDED = "MaxFileSizeExceeded";
        public static final String FILE_IS_EMPTY = "FileIsEmpty";
        public static final String STATE_TRANSFER_NOT_ALLOWED = "StateTransferNotAllowed";
        public static final String DEVICE_TYPE_SHOULD_SUPPORT_FIRMWARE_UPGRADE = "DeviceTypeShouldSupportFirmwareUpgrade";
        public static final String FIRMWARE_FILE_IO = "FileIO";
        public static final String FIELD_TOO_LONG = "invalidFieldLength";
    }
}
