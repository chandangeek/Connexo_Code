/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.EstimationBlock;
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

    @Override
    TranslationKey getEstimatorNameKey() {
        return TranslationKeys.ESTIMATOR_NAME;
    }

    @Override
    String getMessage(ReferenceReadingQuality referenceReadingQuality, EstimationBlock estimationBlock) {
        String message;
        switch (referenceReadingQuality){
            case NO_MC:
                message = getThesaurus().getFormat(MessageSeeds.MAINCHECK_ESTIMATOR_FAIL_EFFECTIVE_MC_NOT_FOUND)
                        .format(blockToString(estimationBlock), getThesaurus().getFormat(TranslationKeys.ESTIMATOR_NAME)
                                .format(), estimationBlock.getReadingType()
                                .getFullAliasName(), validatingUsagePoint.getName());
                break;
            case NO_PURPOSE_ON_UP:
                message = getThesaurus().getFormat(MessageSeeds.MAINCHECK_ESTIMATOR_FAIL_PURPOSE_DOES_NOT_EXIST_ON_UP)
                        .format(blockToString(estimationBlock), getThesaurus().getFormat(TranslationKeys.ESTIMATOR_NAME)
                                .format(), estimationBlock.getReadingType()
                                .getFullAliasName(), validatingUsagePoint.getName());
                break;
            case NO_CHECK_CHANNEL:
                message = getThesaurus().getFormat(MessageSeeds.MAINCHECK_ESTIMATOR_FAIL_NO_OUTPUTS_ON_PURPOSE_WITH_READING_TYPE)
                        .format(blockToString(estimationBlock), getThesaurus().getFormat(TranslationKeys.ESTIMATOR_NAME)
                                .format(), estimationBlock.getReadingType()
                                .getFullAliasName(), validatingUsagePoint.getName());
                break;
            case REFERENCE_DATA_MISSING:
            case REFERENCE_DATA_SUSPECT:
                message = getThesaurus().getFormat(MessageSeeds.MAINCHECK_ESTIMATOR_FAIL_DATA_SUSPECT_OR_MISSING)
                        .format(blockToString(estimationBlock), getThesaurus().getFormat(TranslationKeys.ESTIMATOR_NAME)
                                .format(), validatingUsagePoint.getName(), checkPurpose.getName(), estimationBlock.getReadingType()
                                .getFullAliasName());
                break;
                default:
                    message = getThesaurus().getFormat(MessageSeeds.ESTIMATOR_FAIL_INTERNAL_ERROR)
                            .format(blockToString(estimationBlock), getThesaurus().getFormat(MainCheckEstimator.TranslationKeys.ESTIMATOR_NAME)
                                    .format(), validatingUsagePoint.getName(), checkPurpose.getName(), estimationBlock.getReadingType()
                                    .getFullAliasName());
        }
        return message;
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
