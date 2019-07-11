/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;
import com.elster.jupiter.webservice.issue.impl.actions.CloseIssueAction;
import com.elster.jupiter.webservice.issue.impl.actions.StartProcessWebServiceIssueAction;
import com.elster.jupiter.webservice.issue.impl.event.WebServiceEventHandlerFactory;
import com.elster.jupiter.webservice.issue.impl.template.AuthFailureIssueCreationRuleTemplate;
import com.elster.jupiter.webservice.issue.impl.template.BadAcknowledgementIssueCreationRuleTemplate;
import com.elster.jupiter.webservice.issue.impl.template.EndpointUnavailableIssueCreationRuleTemplate;
import com.elster.jupiter.webservice.issue.impl.template.NoAcknowledgementIssueCreationRuleTemplate;

public enum TranslationKeys implements TranslationKey {

    WEB_SERVICE_ISSUE_TYPE("WebServiceIssueType", "Web service"),
    WEB_SERVICE_ISSUE_REASON(WebServiceIssueService.WEB_SERVICE_ISSUE_REASON + ".key", "Network issues"),
    WEB_SERVICE_ISSUE_REASON_DESCRIPTION(WebServiceIssueService.WEB_SERVICE_ISSUE_REASON + ".description", "Network issues"),
    AUTH_FAILURE_ISSUE_RULE_TEMPLATE_NAME(AuthFailureIssueCreationRuleTemplate.NAME, "Authentication failure"),
    AUTH_FAILURE_ISSUE_RULE_TEMPLATE_DESCRIPTION(AuthFailureIssueCreationRuleTemplate.NAME + ".description", "Create issue when authentication fails"),
    ENDPOINT_UNAVAILABLE_ISSUE_RULE_TEMPLATE_NAME(EndpointUnavailableIssueCreationRuleTemplate.NAME, "Endpoint unavailable"),
    ENDPOINT_UNAVAILABLE_ISSUE_RULE_TEMPLATE_DESCRIPTION(EndpointUnavailableIssueCreationRuleTemplate.NAME + ".description", "Create issue when trying to send request to an unavailable endpoint"),
    NO_ACKNOWLEDGEMENT_ISSUE_RULE_TEMPLATE_NAME(NoAcknowledgementIssueCreationRuleTemplate.NAME, "No acknowledgement"),
    NO_ACKNOWLEDGEMENT_ISSUE_RULE_TEMPLATE_DESCRIPTION(NoAcknowledgementIssueCreationRuleTemplate.NAME + ".description", "Create issue when endpoint doesn''t respond on the request"),
    BAD_ACKNOWLEDGEMENT_ISSUE_RULE_TEMPLATE_NAME(BadAcknowledgementIssueCreationRuleTemplate.NAME, "Bad acknowledgement"),
    BAD_ACKNOWLEDGEMENT_ISSUE_RULE_TEMPLATE_DESCRIPTION(BadAcknowledgementIssueCreationRuleTemplate.NAME + ".description", "Create issue when endpoint responds with an error"),
    END_POINT_CONFIGURATIONS_PROPERTY(AuthFailureIssueCreationRuleTemplate.END_POINT_CONFIGURATIONS, "Web services"),
    SUBSCRIBER(WebServiceEventHandlerFactory.WEB_SERVICE_EVENT_SUBSCRIBER, "Create web service issues"),
    ACTION_START_PROCESS("ActionStartProcess", "Start process"),
    ACTION_START_PROCESS_PROPERTY_PROCESS(StartProcessWebServiceIssueAction.START_PROCESS, "Process"),
    ACTION_CLOSE_ISSUE("issue.action.closeIssue", "Close issue"),
    ACTION_CLOSE_PROPERTY_CLOSE_STATUS(CloseIssueAction.CLOSE_STATUS, "Close status"),
    ACTION_CLOSE_PROPERTY_COMMENT(CloseIssueAction.COMMENT, "Comment");

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

    public String translate(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }
}
