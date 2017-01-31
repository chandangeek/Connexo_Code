/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.collections.KPermutation;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EstimationRuleSet extends IdentifiedObject {

    long getId();

    void setMRID(String mRID);

    void setName(String name);

    void setAliasName(String aliasName);

    void setDescription(String description);

    long getVersion();

    void save();

    void delete();

    List<? extends EstimationRule> getRules();

    void reorderRules(KPermutation permutation);

    List<? extends EstimationRule> getRules(int start, int limit);

    EstimationRuleBuilder addRule(String implementation, String name);

    EstimationRule updateRule(long id, String name, boolean active, List<String> mRIDs, Map<String, Object> properties);

    void deleteRule(EstimationRule rule);

    List<? extends EstimationRule> getRules(Set<? extends ReadingType> readingTypes);

    Instant getObsoleteDate();

    QualityCodeSystem getQualityCodeSystem();
}
