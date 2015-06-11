package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.firmware.FirmwareManagementDeviceStatus;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.rest.FirmwareApplication;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import java.text.MessageFormat;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {
    // firmware versions
    VERSION_IN_USE(1, Keys.VERSION_IN_USE, "This version is in use and can''t be modified"),
    VERSION_IS_DEPRECATED(2, Keys.VERSION_IS_DEPRECATED, "This version is deprecated and can''t be modified"),
    // firmware statuses translation
    STATUS_GHOST(3, Keys.STATUS_GHOST, "Ghost"),
    STATUS_TEST(4, Keys.STATUS_TEST, "Test"),
    STATUS_FINAL(5, Keys.STATUS_FINAL, "Final"),
    STATUS_DEPRECATED(6, Keys.STATUS_DEPRECATED, "Deprecated"),
    // firmware types translation
    TYPE_METER(7, Keys.TYPE_METER, "Meter firmware"),
    TYPE_COMMUNICATION(8, Keys.TYPE_COMMUNICATION, "Communication firmware"),
    // firmware upgrade options translation
    UPGRADE_OPTION_INSTALL(9, Keys.UPGRADE_OPTION_INSTALL, "Upload firmware and activate later"),
    UPGRADE_OPTION_ACTIVATE(10, Keys.UPGRADE_OPTION_ACTIVATE, "Upload firmware and activate immediately"),
    UPGRADE_OPTION_ACTIVATE_ON_DATE(11, Keys.UPGRADE_OPTION_ACTIVATE_ON_DATE, "Upload firmware with activation date"),
    // firmware upgrade options
    UPGRADE_OPTIONS_REQUIRED(12, Keys.UPGRADE_OPTIONS_REQUIRED, "At least one option should be selected"),
   
    DEVICE_TYPE_NOT_FOUND(13, Keys.DEVICE_TYPE_NOT_FOUND, "No device type with id {0} could be found"),
    DEVICE_NOT_FOUND(14, Keys.DEVICE_NOT_FOUND, "No device with id {0} could be found"),
    MAX_FILE_SIZE_EXCEEDED(15, Keys.MAX_FILE_SIZE_EXCEEDED, "File size should be less than " + FirmwareService.MAX_FIRMWARE_FILE_SIZE/1024/1024 + " Mb"),
    FILE_IO(16, Keys.FILE_IO, "Failure while doing IO on file"),
    FIRMWARE_VERSION_NOT_FOUND(14, Keys.FIRMWARE_VERSION_NOT_FOUND, "No firmware version with id {0} could be found"),
    FIRMWARE_ACTION_CHECK_VERSION (15, Keys.FIRMWARE_ACTION_CHECK_VERSION, "Check firmware version"),
    FIRMWARE_ACTION_CHECK_VERSION_NOW (16, Keys.FIRMWARE_ACTION_CHECK_VERSION_NOW, "Check firmware version now"),
    FIRMWARE_CAMPAIGN_NOT_FOUND(17, Keys.FIRMWARE_CAMPAIGN_NOT_FOUND, "No firmware campaign with id {0} could be found"),
    DEVICE_GROUP_NOT_FOUND(18, Keys.DEVICE_GROUP_NOT_FOUND, "No device group with id {0} could be found"),

    FIRMWARE_CAMPAIGN_STATUS_PROCESSING(50, Keys.FIRMWARE_CAMPAIGN_STATUS_PROCESSING, "Processing"),
    FIRMWARE_CAMPAIGN_STATUS_SCHEDULED(51, Keys.FIRMWARE_CAMPAIGN_STATUS_SCHEDULED , "Scheduled"),
    FIRMWARE_CAMPAIGN_STATUS_ONGOING(52, Keys.FIRMWARE_CAMPAIGN_STATUS_ONGOING , "Ongoing"),
    FIRMWARE_CAMPAIGN_STATUS_COMPLETE(53, Keys.FIRMWARE_CAMPAIGN_STATUS_COMPLETE , "Complete"),
    FIRMWARE_CAMPAIGN_STATUS_CANCELLED(54, Keys.FIRMWARE_CAMPAIGN_STATUS_CANCELLED , "Cancelled"),

    FIRMWARE_MANAGEMENT_DEVICE_STATUS_SUCCESS(101, Keys.FIRMWARE_MANAGEMENT_DEVICE_STATUS_PREFIX + FirmwareManagementDeviceStatus.Constants.SUCCESS, "Success"),
    FIRMWARE_MANAGEMENT_DEVICE_STATUS_FAILED(102, Keys.FIRMWARE_MANAGEMENT_DEVICE_STATUS_PREFIX + FirmwareManagementDeviceStatus.Constants.FAILED, "Failed"),
    FIRMWARE_MANAGEMENT_DEVICE_STATUS_ONGOING(103, Keys.FIRMWARE_MANAGEMENT_DEVICE_STATUS_PREFIX + FirmwareManagementDeviceStatus.Constants.ONGOING, "Ongoing"),
    FIRMWARE_MANAGEMENT_DEVICE_STATUS_PENDING(104, Keys.FIRMWARE_MANAGEMENT_DEVICE_STATUS_PREFIX + FirmwareManagementDeviceStatus.Constants.PENDING, "Pending"),
    FIRMWARE_MANAGEMENT_DEVICE_STATUS_CONFIGURATION_ERROR(105, Keys.FIRMWARE_MANAGEMENT_DEVICE_STATUS_PREFIX + FirmwareManagementDeviceStatus.Constants.CONFIGURATION_ERROR, "Configuration error"),
    FIRMWARE_MANAGEMENT_DEVICE_STATUS_CANCELLED(106, Keys.FIRMWARE_MANAGEMENT_DEVICE_STATUS_PREFIX + FirmwareManagementDeviceStatus.Constants.CANCELLED, "Cancelled"),


    SUPPORTED_FIRMWARE_UPGRADE_OPTIONS_NOT_FOUND(1001, "SupportedFirmwareUpgradeOptionsNotFound", "There is no such supported firmware upgrade option"),
    FIRMWARE_UPGRADE_OPTIONS_ARE_DISABLED_FOR_DEVICE_TYPE(1002, "FirmwareUpgradeOptionsAreDisabledForDeviceType", "Firmware upgrade options are disabled for device type"),
    DEFAULT_FIRMWARE_MANAGEMENT_TASK_CAN_NOT_BE_FOUND(1003, "DefaultFirmwareManagementTaskCanNotBeFound", "The default firmware management communication task cannot be found"),
    DEFAULT_FIRMWARE_MANAGEMENT_TASK_IS_NOT_ACTIVE(1004, "DefaultFirmwareManagementTaskIsNotActive", "Firmware version cannot be changed because Firmware management communication task is not active on device configuration"),
    FIRMWARE_UPGRADE_OPTION_ARE_DISABLED_FOR_DEVICE_TYPE(1005, "FirmwareUpgradeOptionAreDisabledForDeviceType", "This firmware upgrade option are disabled for device type"),
    FIRMWARE_UPLOAD_HAS_BEEN_STARTED_CANNOT_BE_CANCELED(1006, "FirmwareUploadHasBeenStartedCannotBeCanceled", "Firmware upload has been started and cannot be canceled"),
    FIRMWARE_UPLOAD_NOT_FOUND(1007, "FirmwareUploadNotFound", "No firmware upload process with id {0} could be found"),
    FIRMWARE_CANNOT_BE_ACTIVATED(1008, "FirmwareCannotBeActivated", "Firmware version cannot be activated"),
    FIRMWARE_CHECK_TASK_IS_NOT_ACTIVE(1009, "FirmwareCheckTaskIsNotActive", "It is impossible to check firmware version because Check firmware version communication task is not active on device configuration"),
    COM_TASK_IS_NOT_ENABLED_FOR_THIS_DEVICE(1010, "comTaskNotFound", "Could not find communication task with id {0}"),
    ;
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

    public String format(Thesaurus thesaurus, Object... args){
        if (thesaurus == null){
            throw new IllegalArgumentException("Thesaurus can't be null");
        }
        String translated = thesaurus.getString(this.getKey(), this.getDefaultFormat());
        return MessageFormat.format(translated, args);
    }
    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

    public static class Keys {
        public static final String VERSION_IN_USE = "VersionInUse";
        public static final String VERSION_IS_DEPRECATED = "VersionIsDeprecated";
        public static final String STATUS_GHOST = "ghost";
        public static final String STATUS_TEST = "test";
        public static final String STATUS_FINAL = "final";
        public static final String STATUS_DEPRECATED = "deprecated";
        public static final String TYPE_METER = "meter";
        public static final String TYPE_COMMUNICATION = "communication";
        public static final String UPGRADE_OPTION_INSTALL = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId();
        public static final String UPGRADE_OPTION_ACTIVATE = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId();
        public static final String UPGRADE_OPTION_ACTIVATE_ON_DATE = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.getId();
        public static final String UPGRADE_OPTIONS_REQUIRED = "FirmwareUpgradeOptionsRequired";
        public static final String DEVICE_TYPE_NOT_FOUND = "deviceTypeNotFound";
        public static final String DEVICE_NOT_FOUND = "deviceNotFound";
        public static final String MAX_FILE_SIZE_EXCEEDED = "MaxFileSizeExceeded";
        public static final String FILE_IO = "FileIO";
        public static final String FIRMWARE_ACTION_CHECK_VERSION = "FirmwareActionCheckVersion";
        public static final String FIRMWARE_ACTION_CHECK_VERSION_NOW = "FirmwareActionCheckVersionNow";
        public static final String FIRMWARE_CAMPAIGN_STATUS_PROCESSING = "FirmwareCampaignStatusProcessing";
        public static final String FIRMWARE_CAMPAIGN_STATUS_SCHEDULED = "FirmwareCampaignStatusScheduled";
        public static final String FIRMWARE_CAMPAIGN_STATUS_ONGOING = "FirmwareCampaignStatusOngoing";
        public static final String FIRMWARE_CAMPAIGN_STATUS_COMPLETE = "FirmwareCampaignStatusComplete";
        public static final String FIRMWARE_CAMPAIGN_STATUS_CANCELLED = "FirmwareCampaignStatusCancelled";
        public static final String FIRMWARE_CAMPAIGN_NOT_FOUND = "firmwareCampaignNotFound";
        public static final String FIRMWARE_VERSION_NOT_FOUND = "firmwareVersionNotFound";
        public static final String DEVICE_GROUP_NOT_FOUND = "deviceGroupNotFound";
        public static final String FIRMWARE_MANAGEMENT_DEVICE_STATUS_PREFIX = "FirmwareManagementDeviceStatus.";

    }
}
