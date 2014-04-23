package com.elster.jupiter.issue.share.entity;

public interface CreationRuleParameter extends Entity {

    String getKey();

    void setKey(String key);

    String getValue();

    void setValue(String value);

    CreationRule getRule();

    void setRule(CreationRule rule);
}
