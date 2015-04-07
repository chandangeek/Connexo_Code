package com.elster.jupiter.validation;

import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface ValidationRuleSetVersion {

    long getId();

    String getName();

    String getDescription();

    void setName(String name);

    void setDescription(String description);

    Instant getStartDate();

    void setStartDate(Instant startDate);

    void save();

    void delete();

    public Instant getObsoleteDate();

    ValidationRuleSet getRuleSet();

    List<? extends ValidationRule> getRules();

    List<? extends ValidationRule> getRules(int start, int limit);

    ValidationRule addRule(ValidationAction action, String implementation, String name);

    ValidationRule updateRule(long id, String name, boolean active, ValidationAction action,  List<String> mRIDs, Map<String, Object> properties);

    void deleteRule(ValidationRule rule);

    List<ValidationRule> getRules(Iterable<? extends ReadingType> readingTypes);

    boolean isObsolete();

    long getVersion();

}
