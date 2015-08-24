package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.issue.datavalidation.impl.event.DataValidationEventHandlerFactory;

public enum TranslationKeys implements TranslationKey {

    DATA_VALIDATION_ISSUE_TYPE("DataValidationIssueType", "Data Validation"),
    DATA_VALIDATION_ISSUE_REASON("DataValidationIssueReason", "Can't estimate data"),
    DATA_VALIDATION_ISSUE_REASON_DESCRIPTION("DataValidationIssueReasonDescription", "Can''t estimate data on {0}"),
    DATA_VALIDATION_ISSUE_RULE_TEMPLATE_NAME("DataValidationIssueRuleTemplateName", "Create issue when suspects can't be estimated"),
    DATA_VALIDATION_ISSUE_RULE_TEMPLATE_DESCRIPTION("DataValidationIssueRuleTemplateDescription", "Create issue when suspects can't be estimated"),
    DEVICE_CONFIGURATIONS_PROPERTY(DataValidationIssueCreationRuleTemplate.DEVICE_CONFIGURATIONS, "Device configurations"),
    AQ_SUBSCRIBER(DataValidationEventHandlerFactory.AQ_DATA_VALIDATION_EVENT_SUBSCRIBER, "Create data validation issues")
    ;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public String getTranslated(Thesaurus thesaurus) {
        return thesaurus.getString(this.getKey(), this.getDefaultFormat());
    }
}
