/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.validation.ValidationService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This rule is identical to the {@link MainCheckEstimator} rule but instead of using the CHECK output on the same usage point
 * it will use data from another usage point with flexibility in output matching.
 */
public class ReferenceSubstitutionEstimator extends AbstractMainCheckEstimator {

    ReferenceSubstitutionEstimator(Thesaurus thesaurus, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService, PropertySpecService propertySpecService) {
        super(thesaurus, metrologyConfigurationService, validationService, propertySpecService);
    }

    ReferenceSubstitutionEstimator(Thesaurus thesaurus, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, metrologyConfigurationService, validationService, propertySpecService, properties);
    }

    @Override
    public void validateProperties(Map<String, Object> properties) {
        // TODO
        /*
        The chosen check usage point doesn't have the selected purpose
         */

        /*
        The chosen reading type is not available on the selected purpose of the usage point
         */
    }

    @Override
    public List<PropertySpec> getPropertySpecs(EstimationPropertyDefinitionLevel level) {
        return EstimationPropertyDefinitionLevel.ESTIMATION_RULE == level ? Collections.emptyList() : getPropertySpecs();
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.ESTIMATOR_NAME.getDefaultFormat();
    }

    public enum TranslationKeys implements TranslationKey {
        ESTIMATOR_NAME(ReferenceSubstitutionEstimator.class.getName(), "Reference substitution"),

        CHECK_PURPOSE("maincheck.purpose", "Check purpose"),
        CHECK_PURPOSE_DESCRIPTION("maincheck.purpose.description", "Check purpose");

        private final String key;
        private final String defaultFormat;

        TranslationKeys(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getDefaultFormat() {
            return this.defaultFormat;
        }

    }
}
