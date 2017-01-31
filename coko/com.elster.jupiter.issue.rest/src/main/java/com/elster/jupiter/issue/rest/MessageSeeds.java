/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    ISSUE_DOES_NOT_EXIST (0001, "IssueDoesNotExist", "Issue doesn't exist", Level.SEVERE),
    ISSUE_ALREADY_CLOSED(0002, "IssueAlreadyClosed", "Issue already closed", Level.SEVERE),
    ISSUE_ACTION_CLASS_LOAD_FAIL(0003, "IssueActionClassLoadFail", "Unable to load Action class \"{0}\" for \"{1}\" action type", Level.SEVERE),
    INVALID_VALUE(0004, "InvalidValue", "Invalid value", Level.SEVERE),
    ACTION_ISSUE_WAS_UNASSIGNED(1000, "action.issue.was.unassigned", "Issue was unassigned" , Level.INFO),
    ACTION_ISSUE_WAS_ASSIGNED_USER(1001, "action.issue.was.assigned.user", "Issue was assigned to user {0}" , Level.INFO),
    ACTION_ISSUE_WAS_ASSIGNED_WORKGROUP(1002, "action.issue.was.assigned.workgorup", "Issue was assigned to workgroup {0}" , Level.INFO),
    ACTION_ISSUE_WAS_ASSIGNED_USER_AND_WORKGROUP(1003, "action.issue.was.assigned.user.workgorup", "Issue was assigned to user {0} and workgroup {1}" , Level.INFO),
    ACTION_ISSUE_PRIORITY_WAS_CHANGED(1004, "action.issue.priority.was.changed", "Issue priority has been changed" , Level.INFO),
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
        return "ISR";
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
