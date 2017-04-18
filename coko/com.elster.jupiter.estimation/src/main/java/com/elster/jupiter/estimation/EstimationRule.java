/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.aggregation.ReadingQualityComment;
import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ProviderType
public interface EstimationRule {

    long getId();

    boolean isActive();

    String getImplementation();

    String getDisplayName();

    void activate();

    void deactivate();

    EstimationRuleSet getRuleSet();

    List<EstimationRuleProperties> getProperties();

    EstimationRuleProperties addProperty(String name, Object value);

    Map<String, Object> getProps();

    Set<ReadingType> getReadingTypes();

    ReadingTypeInEstimationRule addReadingType(ReadingType readingType);

    ReadingTypeInEstimationRule addReadingType(String mRID);

    void deleteReadingType(ReadingType readingType);

    boolean isRequired(String propertyKey);

    String getName();

    Instant getObsoleteDate();

    long getVersion();

    List<PropertySpec> getPropertySpecs();

    boolean isObsolete();

    Estimator createNewEstimator();

    Optional<ReadingQualityComment> getComment();

    void setComment(ReadingQualityComment comment);
}
