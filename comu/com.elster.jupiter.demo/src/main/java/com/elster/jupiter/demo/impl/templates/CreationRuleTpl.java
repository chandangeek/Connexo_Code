package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.IssueRuleBuilder;
import com.elster.jupiter.issue.share.entity.CreationRule;

public enum CreationRuleTpl implements Template<CreationRule, IssueRuleBuilder> {
    CONNECTION_LOST("Connection failed", "CONNECTION_LOST", "reason.connection.failed"),
    COMMUNICATION_FAILED("Connection setup failed", "UNABLE_TO_CONNECT", "reason.connection.setup.failed"),
    CONNECTION_SETUP_LOST("Device communication failed", "DEVICE_COMMUNICATION_FAILURE", "reason.failed.to.communicate"),
    ;
    private String name;
    private String type;
    private String reason;

    CreationRuleTpl(String name, String type, String reason) {
        this.name = name;
        this.type = type;
        this.reason = reason;
    }

    @Override
    public Class<IssueRuleBuilder> getBuilderClass() {
        return IssueRuleBuilder.class;
    }

    @Override
    public IssueRuleBuilder get(IssueRuleBuilder builder) {
        return builder.withName(this.name).withType(this.type).withReason(this.reason);
    }
}
