/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.issue.datavalidation.impl.actions.CloseIssueAction;
import com.energyict.mdc.issue.datavalidation.impl.event.DataValidationEventHandlerFactory;


public enum TranslationKeys implements TranslationKey {

    DATA_VALIDATION_ISSUE_TYPE("DataValidationIssueType", "Data validation"),
    DATA_VALIDATION_ISSUE_REASON("DataValidationIssueReason", "Cannot estimate data"),
    DATA_VALIDATION_ISSUE_REASON_DESCRIPTION("DataValidationIssueReasonDescription", "Cannot estimate data on {0}"),
    DATA_VALIDATION_ISSUE_RULE_TEMPLATE_NAME("DataValidationIssueRuleTemplateName", "Create issue when suspects cannot be estimated"),
    DATA_VALIDATION_ISSUE_RULE_TEMPLATE_DESCRIPTION("DataValidationIssueRuleTemplateDescription", "Create issue when suspects cannot be estimated"),
    DEVICE_CONFIGURATIONS_PROPERTY(DataValidationIssueCreationRuleTemplate.DEVICE_CONFIGURATIONS, "Device configurations"),
    AQ_SUBSCRIBER(DataValidationEventHandlerFactory.AQ_DATA_VALIDATION_EVENT_SUBSCRIBER, "Create data validation issues"),
    ACTION_RETRY_ESTIMATION("ActionRetryEstimation", "Retry estimation"),
    ACTION_RETRY_ESTIMATION_SUCCESS("ActionRetryEstimationSuccess", "Estimation retry succeeded"),
    ACTION_RETRY_ESTIMATION_FAIL("ActionRetryEstimationFailed", "Estimation retry failed"),
    CLOSE_ACTION_PROPERTY_CLOSE_STATUS(CloseIssueAction.CLOSE_STATUS, "Close status"),
    CLOSE_ACTION_PROPERTY_COMMENT(CloseIssueAction.COMMENT, "Comment"),
    CLOSE_ACTION_WRONG_STATUS("action.wrong.status", "You are trying to apply the incorrect status"),
    CLOSE_ACTION_ISSUE_CLOSED("action.issue.closed", "Issue closed"),
    CLOSE_ACTION_ISSUE_ALREADY_CLOSED("action.issue.already.closed", "Issue already closed"),
    CLOSE_ACTION_CLOSE_ISSUE("issue.action.closeIssue", "Close issue"),
    DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES("deviceLifecycleInDeviceStates", "Device lifecycle state in device type ")
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
