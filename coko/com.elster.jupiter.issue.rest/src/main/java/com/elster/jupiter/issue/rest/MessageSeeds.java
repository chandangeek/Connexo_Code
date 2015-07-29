package com.elster.jupiter.issue.rest;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.logging.Level;


public enum MessageSeeds implements MessageSeed, TranslationKey {

    ISSUE_ASSIGNEE_ME (1, "IssueAssigneeMe", "Me", Level.SEVERE),
    ISSUE_ASSIGNEE_UNASSIGNED (2, "IssueAssigneeUnassigned", "Unassigned", Level.SEVERE),
    ISSUE_DOES_NOT_EXIST (3, "IssueDoesNotExist", "Issue doesn't exist", Level.SEVERE),
    ISSUE_ALREADY_CLOSED(4, "IssueAlreadyClosed", "Issue already closed", Level.SEVERE),
    ISSUE_ACTION_CLASS_LOAD_FAIL(5, "IssueActionClassLoadFail", "Unable to load Action class \"{0}\" for \"{1}\" action type", Level.SEVERE),
    ISSUE_ACTION_PHASE_CREATE(6, "IssueActionPhaseCreation", "Issue creation", Level.INFO),
    ISSUE_ACTION_PHASE_OVERDUE(7, "IssueActionPhaseOverdue", "Issue overdue", Level.INFO),
    ISSUE_ACTION_PHASE_CREATE_DESCRIPTION(8, "IssueActionPhaseCreationDescription", "The action will be performed at the issue creation time", Level.INFO),
    ISSUE_ACTION_PHASE_OVERDUE_DESCRIPTION(9, "IssueActionPhaseOverdueDescription", "The action will be performed when the issue becomes overdue", Level.INFO)
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

    public String getTranslated(Thesaurus thesaurus, Object... args){
        String text = thesaurus.getString(getKey(), getDefaultFormat());
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
