/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.validation.properties.ValidationPropertyDefinitionLevel;

import aQute.bnd.annotation.ConsumerType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ConsumerType
public interface Validator extends HasDynamicProperties {

    Optional<QualityCodeIndex> getReadingQualityCodeIndex();

    void init(Channel channel, ReadingType readingType, Range<Instant> interval);

    /**
     * Marks the end of validating the interval specified through init(). Implementing validators return a Map that is either empty, or that contains mappings of Date
     * to ValidationResult, in case these could only be established at the end of the interval.
     */
    Map<Instant, ValidationResult> finish();

    ValidationResult validate(IntervalReadingRecord intervalReadingRecord);

    ValidationResult validate(ReadingRecord readingRecord);

    String getDisplayName();

    String getDisplayName(String property);

    String getDefaultFormat();

    /**
     * Returns the set of target quality code systems supported by this validator.
     *
     * @return the set of target applications supported by this validator.
     * @see ValidationService#getAvailableValidators(QualityCodeSystem)
     */
    Set<QualityCodeSystem> getSupportedQualityCodeSystems();

    /**
     * Returns the list of {@link PropertySpec}s for which the values can be set on the specified {@link ValidationPropertyDefinitionLevel}.
     * <p>Default implementation assumes that the values for all the {@link PropertySpec}s returned by {@link Validator#getPropertySpecs()}
     * can be set only on {@link ValidationPropertyDefinitionLevel#VALIDATION_RULE}
     *
     * @return The List of PropertySpec
     */
    default List<PropertySpec> getPropertySpecs(ValidationPropertyDefinitionLevel level) {
        return ValidationPropertyDefinitionLevel.VALIDATION_RULE == level ? getPropertySpecs() : Collections.emptyList();
    }

    /**
     * Validates values of validator's properties according to business constraints.
     * Note: the method should not try to validate presence of required properties, because this will be done by validation engine.
     *
     * @param properties the values to validate
     */
    default void validateProperties(Map<String, Object> properties) {
        // nothing to do by default
    }
}
