/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@ProviderType
public interface ValidationRuleSetVersion {

    long getId();

    String getDescription();

    void setDescription(String description);

    Instant getStartDate();

    Instant getEndDate();

    void setStartDate(Instant startDate);
    void setEndDate(Instant endDate);

    void save();

    void delete();

    public Instant getObsoleteDate();

    ValidationRuleSet getRuleSet();

    List<? extends ValidationRule> getRules();

    List<? extends ValidationRule> getRules(int start, int limit);

    ValidationRuleBuilder addRule(ValidationAction action, String implementation, String name);

    ValidationRule updateRule(long id, String name, boolean active, ValidationAction action,  List<String> mRIDs, Map<String, Object> properties);

    void deleteRule(ValidationRule rule);

    List<ValidationRule> getRules(Collection<? extends ReadingType> readingTypes);

    boolean isObsolete();

    long getVersion();

    ValidationVersionStatus getStatus();

}
