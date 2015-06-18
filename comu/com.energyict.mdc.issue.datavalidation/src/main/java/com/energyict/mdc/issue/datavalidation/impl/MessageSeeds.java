package com.energyict.mdc.issue.datavalidation.impl;

import java.text.MessageFormat;
import java.util.logging.Level;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.impl.event.DataValidationEventHandlerFactory;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    DATA_VALIDATION_ISSUE_TYPE(1, "DataValidationIssueType", "Data Validation", Level.INFO),
    DATA_VALIDATION_ISSUE_REASON(2, "DataValidationIssueReason", "Can''t estimate data on", Level.INFO),
    DATA_VALIDATION_ISSUE_RULE_TEMPLATE_NAME(3, "DataValidationIssueRuleTemplateName", "Create issue when suspects can''t be estimated", Level.INFO),
    DATA_VALIDATION_ISSUE_RULE_TEMPLATE_DESCRIPTION(4, "DataValidationIssueRuleTemplateDescription", "Create issue when suspects can''t be estimated", Level.INFO),
    DEVICECONFIG_PROPERTY(5, DataValidationIssueCreationRuleTemplate.DEVICE_CONFIGURATIONS, "Device configurations", Level.INFO),
    
    AQ_SUBSCRIBER(1001, DataValidationEventHandlerFactory.AQ_DATA_VALIDATION_EVENT_SUBSCRIBER, "Create data validation issues", Level.INFO),
    
    UNABLE_TO_CREATE_EVENT(2001, "DataValidationIssueUnableToMapEvent", "Unable to create event from event payload: {0}", Level.INFO),
    
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
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
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public String getModule() {
        return IssueDataValidationService.COMPONENT_NAME;
    }
    
    public String getTranslated(Thesaurus thesaurus, Object... args){
        if (thesaurus == null) {
            throw new IllegalArgumentException("Thesaurus can't be null");
        }
        String translated = thesaurus.getString(this.getKey(), this.getDefaultFormat());
        return MessageFormat.format(translated, args);
    }

}