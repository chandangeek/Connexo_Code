package com.elster.jupiter.issue.share.entity;

public interface CreationRuleActionParameter extends Entity {

    String getKey();

    void setKey(String key);

    String getValue();

    void setValue(String value);

    CreationRuleAction getAction();

    void setAction(CreationRuleAction action);
}
