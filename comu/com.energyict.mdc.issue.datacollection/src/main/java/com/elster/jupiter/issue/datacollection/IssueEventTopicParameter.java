package com.elster.jupiter.issue.datacollection;

import com.elster.jupiter.issue.share.cep.CreationRuleTemplateParameter;

public class IssueEventTopicParameter implements CreationRuleTemplateParameter {
    @Override
    public String getName() {
        return "eventTopic";
    }

    @Override
    public String getType() {
        return "number";
    }

    @Override
    public String getLabel() {
        return "Event topic";
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public String getSuffix() {
        return "";
    }

    @Override
    public int getMin() {
        return 10;
    }

    @Override
    public int getMax() {
        return 100;
    }
}
