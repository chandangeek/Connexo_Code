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
    MAX_FILE_SIZE_EXCEEDED(4, Keys.MAX_FILE_SIZE_EXCEEDED, "File size should be less than " + FirmwareService.MAX_FIRMWARE_FILE_SIZE/1024/1024 + " MB", Level.SEVERE),
    STATE_TRANSFER_NOT_ALLOWED(5, Keys.STATE_TRANSFER_NOT_ALLOWED, "Transfer to requested state is not allowed", Level.SEVERE),
    DEVICE_TYPE_SHOULD_SUPPORT_FIRMWARE_UPGRADE(6, Keys.DEVICE_TYPE_SHOULD_SUPPORT_FIRMWARE_UPGRADE, "Device type should support firmware upgrade", Level.SEVERE),
    FILE_IS_EMPTY(8, Keys.FILE_IS_EMPTY, "Firmware file is empty", Level.SEVERE),
    NOT_FOUND_CAMPAIGN_FOR_COMTASK_EXECUTION(9 , CampaignForComTaskExecutionExceptions.NO_CAMPAIGN_FOUND_FOR_COMTASKEXECUTION, "No campaign found for comtask {0}", Level.SEVERE),
    NO_CAMPAIGN_UNAMBIGUOUSLY_DETERMINED_FOR_COMTASK_EXECUTION(10 , CampaignForComTaskExecutionExceptions.CAMPAIGN_NOT_UNAMBIGOUSLY_DETERMINED_FOR_COMTASKEXECUTION, "Campaign could not be determined unambiguously for comtask {0}", Level.SEVERE),
    FIRMWARE_CAMPAIGN_STATUS_INVALID(11, RetryDeviceInFirmwareCampaignExceptions.CAMPAIGN_IS_NOT_ONGOING, "Cannot change status as campaign is not ongoing", Level.SEVERE),
    DEVICE_IN_FIRMWARE_CAMPAIGN_STATE_INVALID(12, RetryDeviceInFirmwareCampaignExceptions.DEVICE_IN_FIRMWARE_CAMPAIGN_STATE_CHANGE_TO_PENDING_NOT_ALLOWED, "Cannot change status to {0} from current status {1}.", Level.SEVERE),
    FIRMWARE_FILE_IO(13, Keys.FIRMWARE_FILE_IO, "Exception while doing IO on firmware file: {0}", Level.SEVERE),
    FIELD_TOO_LONG(14, Keys.FIELD_TOO_LONG, "Field length must not exceed {max} characters", Level.SEVERE),
    VETO_SECURITY_ACCESSOR_DELETION(15, "securityAccessorStillInUseByDeviceTypes", "The security accessor couldn''t be removed because it is still used for firmware management on the following device type(s): {0}", Level.SEVERE),
    SIGNATURE_VALIDATION_FAILED(16, "SignatureValidationFailed", "Signature validation failed.", Level.SEVERE),
    DEVICE_FIRMWARE_NOT_READOUT(17, "DeviceFirmwareNotReadout", "Device firmware hasn''t been read out after last upload.", Level.WARNING),
    MASTER_FIRMWARE_NOT_READOUT(18, "MasterFirmwareNotReadout", "Master firmware hasn''t been read out after last upload.", Level.WARNING),
    CURRENT_FIRMWARE_RANK_BELOW_MINIMUM_SUPPORTED(19, "FirmwareRankBelowMinimumSupported", "{0} has its rank below the requested minimum level.", Level.WARNING),
    UPLOADED_FIRMWARE_RANK_BELOW_CURRENT(20, "UploadedFirmwareRankBelowCurrent", "Uploaded firmware has a lower rank than current.", Level.WARNING),
    MASTER_FIRMWARE_NOT_LATEST(21, "MasterFirmwareNotLatest", "Master device doesn''t have the latest firmware.", Level.WARNING),
    CURRENT_FIRMWARE_IS_GHOST(22, "CurrentFirmwareIsGhost", "There is firmware with ghost status on the device.", Level.WARNING),
    INVALID_FIRMWARE_VERSIONS_PERMUTATION(23, "InvalidFirmwareVersionsPermutation", "The permutation of firmware versions isn''t valid. Their list may have changed since the page was last updated.", Level.SEVERE)
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

    public String format(Thesaurus thesaurus, Object... args){
        if (thesaurus == null){
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
