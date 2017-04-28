/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeValueFactory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointValueFactory;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
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
import java.util.Optional;

/**
 * This rule is identical to the {@link MainCheckEstimator} rule but instead of using the CHECK output on the same usage point
 * it will use data from another usage point with flexibility in output matching.
 */
public class ReferenceSubstitutionEstimator extends AbstractMainCheckEstimator {

    static final String CHECK_USAGE_POINT = TranslationKeys.CHECK_USAGE_POINT.getKey();
    static final String CHECK_READING_TYPE = TranslationKeys.CHECK_READING_TYPE.getKey();

    private MeteringService meteringService;

    private boolean initFailed;

    private UsagePointValueFactory.UsagePointReference referenceUsagePoint;
    private ReadingTypeValueFactory.ReadingTypeReference referenceReadingTypeProperty;

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
        referenceReadingTypeProperty = getReferenceReadingTypeProperty();
    }

    UsagePointValueFactory.UsagePointReference getCheckUsagePointProperty() {
        return (UsagePointValueFactory.UsagePointReference) getProperty(CHECK_USAGE_POINT);
    }

    ReadingTypeValueFactory.ReadingTypeReference getReferenceReadingTypeProperty() {
        return (ReadingTypeValueFactory.ReadingTypeReference) getProperty(CHECK_READING_TYPE);
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks, QualityCodeSystem system) {
        // override validatingUsagePoint
        validatingUsagePoint = referenceUsagePoint.getUsagePoint();
        return initFailed ? SimpleEstimationResult.of(estimationBlocks, Collections.emptyList()) : super.estimate(estimationBlocks, system);
    }

    @Override
    public void validateProperties(Map<String, Object> properties) {
        MetrologyPurpose checkPurpose = Optional.ofNullable(properties.get(CHECK_PURPOSE))
                .map(o -> (MetrologyPurpose) o)
                .orElse(null);
        ReadingTypeValueFactory.ReadingTypeReference checkReadingTypeRef = Optional.ofNullable(properties.get(CHECK_READING_TYPE))
                .map(o -> (ReadingTypeValueFactory.ReadingTypeReference) o)
                .orElse(null);
        UsagePoint checkUsagePoint = Optional.ofNullable(properties.get(CHECK_USAGE_POINT))
                .map(o -> (UsagePoint) o)
                .orElse(null);
        validateCheckUsagePointHasCheckPurpose(checkUsagePoint, checkPurpose, checkReadingTypeRef);
    }

    private void validateCheckUsagePointHasCheckPurpose(UsagePoint checkUsagePoint, MetrologyPurpose checkPurpose, ReadingTypeValueFactory.ReadingTypeReference checkReadingTypeRef) {
        if (checkUsagePoint != null && checkPurpose != null && checkReadingTypeRef != null) {
            Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMC = checkUsagePoint
                    .getCurrentEffectiveMetrologyConfiguration();
            MetrologyContract contract = null;
            if (effectiveMC.isPresent()) {
                Optional<MetrologyContract> metrologyContract = effectiveMC.get().getMetrologyConfiguration()
                        .getContracts()
                        .stream()
                        .filter(c -> c.getMetrologyPurpose().equals(checkPurpose))
                        .findAny();
                if (metrologyContract.isPresent()) {
                    contract = metrologyContract.get();
                }
            }
            if (contract == null) {
                throw new LocalizedFieldValidationException(MessageSeeds.REFERENCE_VALIDATE_PROPS_NO_PURPOSE_ON_USAGE_POINT, "properties." + CHECK_USAGE_POINT);
            }else {
                Channel channel = null;
                Optional<ChannelsContainer> channelsContainerWithCheckChannel = effectiveMC.get()
                        .getChannelsContainer(contract);
                if (channelsContainerWithCheckChannel.isPresent()) {
                    Optional<Channel> checkChannel = channelsContainerWithCheckChannel.get().getChannel(checkReadingTypeRef.getReadingType());
                    if (checkChannel.isPresent()) {
                        channel = checkChannel.get();
                    }
                }
                if (channel == null){
                    throw new LocalizedFieldValidationException(MessageSeeds.REFERENCE_VALIDATE_PROPS_NO_READING_TYPE_ON_PURPOSE_ON_USAGE_POINT, "properties." + CHECK_READING_TYPE);
                }
            }
        }
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

    @Override
    String getMessage(ReferenceReadingQuality referenceReadingQuality, EstimationBlock estimationBlock) {
        return "";
    }

    private PropertySpec buildReferenceUsagePointPropertySpec() {
        return getPropertySpecService()
                .specForValuesOf(new UsagePointValueFactory(meteringService))
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
