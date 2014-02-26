package com.elster.jupiter.issue.module;

import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    ISSUE_NOT_PRESENT(1001, "issue.not.present", "Issue doesn't exist", Level.SEVERE),
    ISSUE_ALREADY_CHANGED(1002, "issue.already.changed", "Issue has been already changed", Level.SEVERE),
    ISSUE_ASSIGNEE_BAD(1003, "issue.assignee.bad", "Bad assignee", Level.SEVERE);

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

    public String getFormated(Object... args){
        return MessageSeeds.getFormated(this, args);
    }

    public static String getFormated(MessageSeed messageSeed, Object... args){
        return MessageFormat.format(messageSeed.getDefaultFormat(), args);
    }
}
