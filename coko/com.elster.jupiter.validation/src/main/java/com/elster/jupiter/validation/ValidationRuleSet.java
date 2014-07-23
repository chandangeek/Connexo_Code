package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.metering.ReadingType;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

    ValidationRule updateRule(long id, String name, String implementation, boolean active, List<String> mRIDs, Map<String, Object> properties);

    void deleteRule(ValidationRule rule);

    List<ValidationRule> getRules(Iterable<? extends ReadingType> readingTypes);

    public Date getObsoleteDate();
}

