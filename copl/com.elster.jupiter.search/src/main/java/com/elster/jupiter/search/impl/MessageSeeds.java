package com.elster.jupiter.search.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.logging.Level;


public enum MessageSeeds implements MessageSeed {

    INVALID_VALUE(1, "InvalidValue", "Invalid value"),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;

    MessageSeeds(int number, String key, String defaultFormat) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    public static String getString(MessageSeed messageSeed, Thesaurus thesaurus, Object... args) {
        String text = thesaurus.getString(messageSeed.getKey(), messageSeed.getDefaultFormat());
        return MessageFormat.format(text, args);
    }

    @Override
    public String getModule() {
        return SearchService.COMPONENT_NAME;
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
        return Level.SEVERE;
    }
}
