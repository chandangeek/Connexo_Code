package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.rest.FirmwareApplication;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import java.text.MessageFormat;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {
    // firmware versions
    VERSION_IN_USE(1, Keys.VERSION_IN_USE, "This version is in use and can''t be modified"),
    VERSION_IS_DEPRECATED(2, Keys.VERSION_IS_DEPRECATED, "This version is deprecated and can''t be modified"),
    // firmware statuses translation
    STATUS_GHOST(3, Keys.STATUS_GHOST, FirmwareStatus.GHOST.getDisplayValue()),
    STATUS_TEST(4, Keys.STATUS_TEST, FirmwareStatus.TEST.getDisplayValue()),
    STATUS_FINAL(5, Keys.STATUS_FINAL, FirmwareStatus.FINAL.getDisplayValue()),
    STATUS_DEPRECATED(6, Keys.STATUS_DEPRECATED, FirmwareStatus.DEPRECATED.getDisplayValue()),
    // firmware types translation
    TYPE_METER(7, Keys.TYPE_METER, FirmwareType.METER.getDisplayValue()),
    TYPE_COMMUNICATION(8, Keys.TYPE_COMMUNICATION, FirmwareType.COMMUNICATION.getDisplayValue()),
    // firmware upgrade options translation
    UPGRADE_OPTION_INSTALL(9, Keys.UPGRADE_OPTION_INSTALL, "Upload firmware"),
    UPGRADE_OPTION_ACTIVATE(10, Keys.UPGRADE_OPTION_ACTIVATE, "Upload firmware and activate immediately"),
    UPGRADE_OPTION_ACTIVATE_ON_DATE(11, Keys.UPGRADE_OPTION_ACTIVATE_ON_DATE, "Upload firmware with activation date"),
    // firmware upgrade options
    UPGRADE_OPTIONS_REQUIRED(12, Keys.UPGRADE_OPTIONS_REQUIRED, "At least one option should be selected"),
    ;
    private final int number;
    private final String key;
    private final String format;

    private MessageSeeds(int number, String key, String format) {
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
        public static final String VERSION_IS_DEPRECATED = "VersionInDeprecated";
        public static final String STATUS_GHOST = FirmwareStatus.GHOST.getStatus();
        public static final String STATUS_TEST = FirmwareStatus.TEST.getStatus();
        public static final String STATUS_FINAL = FirmwareStatus.FINAL.getStatus();
        public static final String STATUS_DEPRECATED = FirmwareStatus.DEPRECATED.getStatus();
        public static final String TYPE_METER = FirmwareType.METER.getType();
        public static final String TYPE_COMMUNICATION = FirmwareType.COMMUNICATION.getType();
        public static final String UPGRADE_OPTION_INSTALL = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId();
        public static final String UPGRADE_OPTION_ACTIVATE = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId();
        public static final String UPGRADE_OPTION_ACTIVATE_ON_DATE = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.getId();
        public static final String UPGRADE_OPTIONS_REQUIRED = "FirmwareUpgradeOptionsRequired";
    }
}
