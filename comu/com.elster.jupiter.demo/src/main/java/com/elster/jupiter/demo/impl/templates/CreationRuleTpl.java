/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.IssueRuleBuilder;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;

public enum CreationRuleTpl implements Template<CreationRule, IssueRuleBuilder> {

    CONNECTION_LOST("Connection failed", "CONNECTION_LOST", "reason.connection.failed"),
    COMMUNICATION_FAILED("Connection setup failed", "UNABLE_TO_CONNECT", "reason.connection.setup.failed"),
    CONNECTION_SETUP_LOST("Device communication failed", "DEVICE_COMMUNICATION_FAILURE", "reason.failed.to.communicate"),
    CANNOT_ESTIMATE_SUSPTECTS("Create issue when suspects can't be estimated","CANNOT_ESTIMATE_DATA", "reason.cant.estimate.data", DueInType.WEEK, IssueRuleBuilder.BASIC_DATA_VALIDATION_RULE_TEMPLATE, Priority.DEFAULT, true),
    DEVICE_ALARM("Device Alarm", "END_DEVICE_EVENT_CREATED", "alarm.reason", DueInType.WEEK, IssueRuleBuilder.BASIC_DEVICE_ALARM_RULE_TEMPLATE, Priority.DEFAULT, true)
    ;

    private static final String BASIC_DATA_COLLECTION_RULE_TEMPLATE = "BasicDataCollectionRuleTemplate";

    private String name;
    private String type;
    private String reason;
    private DueInType dueInType;
    private String ruleTemplateName;
    private Priority priority;
    private boolean status;

    CreationRuleTpl(String name, String type, String reason) {
        this(name, type, reason, DueInType.WEEK, BASIC_DATA_COLLECTION_RULE_TEMPLATE, Priority.DEFAULT, true);
    }

    CreationRuleTpl(String name, String type, String reason, DueInType dueIntype, String ruleTemplateName, Priority priority, boolean status) {
        this.name = name;
        this.type = type;
        this.reason = reason;
        this.dueInType = dueIntype;
        this.ruleTemplateName = ruleTemplateName;
        this.priority = priority;
        this.status = status;
    }

    @Override
    public Class<IssueRuleBuilder> getBuilderClass() {
        return IssueRuleBuilder.class;
    }

    @Override
    public IssueRuleBuilder get(IssueRuleBuilder builder) {
        return builder.withName(this.name).
                withType(this.type).
                withReason(this.reason).
                withDueInType(this.dueInType).
                withRuleTemplate(this.ruleTemplateName).
                withPriority(this.priority).
                withStatus(this.status);
    }

    public String getName() {
        return this.name;
    }
}
