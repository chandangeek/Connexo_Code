package com.elster.jupiter.domain.util.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import java.util.logging.Level;

/**
 * Created by bvn on 5/26/15.
 */
public enum MessageSeeds implements MessageSeed {
    MAX_PAGE_SIZE_EXCEEDED(1, "The maximum page size of {0} was exceeded"),
    UNPAGED_NOT_ALLOWED(2, "This query can only be called with paging parameters");

    private final int number;
    private final String defaultFormat;

    MessageSeeds(int number, String defaultFormat) {
        this.number = number;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getModule() {
        return "DUM";
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }
}
