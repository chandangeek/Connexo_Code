/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    FIELD_CAN_NOT_BE_EMPTY(1, Constants.FIELD_CAN_NOT_BE_EMPTY, "This field is required", Level.SEVERE),
    ASSIGN_USER_EXCEPTION(2, Constants.ASSIGN_USER_EXCEPTION, "Only members of \"Administrators\" role can perform this action.", Level.SEVERE),
    NO_BPM_CONNECTION(3, Constants.NO_BPM_CONNECTION, "Connection to Flow failed.", Level.SEVERE),
    PROCESS_NOT_AVAILABLE(4, Constants.PROCESS_NOT_AVAILABLE, "Process {0} not available.", Level.SEVERE),
    EDIT_TASK_CONCURRENT_TITLE(5, Constants.EDIT_TASK_CONCURRENT_TITLE, "Failed to save task ''{0}''", Level.SEVERE ),
    EDIT_TASK_CONCURRENT_BODY(6, Constants.EDIT_TASK_CONCURRENT_BODY, "''{0}'' has changed since the page was last updated.", Level.SEVERE ),
    EDIT_PROCESS_CONCURRENT_TITLE(7, Constants.EDIT_PROCESS_CONCURRENT_TITLE, "Failed to save process ''{0}''", Level.SEVERE ),
    EDIT_PROCESS_CONCURRENT_BODY(8, Constants.EDIT_PROCESS_CONCURRENT_BODY, "''{0}'' has changed since the page was last updated.", Level.SEVERE ),
    START_PROCESS_CONCURRENT_TITLE(9, Constants.START_PROCESS_CONCURRENT_TITLE, "Failed to start process ''{0}''", Level.SEVERE ),
    START_PROCESS_CONCURRENT_BODY(10, Constants.START_PROCESS_CONCURRENT_BODY, "''{0}'' process has changed since the page was last updated.", Level.SEVERE ),
    NO_TASK_WITH_ID(11, Constants.NO_TASK_WITH_ID, "No task with ID ''{0}''", Level.SEVERE ),
    START_TASK_CONCURRENT_TITLE(12, Constants.START_TASK_CONCURRENT_TITLE, "Failed to start task ''{0}''", Level.SEVERE ),
    START_TASK_CONCURRENT_BODY(13, Constants.START_TASK_CONCURRENT_BODY, "''{0}'' status has changed since the page was last updated.", Level.SEVERE ),
    COMPLETE_TASK_CONCURRENT_TITLE(14, Constants.COMPLETE_TASK_CONCURRENT_TITLE, "Failed to complete task ''{0}''", Level.SEVERE ),
    COMPLETE_TASK_CONCURRENT_BODY(15, Constants.COMPLETE_TASK_CONCURRENT_BODY, "''{0}'' status has changed since the page was last updated.", Level.SEVERE ),
    SAVE_TASK_CONCURRENT_TITLE(14, Constants.SAVE_TASK_CONCURRENT_TITLE, "Failed to save task ''{0}''", Level.SEVERE ),
    SAVE_TASK_CONCURRENT_BODY(15, Constants.SAVE_TASK_CONCURRENT_BODY, "''{0}'' status has changed since the page was last updated.", Level.SEVERE )
    ;

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
        public static final String EDIT_TASK_CONCURRENT_TITLE = "BPM.EditTaskConcurrentTitle";
        public static final String EDIT_PROCESS_CONCURRENT_TITLE = "BPM.EditProcessConcurrentTitle";
        public static final String START_TASK_CONCURRENT_TITLE = "BPM.StartTaskConcurrentTitle";
        public static final String COMPLETE_TASK_CONCURRENT_TITLE = "BPM.CompleteTaskConcurrentTitle";
        public static final String SAVE_TASK_CONCURRENT_TITLE = "BPM.SaveTaskConcurrentTitle";
        public static final String START_PROCESS_CONCURRENT_TITLE = "BPM.StartProcessConcurrentTitle";
        public static final String EDIT_TASK_CONCURRENT_BODY = "BPM.EditTaskConcurrentBody";
        public static final String START_TASK_CONCURRENT_BODY = "BPM.StartTaskConcurrentBody";
        public static final String COMPLETE_TASK_CONCURRENT_BODY = "BPM.CompleteTaskConcurrentBody";
        public static final String SAVE_TASK_CONCURRENT_BODY = "BPM.SaveTaskConcurrentBody";
        public static final String EDIT_PROCESS_CONCURRENT_BODY = "BPM.EditProcessConcurrentBody";
        public static final String START_PROCESS_CONCURRENT_BODY = "BPM.StartProcessConcurrentBody";
        public static final String NO_TASK_WITH_ID = "BPM.NoTaskWithId";
    }

}

