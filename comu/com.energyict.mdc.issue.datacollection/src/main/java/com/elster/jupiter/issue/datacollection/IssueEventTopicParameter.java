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
        return "string";
    }

    @Override
    public String getLabel() {
        return "isu.issue.i18n.rulename.eventTopic";
    }

    @Override
    public boolean isOptional() {
        return false;
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
