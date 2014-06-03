package com.elster.jupiter.issue.share.entity;

public interface ActionParameter extends Entity {

    String getKey();

    void setKey(String key);

    String getValue();

    void setValue(String value);

    CreationRuleAction getAction();
}
