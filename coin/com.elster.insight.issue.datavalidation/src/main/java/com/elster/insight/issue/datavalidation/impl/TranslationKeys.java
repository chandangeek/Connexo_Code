/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.insight.issue.datavalidation.impl.actions.CloseUsagePointIssueAction;
import com.elster.insight.issue.datavalidation.impl.event.DataValidationEventHandlerFactory;


public enum TranslationKeys implements TranslationKey {

    DATA_VALIDATION_ISSUE_TYPE("UsagePointDataValidationIssueType", "Data validation"),
    DATA_VALIDATION_ISSUE_REASON("UsagePointDataValidationIssueReason", "Cannot estimate usage point data"),
    DATA_VALIDATION_ISSUE_REASON_DESCRIPTION("UsagePointDataValidationIssueReasonDescription", "Cannot estimate usage point data on {0}"),
    DATA_VALIDATION_ISSUE_RULE_TEMPLATE_NAME("UsagePointDataValidationIssueRuleTemplateName", "Create usage point issue when suspects cannot be estimated"),
    DATA_VALIDATION_ISSUE_RULE_TEMPLATE_DESCRIPTION("UsagePointDataValidationIssueTemplateDescription", "Create usage point issue when suspects cannot be estimated"),
    METROLOGY_CONFIGURATIONS_PROPERTY(UsagePointDataValidationIssueCreationRuleTemplate.METROLOGY_CONFIGS, "Metrology configurations"),
    AQ_SUBSCRIBER(DataValidationEventHandlerFactory.AQ_DATA_VALIDATION_EVENT_SUBSCRIBER, "Create data validation issues"),
    ACTION_RETRY_ESTIMATION("UsagePointActionRetryEstimation", "Retry estimation"),
    ACTION_RETRY_ESTIMATION_SUCCESS("UsagePointActionRetryEstimationSuccess", "Estimation retry succeeded"),
    ACTION_RETRY_ESTIMATION_FAIL("UsagePointActionRetryEstimationFailed", "Estimation retry failed"),
    CLOSE_ACTION_PROPERTY_CLOSE_STATUS(CloseUsagePointIssueAction.CLOSE_STATUS, "Close status"),
    CLOSE_ACTION_PROPERTY_COMMENT(CloseUsagePointIssueAction.COMMENT, "Comment"),
    CLOSE_ACTION_WRONG_STATUS("usage.point.issue.action.wrong.status", "You are trying to apply the incorrect status"),
    CLOSE_ACTION_ISSUE_CLOSED("action.usage.point.issue.closed", "Usage point issue closed"),
    CLOSE_ACTION_ISSUE_ALREADY_CLOSED("action.usage.point.issue.already.closed", "Usage point issue already closed"),
    CLOSE_ACTION_CLOSE_ISSUE("usage.point.issue.action.closeIssue", "Close usage point issue"),
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
        return thesaurus.getFormat(this).format();
    }
}
