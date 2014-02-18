package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.IdentifiedObject;

import java.util.List;

public interface ValidationRuleSet extends IdentifiedObject {

    long getId();

    void setMRID(String mRID);

    void setName(String name);

    void setAliasName(String aliasName);

    void setDescription(String description);

    long getVersion();

    void save();

    void delete();

    List<? extends ValidationRule> getRules();

    List<? extends ValidationRule> getRules(int start, int limit);

    ValidationRule addRule(ValidationAction action, String implementation, String name);

    void deleteRule(ValidationRule rule);
}

