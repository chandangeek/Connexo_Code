package com.elster.jupiter.issue.share.entity;

import java.util.List;

public interface CreationRuleAction extends Entity {

    CreationRuleActionPhase getPhase();

    void setPhase(CreationRuleActionPhase phase);

    CreationRule getRule();

    void setRule(CreationRule rule);

    IssueActionType getType();

    void setType(IssueActionType type);

    List<ActionParameter> getParameters();

    void addParameter(String key, String value);
}
