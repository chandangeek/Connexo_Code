/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@ProviderType
public interface ValidationRuleSet extends IdentifiedObject {

    long getId();

    QualityCodeSystem getQualityCodeSystem();

    void setMRID(String mRID);

    void setName(String name);

    void setAliasName(String aliasName);

    void setDescription(String description);

    long getVersion();

    void save();

    void delete();

    List<? extends ValidationRule> getRules();

    List<? extends ValidationRuleSetVersion> getRuleSetVersions();

    List<? extends ValidationRuleSetVersion> getRuleSetVersions(int start, int limit);

    ValidationRuleSetVersion addRuleSetVersion(String description, Instant startDate);

    ValidationRuleSetVersion updateRuleSetVersion(long id, String description, Instant startDate);

    ValidationRuleSetVersion cloneRuleSetVersion(long ruleSetVersionId, String description, Instant startDate);

    void deleteRuleSetVersion(ValidationRuleSetVersion version);

    List<ValidationRule> getRules(Collection<? extends ReadingType> readingTypes);

    Instant getObsoleteDate();
}

