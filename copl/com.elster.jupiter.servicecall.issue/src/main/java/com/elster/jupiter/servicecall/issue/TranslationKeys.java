/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    SERVICE_CALL_ISSUE_TYPE("ServiceCallIssueType", "Service Call"),
    SERVICE_CALL_ISSUE_FAILED_REASON("ServiceCallIssueFailedReason", "Service call failed"),
    SERVICE_CALL_ISSUE_FAILED_REASON_DESCRIPTION("ServiceCallIssueFailedReasonDescription", "Service call failed on {0}"),
    SERVICE_CALL_ISSUE_PARTIAL_SUCCEED_REASON("ServiceCallIssuePartialSucceedReason", "Service call partial succeed"),
    SERVICE_CALL_ISSUE_PARTIAL_SUCCEED_REASON_DESCRIPTION("ServiceCallIssuePartialSucceedReasonDescription", "Service call partial succeed description"),
    SERVICE_CALL_TYPE_HANDLER("ServiceCallTypeHandler", "Service call handler name"),
    SERVICE_CALL_TYPE_HANDLER_DESCRIPTION("ServiceCallTypeHandlerDescription", "Service call handler description"),

    SERVICE_CALL_ISSUE_RULE_TEMPLATE_NAME("ServiceCallIssueRuleTemplateName", "State change"),
    SERVICE_CALL_ISSUE_RULE_TEMPLATE_DESCRIPTION("ServiceCallIssueRuleTemplateDescription", "State change"),
    AQ_SUBSCRIBER("ServiceCallIssuesAq", "Create data servicecall issues"),
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
