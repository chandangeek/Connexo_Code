/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationRule;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

/**
 * Provides support for managing usage point's specific validation configuration
 * (for example, overriding properties of validation rules configured for a usage point).
 */
@ProviderType
public interface UsagePointValidation {

    /**
     * Finds all properties that are overridden on all validation rules and all usage point's channels
     *
     * @return a List of {@link ChannelValidationRuleOverriddenProperties}
     */
    List<? extends ChannelValidationRuleOverriddenProperties> findAllOverriddenProperties();

    /**
     * Finds properties that are overridden on a specified {@link ValidationRule} and channel's {@link ReadingType}
     *
     * @param validationRule target {@link ValidationRule}
     * @param readingType target {@link ReadingType} of usage point's channel
     * @return {@link ChannelValidationRuleOverriddenProperties} or Optional.empty() if no properties are overridden for specified validation rule and reading type
     */
    Optional<? extends ChannelValidationRuleOverriddenProperties> findOverriddenProperties(ValidationRule validationRule, ReadingType readingType);

    /**
     * Finds {@link ChannelValidationRuleOverriddenProperties} entity by id.
     *
     * @return {@link ChannelValidationRuleOverriddenProperties} or Optional.empty() if no entity with such id
     */
    Optional<? extends ChannelValidationRuleOverriddenProperties> findChannelValidationRuleOverriddenProperties(long id);

    /**
     * Finds and locks {@link ChannelValidationRuleOverriddenProperties} entity by id and version.
     *
     * @return {@link ChannelValidationRuleOverriddenProperties} or Optional.empty() if no entity with such id and version
     */
    Optional<? extends ChannelValidationRuleOverriddenProperties> findAndLockChannelValidationRuleOverriddenProperties(long id, long version);

    /**
     * Starts the process of overriding {@link ValidationRule}'s properties on {@link ValidationPropertyDefinitionLevel#TARGET_OBJECT},
     * which is a usage point's channel in this context identified by {@link ReadingType}
     *
     * @param validationRule target {@link ValidationRule} for which the properties are going to be redefined
     * @param readingType target {@link ReadingType} of usage point's channel
     * @return {@link PropertyOverrider}
     */
    PropertyOverrider overridePropertiesFor(ValidationRule validationRule, ReadingType readingType);

    /**
     * Builder for {@link ChannelValidationRuleOverriddenProperties}
     */
    @ProviderType
    interface PropertyOverrider {

        PropertyOverrider override(String propertyName, Object propertyValue);

        ChannelValidationRuleOverriddenProperties complete();
    }
}
