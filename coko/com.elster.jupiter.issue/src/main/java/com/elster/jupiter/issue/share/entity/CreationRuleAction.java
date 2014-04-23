package com.elster.jupiter.issue.share.entity;

import java.util.List;

public interface CreationRuleAction extends Entity {

    CreationRuleActionPhase getPhase();

    void setPhase(CreationRuleActionPhase phase);

    CreationRule getRule();

    void setRule(CreationRule rule);

    CreationRuleActionType getType();

    void setType(CreationRuleActionType type);

    List<CreationRuleActionParameter> getParameters();

    void addParameter(String key, String value);
}
