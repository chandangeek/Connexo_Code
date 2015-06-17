package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.firmware.FirmwareService;

import java.text.MessageFormat;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {
    FIELD_IS_REQUIRED(1, Keys.FIELD_IS_REQUIRED, "This field is required", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_NAME_LENGTH(2, Keys.FIELD_SIZE_BETWEEN_1_AND_NAME_LENGTH, "Field's text length should be between 1 and " + Table.NAME_LENGTH + " symbols", Level.SEVERE),
    NAME_MUST_BE_UNIQUE(3, Keys.NAME_MUST_BE_UNIQUE, "The name must be unique", Level.SEVERE),
    MAX_FILE_SIZE_EXCEEDED(4, Keys.MAX_FILE_SIZE_EXCEEDED, "File size should be less than " + FirmwareService.MAX_FIRMWARE_FILE_SIZE/1024/1024 + " MB", Level.SEVERE),
    STATE_TRANSFER_NOT_ALLOWED(5, Keys.STATE_TRANSFER_NOT_ALLOWED, "Transfer to requested state is not allowed", Level.SEVERE),
    DEVICE_TYPE_SHOULD_SUPPORT_FIRMWARE_UPGRADE(6, Keys.DEVICE_TYPE_SHOULD_SUPPORT_FIRMWARE_UPGRADE, "Device type should support firmware upgrade", Level.SEVERE),
    MESSAGE_SUBSCRIBER_NAME(7, FirmwareCampaignHandlerFactory.FIRMWARE_CAMPAIGNS_SUBSCRIBER, "Process firmware campaigns", Level.INFO),
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

    public String format(Thesaurus thesaurus, Object... args){
        if (thesaurus == null){
            throw new IllegalArgumentException("Thesaurus can't be null");
        }
        String translated = thesaurus.getString(this.getKey(), this.getDefaultFormat());
        return MessageFormat.format(translated, args);
    }

    @Override
    public Level getLevel() {
        return level;
    }

    public static class Keys {
        public static final String FIELD_IS_REQUIRED = "FieldIsRequired";
        public static final String FIELD_SIZE_BETWEEN_1_AND_NAME_LENGTH = "FieldSizeBetween1and80";
        public static final String NAME_MUST_BE_UNIQUE = "NameMustBeUnique";
        public static final String MAX_FILE_SIZE_EXCEEDED = "MaxFileSizeExceeded";
        public static final String STATE_TRANSFER_NOT_ALLOWED = "StateTransferNotAllowed";
        public static final String DEVICE_TYPE_SHOULD_SUPPORT_FIRMWARE_UPGRADE = "DeviceTypeShouldSupportFirmwareUpgrade";
    }
}
