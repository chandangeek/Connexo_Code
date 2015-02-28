package com.elster.jupiter.systemadmin.rest.resource;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.systemadmin.rest.LicensingApplication;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.logging.Level;

public enum  MessageSeeds implements MessageSeed, TranslationKey {

    PURGE_HISTORY_DOES_NOT_EXIST(1, "PurgeHostoryDoesNotExist", "The purge history record with id = '{0}' doesn't exist", Level.WARNING),
    INVALID_LICENSE_FILE(2, "InvalidLicenseFile", "Invalid license file", Level.SEVERE),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return LicensingApplication.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    public static String getString(MessageSeed messageSeed, Thesaurus thesaurus, Object... args){
        String text = thesaurus.getString(messageSeed.getKey(), messageSeed.getDefaultFormat());
        return MessageFormat.format(text, args);
    }

    public static Optional<MessageSeeds> getByKey(String key) {
        if (key != null) {
            for (MessageSeeds column : MessageSeeds.values()) {
                if (column.getKey().equals(key)) {
                    return Optional.of(column);
                }
            }
        }
        return Optional.empty();
    }
}
