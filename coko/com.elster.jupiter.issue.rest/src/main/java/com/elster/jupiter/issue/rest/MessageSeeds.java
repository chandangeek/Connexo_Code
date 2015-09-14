package com.elster.jupiter.issue.rest;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    ISSUE_DOES_NOT_EXIST (0001, "IssueDoesNotExist", "Issue doesn't exist", Level.SEVERE),
    ISSUE_ALREADY_CLOSED(0002, "IssueAlreadyClosed", "Issue already closed", Level.SEVERE),
    ISSUE_ACTION_CLASS_LOAD_FAIL(0003, "IssueActionClassLoadFail", "Unable to load Action class \"{0}\" for \"{1}\" action type", Level.SEVERE),
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
        return IssueService.COMPONENT_NAME;
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
