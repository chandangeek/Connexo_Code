package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.IssueRuleBuilder;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;

public enum CreationRuleTpl implements Template<CreationRule, IssueRuleBuilder> {

    CONNECTION_LOST("Connection failed", "CONNECTION_LOST", "reason.connection.failed"),
    COMMUNICATION_FAILED("Connection setup failed", "UNABLE_TO_CONNECT", "reason.connection.setup.failed"),
    CONNECTION_SETUP_LOST("Device communication failed", "DEVICE_COMMUNICATION_FAILURE", "reason.failed.to.communicate"),
    CANNOT_ESTIMATE_SUSPTECTS("Create issue when suspects can't be estimated","CANNOT_ESTIMATE_DATA", "reason.cant.estimate.data", DueInType.WEEK, IssueRuleBuilder.BASIC_DATA_VALIDATION_RULE_TEMPLATE ),
    DEVICE_ALARM("Device Alarm", "END_DEVICE_EVENT_CREATED", "alarm.reason", DueInType.WEEK, IssueRuleBuilder.BASIC_DEVICE_ALARM_RULE_TEMPLATE)
    ;

    private static final String BASIC_DATA_COLLECTION_RULE_TEMPLATE = "BasicDataCollectionRuleTemplate";

    private String name;
    private String type;
    private String reason;
    private DueInType dueInType;
    private String ruleTemplateName;

    CreationRuleTpl(String name, String type, String reason) {
        this(name, type, reason, DueInType.WEEK, BASIC_DATA_COLLECTION_RULE_TEMPLATE);
    }

    CreationRuleTpl(String name, String type, String reason, DueInType dueIntype, String ruleTemplateName) {
        this.name = name;
        this.type = type;
        this.reason = reason;
        this.dueInType = dueIntype;
        this.ruleTemplateName = ruleTemplateName;
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
                withRuleTemplate(this.ruleTemplateName);
    }

    public String getName() {
        return this.name;
    }
}
