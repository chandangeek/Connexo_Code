/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.validation.ValidationService;

import java.util.Map;


/**
 * This estimator will replace suspect values for a channel with time-corresponding readings
 * of the channel with the same reading type of the check channel on the configured purpose of the same usage point.
 */
public class MainCheckEstimator extends AbstractMainCheckEstimator {

    MainCheckEstimator(Thesaurus thesaurus, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService, PropertySpecService propertySpecService) {
        super(thesaurus, metrologyConfigurationService, validationService, propertySpecService);
    }

    MainCheckEstimator(Thesaurus thesaurus, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, metrologyConfigurationService, validationService, propertySpecService, properties);
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.ESTIMATOR_NAME.getDefaultFormat();
    }

    public enum TranslationKeys implements TranslationKey {
        ESTIMATOR_NAME(MainCheckEstimator.class.getName(), "Main/Check substitution");

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
