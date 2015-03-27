package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.firmware.rest.FirmwareApplication;

import java.text.MessageFormat;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {
    VERSION_IN_USE(1, Keys.VERSION_IN_USE, "This version is in use and can''t be modified"),
    VERSION_IS_DEPRECATED(2, Keys.VERSION_IS_DEPRECATED, "This version is deprecated and can''t be modified"),
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
    }
}
