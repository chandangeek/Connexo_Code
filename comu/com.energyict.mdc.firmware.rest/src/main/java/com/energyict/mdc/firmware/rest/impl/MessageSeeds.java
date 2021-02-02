/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    // firmware versions
    VERSION_IN_USE(1, Keys.VERSION_IN_USE, "This version is in use and can''t be modified"),
    VERSION_IS_DEPRECATED(2, Keys.VERSION_IS_DEPRECATED, "This version is deprecated and can''t be modified"),
    // TODO: UPGRADE_OPTIONS and some other labels below are not message seeds, rather translation keys
    // firmware upgrade options translation
    UPGRADE_OPTION_INSTALL(9, Keys.UPGRADE_OPTION_INSTALL, "Upload firmware/image and activate later"),
    UPGRADE_OPTION_ACTIVATE(10, Keys.UPGRADE_OPTION_ACTIVATE, "Upload firmware/image and activate immediately"),
    UPGRADE_OPTION_ACTIVATE_ON_DATE(11, Keys.UPGRADE_OPTION_ACTIVATE_ON_DATE, "Upload firmware/image with activation date"),

    UPGRADE_OPTIONS_REQUIRED(12, Keys.UPGRADE_OPTIONS_REQUIRED, "At least one option should be selected"),

    DEVICE_TYPE_NOT_FOUND(13, Keys.DEVICE_TYPE_NOT_FOUND, "No device type with id {0} could be found"),
    DEVICE_NOT_FOUND(14, Keys.DEVICE_NOT_FOUND, "No device {0} could be found"),
    MAX_FILE_SIZE_EXCEEDED(15, Keys.MAX_FILE_SIZE_EXCEEDED, "File size should be less than " + FirmwareService.MAX_FIRMWARE_FILE_SIZE / 1024 / 1024 + " Mb"),
    FILE_IO(16, Keys.FILE_IO, "Failure while doing IO on file"),
    FIRMWARE_CAMPAIGN_NOT_FOUND(17, Keys.FIRMWARE_CAMPAIGN_NOT_FOUND, "No firmware campaign with id {0} could be found"),
    DEVICE_GROUP_NOT_FOUND(18, Keys.DEVICE_GROUP_NOT_FOUND, "No device group with id {0} could be found"),
    FIRMWARE_VERSION_NOT_FOUND(19, Keys.FIRMWARE_VERSION_NOT_FOUND, "No firmware version with id {0} could be found"),
    FIRMWARE_VERSION_MISSING(20, Keys.FIRMWARE_VERSION_MISSING, "Firmware version is missing in the request"),
    FIRMWARE_ACTION_CHECK_VERSION(21, Keys.FIRMWARE_ACTION_CHECK_VERSION, "Check firmware version/image"),
    INVALID_VALUE(22, "InvalidValue", "Invalid value"),

    SUPPORTED_FIRMWARE_UPGRADE_OPTIONS_NOT_FOUND(1001, "SupportedFirmwareUpgradeOptionsNotFound", "There is no such supported firmware upgrade option"),
    FIRMWARE_UPGRADE_OPTIONS_ARE_DISABLED_FOR_DEVICE_TYPE(1002, "FirmwareUpgradeOptionsAreDisabledForDeviceType", "Firmware upgrade options are disabled for device type"),
    DEFAULT_FIRMWARE_MANAGEMENT_TASK_CAN_NOT_BE_FOUND(1003, "DefaultFirmwareManagementTaskCanNotBeFound", "The default firmware management communication task cannot be found"),
    DEFAULT_FIRMWARE_MANAGEMENT_TASK_IS_NOT_ACTIVE(1004, "DefaultFirmwareManagementTaskIsNotActive", "Firmware version cannot be changed because Firmware management communication task is not active on device configuration"),
    FIRMWARE_UPGRADE_OPTION_ARE_DISABLED_FOR_DEVICE_TYPE(1005, "FirmwareUpgradeOptionAreDisabledForDeviceType", "This firmware upgrade option are disabled for device type"),
    FIRMWARE_UPLOAD_HAS_BEEN_STARTED_CANNOT_BE_CANCELED(1006, "FirmwareUploadHasBeenStartedCannotBeCancelled", "Firmware upload has been started and can''t be cancelled"),
    FIRMWARE_UPLOAD_NOT_FOUND(1007, "FirmwareUploadNotFound", "No firmware upload process with id {0} could be found"),
    FIRMWARE_CANNOT_BE_ACTIVATED(1008, "FirmwareCannotBeActivated", "Firmware version cannot be activated"),
    FIRMWARE_CHECK_TASK_IS_NOT_ACTIVE(1009, "FirmwareCheckTaskIsNotActive", "The ''Check firmware version'' action can''t be executed because there is no communication task with the ''Status information - Read'' action on the device configuration of this device."),
    COM_TASK_IS_NOT_ENABLED_FOR_THIS_DEVICE(1010, "comTaskNotFound", "Could not find communication task with id {0}"),
    FIRMWARE_ACTIVATION_DATE_IS_BEFORE_UPLOAD(1011, "FirmwareActivationDateIsBeforeUpload", "This date should be after the ''Upload file'' date"),

    FIRMWARE_CHECK_TASK_CONCURRENT_FAIL_TITLE(1012, "FirmwareCheckTaskConcurrentFail", "Failed to run ''{0}''"),
    FIRMWARE_CHECK_TASK_CONCURRENT_FAIL_BODY(1013, "FirmwareCheckTaskConcurrentBody", "{0} has changed since the page was last updated."),
    FIRMWARE_COMMUNICATION_TASK_NAME(1014, "FirmwareTaskName", "Firmware communication task"),
    NOT_ABLE_TO_CREATE_CAMPAIGN(1015, "NotAbleToCreateCampaign", "Not able to createInfo a new firmware campaign from the specified information"),
    SECURITY_ACCESSOR_EXPIRED(1018, "SecurityAccessorExpired", "Security accessor expired."),
    SIGNATURE_VALIDATION_FAILED(1019, "SignatureValidationFailed", "Incorrect firmware file: image signature verification failed."),
    FIRMWARE_UPLOAD_RETRIED(1020, "FirmwareUploadRetried", "Firmware upload retried."),
    VERIFICATION_RETRIED(1021, "VerificationRetried", "Verification retried."),

    DEVICETYPE_WITH_ID_ISNT_FOUND(2000, "DeviceTypeWithIdIsntFound", "Device type with id {0} isn''t found."),
    PROTOCOL_WITH_ID_ISNT_FOUND(2001, "ProtocolWithIdIsntFound", "Protocol supported firmware option with id ''{0}'' isn''t found.");

    private final int number;
    private final String key;
    private final String format;

    MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return FirmwareApplication.COMPONENT_NAME;
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
        return Level.SEVERE;
    }

    public static class Keys {
        public static final String VERSION_IN_USE = "VersionInUse";
        public static final String VERSION_IS_DEPRECATED = "VersionIsDeprecated";
        public static final String UPGRADE_OPTION_INSTALL = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId();
        public static final String UPGRADE_OPTION_ACTIVATE = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId();
        public static final String UPGRADE_OPTION_ACTIVATE_ON_DATE = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.getId();
        public static final String UPGRADE_OPTIONS_REQUIRED = "FirmwareUpgradeOptionsRequired";
        public static final String DEVICE_TYPE_NOT_FOUND = "deviceTypeNotFound";
        public static final String DEVICE_NOT_FOUND = "deviceNotFound";
        public static final String MAX_FILE_SIZE_EXCEEDED = "MaxFileSizeExceeded";
        public static final String FILE_IO = "FileIO";
        public static final String FIRMWARE_ACTION_CHECK_VERSION = "FirmwareActionCheckVersion";
        public static final String FIRMWARE_CAMPAIGN_NOT_FOUND = "firmwareCampaignNotFound";
        public static final String FIRMWARE_VERSION_NOT_FOUND = "firmwareVersionNotFound";
        public static final String FIRMWARE_VERSION_MISSING = "firmwareVersionMissing";
        public static final String DEVICE_GROUP_NOT_FOUND = "deviceGroupNotFound";

    }
}
