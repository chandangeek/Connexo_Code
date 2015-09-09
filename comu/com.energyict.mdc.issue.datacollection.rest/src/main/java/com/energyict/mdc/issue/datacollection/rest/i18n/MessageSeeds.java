package com.energyict.mdc.issue.datacollection.rest.i18n;

import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;


public enum MessageSeeds implements MessageSeed {

    ISSUE_DOES_NOT_EXIST(1, "IssueDoesNotExist", "Issue doesn't exist"),
    ISSUE_ALREADY_CLOSED(2, "IssueAlreadyClosed", "Issue already closed"),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return IssueDataCollectionService.COMPONENT_NAME;
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

}