/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimators.MissingRequiredProperty;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeValueFactory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.validation.ValidationService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This rule is identical to the {@link MainCheckEstimator} rule but instead of using the CHECK output on the same usage point
 * it will use data from another usage point with flexibility in output matching.
 */
public class ReferenceSubstitutionEstimator extends AbstractMainCheckEstimator {

    static final String CHECK_USAGE_POINT = TranslationKeys.CHECK_USAGE_POINT.getKey();
    static final String CHECK_READING_TYPE = TranslationKeys.CHECK_READING_TYPE.getKey();

    private MeteringService meteringService;

    private UsagePoint referenceUsagePoint;
    private ReadingType referenceReadinType;

    ReferenceSubstitutionEstimator(Thesaurus thesaurus, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService, PropertySpecService propertySpecService, MeteringService meteringService) {
        super(thesaurus, metrologyConfigurationService, validationService, propertySpecService);
        this.meteringService = meteringService;
    }

    ReferenceSubstitutionEstimator(Thesaurus thesaurus, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService, PropertySpecService propertySpecService, MeteringService meteringService, Map<String, Object> properties) {
        super(thesaurus, metrologyConfigurationService, validationService, propertySpecService, properties);
        this.meteringService = meteringService;
    }

    @Override
    protected void init() {
        super.init();

        referenceUsagePoint = getCheckUsagePointProperty();
        referenceReadinType = getReferenceReadingTypeProperty().getReadingType();
    }

    UsagePoint getCheckUsagePointProperty() {
        UsagePoint value = (UsagePoint) getProperty(CHECK_USAGE_POINT);
        if (value == null) {
            throw new MissingRequiredProperty(getThesaurus(), CHECK_USAGE_POINT);
        }
        return value;
    }

    ReadingTypeValueFactory.ReadingTypeReference getReferenceReadingTypeProperty() {
        return (ReadingTypeValueFactory.ReadingTypeReference) getProperty(CHECK_READING_TYPE);
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
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                buildCheckPurposePropertySpec(),
                buildReferenceUsagePointPropertySpec(),
                buildReferenceReadingTypePropertySpec()
        );
    }

    private PropertySpec buildReferenceUsagePointPropertySpec() {
        return getPropertySpecService()
                .referenceSpec(UsagePoint.class)
                .named(TranslationKeys.CHECK_USAGE_POINT)
                .describedAs(TranslationKeys.CHECK_USAGE_POINT_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
    }

    private PropertySpec buildReferenceReadingTypePropertySpec() {
        return getPropertySpecService()
                .specForValuesOf(new ReadingTypeValueFactory(meteringService, ReadingTypeValueFactory.Mode.ONLY_REGULAR))
                .named(TranslationKeys.CHECK_READING_TYPE)
                .describedAs(TranslationKeys.CHECK_READING_TYPE_DESCRIPTION)
                .fromThesaurus(getThesaurus())
                .markRequired()
                .finish();
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.ESTIMATOR_NAME.getDefaultFormat();
    }

    public enum TranslationKeys implements TranslationKey {
        ESTIMATOR_NAME(ReferenceSubstitutionEstimator.class.getName(), "Reference substitution"),

        CHECK_USAGE_POINT("reference.check.usagepoint", "Check usage point"),
        CHECK_USAGE_POINT_DESCRIPTION("reference.check.usagepoint.description", "Check usage point"),
        CHECK_READING_TYPE("reference.check.readingtype", "Check reading type"),
        CHECK_READING_TYPE_DESCRIPTION("reference.check.readingtype.description", "Check reading type");

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
