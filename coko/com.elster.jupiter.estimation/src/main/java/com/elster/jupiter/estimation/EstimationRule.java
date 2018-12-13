/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingQualityComment;
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

    List<PropertySpec> getPropertySpecs(EstimationPropertyDefinitionLevel level);

    boolean isObsolete();

    boolean isMarkProjected();

    /**
     * Creates new {@link Estimator} instance which is configured in this rule.
     * Created estimator is initialized only using the properties from the {@link EstimationPropertyDefinitionLevel#ESTIMATION_RULE} level,
     * so the properties that could be overridden on other {@link EstimationPropertyDefinitionLevel}s will not be passed into estimator.
     *
     * Deprecated since new method is introduced {@link com.elster.jupiter.estimation.impl.IEstimationRule#createNewEstimator(ChannelsContainer, ReadingType)}.
     *
     * @return {@link Estimator}
     */
    @Deprecated
    Estimator createNewEstimator();

    Optional<ReadingQualityComment> getComment();

    void setComment(ReadingQualityComment comment);
}
