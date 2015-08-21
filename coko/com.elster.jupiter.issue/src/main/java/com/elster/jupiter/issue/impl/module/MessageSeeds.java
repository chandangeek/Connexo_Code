package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.issue.impl.actions.AssignIssueAction;
import com.elster.jupiter.issue.impl.actions.CommentIssueAction;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed, TranslationKey {
// 0001 - 1000 General validation messages
    FIELD_CAN_NOT_BE_EMPTY (1, Keys.FIELD_CAN_NOT_BE_EMPTY, "This field is required", Level.SEVERE),

    FIELD_SIZE_BETWEEN_1_AND_80(2, Keys.FIELD_SIZE_BETWEEN_1_AND_80, "Field's text length should be between 1 and 80 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_128(3, Keys.FIELD_SIZE_BETWEEN_1_AND_128, "Field's text length should be between 1 and 128 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_200(4, Keys.FIELD_SIZE_BETWEEN_1_AND_200, "Field's text length should be between 1 and 200 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_256(5, Keys.FIELD_SIZE_BETWEEN_1_AND_256, "Field's text length should be between 1 and 256 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_400(6, Keys.FIELD_SIZE_BETWEEN_1_AND_400, "Field's text length should be between 1 and 400 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_1024(7, Keys.FIELD_SIZE_BETWEEN_1_AND_1024, "Field's text length should be between 1 and 1024 symbols", Level.SEVERE),

// Domain record specific
    ISSUE_COMMENT_COMMENT_SIZE(1001, Keys.ISSUE_COMMENT_COMMENT_SIZE, "Comment length should be more than 1 symbol", Level.SEVERE),
    ACTION_TYPE_DESCRIPTION_SIZE(1002, Keys.ACTION_TYPE_DESCRIPTION_SIZE, "Description length should be less than 256 symbol", Level.SEVERE),

// Status translations
    ISSUE_STATUS_OPEN(2001, "issue.status.open", "Open", Level.INFO),
    ISSUE_STATUS_RESOLVED(2002, "issue.status.resolved", "Resolved", Level.INFO),
    ISSUE_STATUS_WONT_FIX(2003, "issue.status.wont.fix", "Won't fix", Level.INFO),
    ISSUE_STATUS_IN_PROGRESS(2004, "issue.status.in.progress", "In progress", Level.INFO),

// 4001 - 4999 Issue actions
    ACTION_ASSIGN_ISSUE(4002, "issue.action.assignIssue", "Assign issue", Level.INFO),
    ACTION_COMMENT_ISSUE(4003, "issue.action.commentIssue", "Comment issue", Level.INFO),

    ACTION_INCORRECT_PARAMETERS(4501, "action.incorrect.parameters", "Incorrect parameters for action" , Level.INFO),
    ACTION_WRONG_COMMENT(4504, "action.issue.comment", "Please provide a correct comment" , Level.INFO),
    ACTION_WRONG_ASSIGNEE(4505, "action.issue.wrong.assignee", "Wrong assignee" , Level.INFO),
    ACTION_ISSUE_WAS_ASSIGNED(4506, "action.issue.was.assigned", "Issue was assigned to {0}" , Level.INFO),
    ACTION_ISSUE_WAS_COMMENTED(4508, "action.issue.was.commented", "Issue was commented" , Level.INFO),

// 5001 - 5999 Issue action properties

    ASSIGNACTION_PROPERTY_ASSIGNEE(5001, AssignIssueAction.ASSIGNEE, "Assignee", Level.INFO),
    ASSIGNEACTION_PROPERTY_COMMENT(5002, AssignIssueAction.COMMENT, "Comment", Level.INFO),
    COMMENTACTION_PROPERTY_COMMENT(5005, CommentIssueAction.ISSUE_COMMENT, "Comment", Level.INFO),

// 9001 - ... All messages
    ISSUE_DROOLS_VALIDATION(9001, "issue.drools.validation", "{0}", Level.SEVERE),
    ISSUE_OVERDUE_NOTIFICATION(9002, "issue.overdue.notification", "Issue \"{0}\" is overdue", Level.INFO),
    ISSUE_ASSIGN_RULE_GET_ASSIGNEE(9003, "issue.assign.rul.get.assignee", "Failed to parse assignee id in rule \"{0}\", possible error in rule.", Level.SEVERE),
    ISSUE_ACTION_FAIL(9004, "issue.action.fail", "Exception occurs during an action \"{0}\", for issue \"{1}\"", Level.SEVERE),
    ISSUE_ACTION_CLASS_LOAD_FAIL(9005, "issue.action.class.load.fail", "Unable to load Action class \"{0}\" for \"{1}\" action type", Level.SEVERE),
    ISSUE_CREATION_RULE_VALIDATION_FAILED(9006, "issue.creation.rule.validation", "Creation rule validation failed", Level.SEVERE),
    ISSUE_CREATION_RULE_PARAMETER_ABSENT(9007, "issue.creation.parameter.absent", "Required parameter is absent", Level.SEVERE),
    ISSUE_CREATION_RULE_INVALID_SRTING_PARAMETER(9008, "issue.creation.invalid.string.parameter", "String length should be between %s and %s characters", Level.SEVERE),
    ISSUE_CREATION_RULE_INCORRECT_NUMBER_PARAMETER(9009, "issue.creation.incorrect.number.parameter", "Number should be between %s and %s", Level.SEVERE),
    ISSUE_CREATION_RULE_INVALID_NUMBER_PARAMETER(9010, "issue.creation.invalid.number.parameter", "%s is not a number", Level.SEVERE),
    NOT_UNIQUE_KEY(9011, "issue.not.unique.key", "The key '{0}' is already in use", Level.SEVERE),
    CREATION_RULE_UNIQUE_NAME(9012, Keys.CREATION_RULE_UNIQUE_NAME, "The name should be unique", Level.SEVERE),
    PROPERTY_NOT_IN_PROPERTYSPECS(9013, Keys.PROPERTY_NOT_IN_PROPERTYSPECS, "Property is not in property specs", Level.SEVERE),
    PROPERTY_MISSING(9014, Keys.PROPERTY_MISSING, "This field is required", Level.SEVERE),
    PROPERTY_NOT_POSSIBLE_VALUE(9015, Keys.PROPERTY_NOT_POSSIBLE_VALUE, "The value is not listed as a possible value for this property", Level.SEVERE)
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

    public String getTranslated(Thesaurus thesaurus, Object... args){
        if (thesaurus == null) {
            throw new IllegalArgumentException("Thesaurus can't be null");
        }
        String translated = thesaurus.getString(this.getKey(), this.getDefaultFormat());
        return MessageFormat.format(translated, args);
    }

    public static class Keys {
        private Keys() {}
        
        public static final String FIELD_CAN_NOT_BE_EMPTY       = "FieldCanNotBeEmpty";
        public static final String FIELD_SIZE_BETWEEN_1_AND_80  = "FieldSizeBetween1and80";
        public static final String FIELD_SIZE_BETWEEN_1_AND_128 = "FieldSizeBetween1and128";
        public static final String FIELD_SIZE_BETWEEN_1_AND_200 = "FieldSizeBetween1and200";
        public static final String FIELD_SIZE_BETWEEN_1_AND_256 = "FieldSizeBetween1and256";
        public static final String FIELD_SIZE_BETWEEN_1_AND_400 = "FieldSizeBetween1and400";
        public static final String FIELD_SIZE_BETWEEN_1_AND_1024= "FieldSizeBetween1and1024";

        public static final String ISSUE_COMMENT_COMMENT_SIZE   = "IssueCommentCommentSize";
        public static final String ACTION_TYPE_DESCRIPTION_SIZE = "ActionTypeDescriptionSize";
        public static final String CREATION_RULE_UNIQUE_NAME    = "CreationRuleUniqueName";
        public static final String PROPERTY_NOT_IN_PROPERTYSPECS= "PropertyNotInPropertySpecs";
        public static final String PROPERTY_MISSING             = "PropertyMissing";
        public static final String PROPERTY_NOT_POSSIBLE_VALUE = "XisNotAPossibleValue";
    }
}
