package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    FIELD_CAN_NOT_BE_EMPTY(1, Constants.FIELD_CAN_NOT_BE_EMPTY, "This field is required", Level.SEVERE),
    ASSIGN_USER_EXCEPTION(2, Constants.ASSIGN_USER_EXCEPTION, "Only members of \"Administrators\" role can perform this action.", Level.SEVERE),
    NO_BPM_CONNECTION(3, Constants.NO_BPM_CONNECTION, "Connection to Flow failed.", Level.SEVERE),
    PROCESS_NOT_AVAILABLE(4, Constants.PROCESS_NOT_AVAILABLE, "Process {0} not available.", Level.SEVERE);

    public static final String COMPONENT_NAME = "BPM";

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
        return COMPONENT_NAME;
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

    public enum Constants {
        ;
        public static final String NO_BPM_CONNECTION= "NoBpmConnection";
        public static final String ASSIGN_USER_EXCEPTION= "BPM.AssignUserException";
        public static final String FIELD_CAN_NOT_BE_EMPTY= "BPM.FieldCanNotBeEmpty";
        public static final String PROCESS_NOT_AVAILABLE= "BPM.ProcessNotAvailable";

    }

}

