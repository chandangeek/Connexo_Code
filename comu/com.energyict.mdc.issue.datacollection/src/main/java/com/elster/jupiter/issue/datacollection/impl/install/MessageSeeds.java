package com.elster.jupiter.issue.datacollection.impl.install;

import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    ISSUE_CREATION_RULE_PARAMETER_ABSENT(1, "issue.creation.parameter.absent", "Required parameter is absent", Level.SEVERE);

    private final int number;
    private final String key;
    private final String format;
    private final Level level;

    private MessageSeeds(int number, String key, String format, Level level) {
        this.number = number;
        this.key = key;
        this.format = format;
        this.level = level;
    }

    @Override
    public String getModule() {
        return ModuleConstants.COMPONENT_NAME;
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


}

