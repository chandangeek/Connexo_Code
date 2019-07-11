/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl.i18n;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    START_PROCESS_ACTION("issue.action.startProcess", "Start process"),
    START_PROCESS_ACTION_PROCESS("issue.action.process", "Process"),
    START_PROCESS_ACTION_SELECT_PROCESS("issue.action.select_process", "Select process"),
    SERVICE_CALL_ISSUE_TYPE("ServiceCallIssueType", "Service call"),
    SERVICE_CALL_ISSUE_FAILED_REASON("ServiceCallIssueFailedReason", "Service call failed"),
    SERVICE_CALL_ISSUE_FAILED_REASON_DESCRIPTION("ServiceCallIssueFailedReasonDescription", "Service call failed"),
    SERVICE_CALL_ISSUE_PARTIAL_SUCCEED_REASON("ServiceCallIssuePartialSucceedReason", "Service call partially succeeded"),
    SERVICE_CALL_ISSUE_PARTIAL_SUCCEED_REASON_DESCRIPTION("ServiceCallIssuePartialSucceedReasonDescription", "Service call partially succeeded"),
    SERVICE_CALL_TYPE_HANDLER("ServiceCallTypeHandler", "Service call handler name"),
    SERVICE_CALL_TYPE_STATE("ServiceCallTypeState", "Service call state"),

    ACTION_RETRY_NOW("ActionRetryNow", "Retry now"),

    SERVICE_CALL_ISSUE_RULE_TEMPLATE_NAME("ServiceCallIssueRuleTemplateName", "State change"),
    SERVICE_CALL_ISSUE_RULE_TEMPLATE_DESCRIPTION("ServiceCallIssueRuleTemplateDescription", "State change");

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
