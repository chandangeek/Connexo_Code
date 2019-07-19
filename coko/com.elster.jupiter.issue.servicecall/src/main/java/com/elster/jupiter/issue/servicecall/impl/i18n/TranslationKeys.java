/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl.i18n;

import com.elster.jupiter.issue.servicecall.impl.ServiceCallIssueCreationRuleTemplate;
import com.elster.jupiter.issue.servicecall.impl.action.CloseIssueAction;
import com.elster.jupiter.issue.servicecall.impl.event.ServiceCallMessageHandlerFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    START_PROCESS_ACTION("issue.action.startProcess", "Start process"),
    START_PROCESS_ACTION_PROCESS("issue.action.process", "Process"),
    START_PROCESS_ACTION_SELECT_PROCESS("issue.action.select_process", "Select process"),
    SERVICE_CALL_ISSUE_TYPE("servicecall.issue.Type", "Service call"),
    SERVICE_CALL_ISSUE_FAILED_REASON("servicecall.issue.FailedReason", "Service call failed"),
    SERVICE_CALL_ISSUE_FAILED_REASON_DESCRIPTION("servicecall.issue.FailedReasonDescription", "Service call failed"),
    SERVICE_CALL_ISSUE_PARTIAL_SUCCEED_REASON("servicecall.issue.PartialSucceedReason", "Service call partially succeeded"),
    SERVICE_CALL_ISSUE_PARTIAL_SUCCEED_REASON_DESCRIPTION("servicecall.issue.PartialSucceedReasonDescription", "Service call partially succeeded"),
    SERVICE_CALL_TYPE("servicecall.issue.type.name", "Service call type name"),
    SERVICE_CALL_TYPE_STATE("servicecall.issue.type.state", "Service call state"),
    PARAMETER_AUTO_RESOLUTION(ServiceCallIssueCreationRuleTemplate.AUTORESOLUTION, "Auto resolution"),
    AQ_SUBSCRIBER(ServiceCallMessageHandlerFactory.AQ_SERVICE_CALL_EVENT_SUBSCRIBER, "Create service call issues"),

    CLOSE_ACTION_PROPERTY_CLOSE_STATUS(CloseIssueAction.CLOSE_STATUS, "Close status"),
    CLOSE_ACTION_PROPERTY_COMMENT(CloseIssueAction.COMMENT, "Comment"),
    CLOSE_ACTION_WRONG_STATUS("action.wrong.status", "You are trying to apply the incorrect status"),
    CLOSE_ACTION_ISSUE_CLOSED("action.issue.closed", "Issue closed"),
    CLOSE_ACTION_ISSUE_ALREADY_CLOSED("action.issue.already.closed", "Issue already closed"),
    CLOSE_ACTION_CLOSE_ISSUE("issue.action.closeIssue", "Close issue"),

    ACTION_RETRY_NOW("ActionRetryNow", "Retry now"),

    SERVICE_CALL_ISSUE_RULE_TEMPLATE_NAME("servicecall.issue.RuleTemplateName", "State change"),
    SERVICE_CALL_ISSUE_RULE_TEMPLATE_DESCRIPTION("servicecall.issue.TemplateDescription", "State change");

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public String getTranslated(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }
}
