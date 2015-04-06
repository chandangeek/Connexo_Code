package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;
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

    List<? extends ValidationRuleSetVersion> getRuleSetVersions();

    ValidationRuleSetVersion addRuleSetVersion(String name, String description, Instant startDate);

    ValidationRuleSetVersion updateRuleSetVersion(long id, String name, String description, Instant startDate);

    void deleteRuleSetVersion(ValidationRuleSetVersion version);

    List<ValidationRule> getRules(Iterable<? extends ReadingType> readingTypes);

    public Instant getObsoleteDate();
}

