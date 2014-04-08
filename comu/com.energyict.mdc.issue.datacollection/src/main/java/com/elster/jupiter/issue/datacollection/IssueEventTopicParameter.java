package com.elster.jupiter.issue.datacollection;

import com.elster.jupiter.issue.share.cep.CreationRuleTemplateParameter;

/**
 * This class is an example of implementation for CreationRuleTemplate Parameter interface.
 * Can be located in OSGI private packages.
 */
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
        return "eventTopic";
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public String getSuffix() {
        return "(issue)";
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
