package com.elster.jupiter.issue.rest.i18n;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.logging.Level;


public enum MessageSeeds implements MessageSeed {

    ISSUE_ASSIGNEE_ME (0001, "IssueAssigneeMe", "Me", Level.SEVERE),
    ISSUE_ASSIGNEE_UNASSIGNED (0002, "IssueAssigneeUnassigned", "Unassigned", Level.SEVERE),
    ISSUE_DOES_NOT_EXIST (0003, "IssueDoesNotExist", "Issue doesn't exist", Level.SEVERE),
    ISSUE_WAS_ALREADY_CHANGED (0004, "IssueWasAlreadyChanged", "Issue has been already changed", Level.SEVERE),
    ISSUE_ACTION_CLASS_LOAD_FAIL(0005, "IssueActionClassLoadFail", "Unable to load Action class \"{0}\" for \"{1}\" action type", Level.SEVERE),
    ISSUE_ACTION_PHASE_CREATE(0006, "IssueActionPhaseCreation", "Issue creation", Level.INFO),
    ISSUE_ACTION_PHASE_OVERDUE(0007, "IssueActionPhaseOverdue", "Issue overdue", Level.INFO)
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

    public String getFormated(Object... args){
        return MessageFormat.format(this.getDefaultFormat(), args);
    }

    public static String getString(MessageSeed messageSeed, Thesaurus thesaurus, Object... args){
        String text = thesaurus.getString(messageSeed.getKey(), messageSeed.getDefaultFormat());
        return MessageFormat.format(text, args);
    }


    public static MessageSeeds getByKey(String key) {
        if (key != null) {
            for (MessageSeeds column : MessageSeeds.values()) {
                if (column.getKey().equals(key)) {
                    return column;
                }
            }
        }
        return null;
    }
}
