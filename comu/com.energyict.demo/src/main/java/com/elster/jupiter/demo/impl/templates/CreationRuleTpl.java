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
    CANNOT_ESTIMATE_SUSPTECTS("Create issue when suspects can't be estimated","CANNOT_ESTIMATE_DATA", "reason.cant.estimate.data", DueInType.DAY, 0L, IssueRuleBuilder.BASIC_DATA_VALIDATION_RULE_TEMPLATE, Priority.DEFAULT, true),
    CANNOT_ESTIMATE_USAGEPOINTDATA("Create issue when usage point data suspects can't be estimated","CANNOT_ESTIMATE_USAGEPOINT_DATA", "reason.cant.estimate.usagepoint.data", DueInType.DAY, 0L, IssueRuleBuilder.USAGE_POINT_DATA_VALIDATION_RULE_TEMPLATE, Priority.DEFAULT, true),
    DEVICE_ALARM("Tampering", "END_DEVICE_EVENT_CREATED", "Tampering", DueInType.DAY, 0L, IssueRuleBuilder.BASIC_DEVICE_ALARM_RULE_TEMPLATE, Priority.get(30,30), true),
    DEVICELIFECYCLE_FAILED_TRANSITION("Device lifecycle failed transition", "TRANSITION_FAILURE", "Tampering", DueInType.DAY, 0L, IssueRuleBuilder.BASIC_DEVICE_ALARM_RULE_TEMPLATE, Priority.get(30,30), true)
    ;

    private static final String BASIC_DATA_COLLECTION_RULE_TEMPLATE = "BasicDataCollectionRuleTemplate";

    private String name;
    private String type;
    private String reason;
    private DueInType dueInType;
    private long dueInValue;
    private String ruleTemplateName;
    private Priority priority;
    private boolean active;

    CreationRuleTpl(String name, String type, String reason) {
        this(name, type, reason, DueInType.DAY, 0L, BASIC_DATA_COLLECTION_RULE_TEMPLATE, Priority.get(30,20), true);
    }

    CreationRuleTpl(String name, String type, String reason, DueInType dueIntype, long dueInValue, String ruleTemplateName, Priority priority, boolean active) {
        this.name = name;
        this.type = type;
        this.reason = reason;
        this.dueInType = dueIntype;
        this.dueInValue = dueInValue;
        this.ruleTemplateName = ruleTemplateName;
        this.priority = priority;
        this.active = active;
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
                withDueInValue(this.dueInValue).
                withRuleTemplate(this.ruleTemplateName).
                withPriority(this.priority).
                withStatus(this.active);
    }

    public String getName() {
        return this.name;
    }
}
