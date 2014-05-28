package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
// 0001 - 1000 General validation messages
    FIELD_CAN_NOT_BE_EMPTY (0001, Keys.FIELD_CAN_NOT_BE_EMPTY, "Field can't be empty", Level.SEVERE),

    FIELD_SIZE_BETWEEN_1_AND_80(0002, Keys.FIELD_SIZE_BETWEEN_1_AND_80, "Field's text length should be between 1 and 80 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_128(0003, Keys.FIELD_SIZE_BETWEEN_1_AND_128, "Field's text length should be between 1 and 128 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_200(0004, Keys.FIELD_SIZE_BETWEEN_1_AND_200, "Field's text length should be between 1 and 200 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_256(0005, Keys.FIELD_SIZE_BETWEEN_1_AND_256, "Field's text length should be between 1 and 256 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_400(0006, Keys.FIELD_SIZE_BETWEEN_1_AND_400, "Field's text length should be between 1 and 400 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_1024(0007, Keys.FIELD_SIZE_BETWEEN_1_AND_1024, "Field's text length should be between 1 and 1024 symbols", Level.SEVERE),

// Domain record specific
    ISSUE_COMMENT_COMMENT_SIZE(1001, Keys.ISSUE_COMMENT_COMMENT_SIZE, "Comment length should be more than 1 symbol", Level.SEVERE),
    ACTION_TYPE_DESCRIPTION_SIZE(1002, Keys.ACTION_TYPE_DESCRIPTION_SIZE, "Description length should be less than 256 symbol", Level.SEVERE),

// 9001 - ... All messages
    ISSUE_DROOLS_VALIDATION(9001, "issue.drools.validation", "{0}", Level.SEVERE),
    ISSUE_OVERDUE_NOTIFICATION(9002, "issue.overdue.notification", "Issue \"{0}\" is overdue", Level.INFO),
    ISSUE_ASSIGN_RULE_GET_ASSIGNEE(9003, "issue.assign.rul.get.assignee", "Failed to parse assignee id in rule \"{0}\", possible error in rule.", Level.SEVERE),
    ISSUE_ACTION_FAIL(9004, "issue.action.fail", "Exception occurs during an action \"{0}\", for issue \"{1}\"", Level.SEVERE),
    ISSUE_ACTION_CLASS_LOAD_FAIL(9005, "issue.action.class.load.fail", "Unable to load Action class \"{0}\" for \"{1}\" action type", Level.SEVERE),
    ISSUE_CREATION_RULE_VALIDATION_FAILED(9006, "issue.creation.rule.validation", "Creation rule validation failed", Level.SEVERE),
    ISSUE_CREATION_RULE_PARAMETER_ABSENT(9007, "issue.creation.parameter.absent", "Required parameter is absent", Level.SEVERE),
    ISSUE_CREATION_RULE_INVALID_SRTING_PARAMETER(9008, "issue.creation.invalid.string.parameter", "String length should be between %s and %s simbols", Level.SEVERE);

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
        if (key.startsWith(getModule() + ".")){
            // +1 to skip the dot symbol
            return key.substring(getModule().length() + 1);
        }
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

    public void log(Logger logger, Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args));
    }

    public void log(Logger logger, Thesaurus thesaurus, Throwable t, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args), t);
    }

    public static class Keys {
        private Keys() {}
        private static final String KEY_PREFIX = IssueService.COMPONENT_NAME + ".";

        public static final String FIELD_CAN_NOT_BE_EMPTY       = KEY_PREFIX + "FieldCanNotBeEmpty";
        public static final String FIELD_SIZE_BETWEEN_1_AND_80  = KEY_PREFIX + "FieldSizeBetween1and80";
        public static final String FIELD_SIZE_BETWEEN_1_AND_128 = KEY_PREFIX + "FieldSizeBetween1and128";
        public static final String FIELD_SIZE_BETWEEN_1_AND_200 = KEY_PREFIX + "FieldSizeBetween1and200";
        public static final String FIELD_SIZE_BETWEEN_1_AND_256 = KEY_PREFIX + "FieldSizeBetween1and256";
        public static final String FIELD_SIZE_BETWEEN_1_AND_400 = KEY_PREFIX + "FieldSizeBetween1and400";
        public static final String FIELD_SIZE_BETWEEN_1_AND_1024= KEY_PREFIX + "FieldSizeBetween1and1024";

        public static final String ISSUE_COMMENT_COMMENT_SIZE   = KEY_PREFIX + "IssueCommentCommentSize";
        public static final String ACTION_TYPE_DESCRIPTION_SIZE   = KEY_PREFIX + "ActionTypeDescriptionSize";
    }
}
