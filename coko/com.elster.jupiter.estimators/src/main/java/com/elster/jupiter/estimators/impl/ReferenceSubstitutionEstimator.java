/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeComparator;
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
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.validation.ValidationService;

import java.math.BigDecimal;
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

    private String currentUsagePointName;
    private MetrologyPurpose currentMetrologyPurpose;
    private ReadingType currentReadingType;

    private MeteringService meteringService;
    private ReadingType validatingReadingType;

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

    private void checkOverridenProperties() throws EstimationCancelledException {
        if (checkPurpose == null || referenceUsagePoint == null || referenceReadingTypeProperty == null) {
            LoggingContext.get()
                    .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.REFERENCE_ESTIMATOR_MISC_CONFIGURATION_NOT_COMPLETE)
                            .format(currentUsagePointName, currentMetrologyPurpose.getName(), currentReadingType.getFullAliasName(), getThesaurus().getFormat(TranslationKeys.ESTIMATOR_NAME).format()));
            throw new EstimationCancelledException();
        }
    }

    private UsagePointValueFactory.UsagePointReference getCheckUsagePointProperty() {
        return (UsagePointValueFactory.UsagePointReference) getProperty(CHECK_USAGE_POINT);
    }

    private ReadingTypeValueFactory.ReadingTypeReference getReferenceReadingTypeProperty() {
        return (ReadingTypeValueFactory.ReadingTypeReference) getProperty(CHECK_READING_TYPE);
    }

    @Override
    TranslationKey getEstimatorNameKey() {
        return TranslationKeys.ESTIMATOR_NAME;
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks, QualityCodeSystem system) {
        try {
            validateEstimationBlocksSize(estimationBlocks);
            getValidatingUsagePointData(estimationBlocks.get(0));
            checkOverridenProperties();
            // override validatingUsagePoint
            validatingUsagePoint = referenceUsagePoint.getUsagePoint();
            validatingReadingType = estimationBlocks.get(0).getReadingType();
            setCheckReadingTypeIfNull(referenceReadingTypeProperty.getReadingType());
            validateReferenceReadingType(estimationBlocks.get(0).getReadingType(), checkReadingType);
        } catch (EstimationCancelledException e) {
            return SimpleEstimationResult.of(estimationBlocks, Collections.emptyList());
        }
        return super.estimate(estimationBlocks, system);
    }

    private void getValidatingUsagePointData(EstimationBlock estimationBlock) {
        currentReadingType = estimationBlock.getReadingType();
        Optional<UsagePoint> usagePoint = estimationBlock.getChannel().getChannelsContainer().getUsagePoint();
        if (usagePoint.isPresent()) {
            currentUsagePointName = usagePoint.get().getName();
        } else {
            throw new IllegalStateException("Channels container of estimation block has no usage point");
        }
        ChannelsContainer channelsContainer = estimationBlock.getChannel().getChannelsContainer();
        if (channelsContainer instanceof MetrologyContractChannelsContainer) {
            currentMetrologyPurpose = ((MetrologyContractChannelsContainer) channelsContainer).getMetrologyContract().getMetrologyPurpose();
        } else {
            throw new IllegalStateException("Channels container is not instance of MetrologyContractChannelsContainer");
        }
    }

    private void validateEstimationBlocksSize(List<EstimationBlock> estimationBlocks) throws
            EstimationCancelledException {
        if (estimationBlocks.isEmpty()) {
            throw new EstimationCancelledException();
        }
    }

    private void validateReferenceReadingType(ReadingType validatingReadingType, ReadingType referenceReadingType) throws
            EstimationCancelledException {
        if (!areReadingTypesComparable(validatingReadingType, referenceReadingType)) {
            LoggingContext.get()
                    .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.REFERENCE_ESTIMATOR_REFERENCE_READING_TYPE_NOT_COMPARABLE)
                            .format(currentUsagePointName, currentMetrologyPurpose.getName(), currentReadingType.getFullAliasName(), getThesaurus().getFormat(TranslationKeys.ESTIMATOR_NAME)
                                    .format()));
            throw new EstimationCancelledException();
        }
    }

    private boolean areReadingTypesComparable(ReadingType validatingReadingType, ReadingType referenceReadingType) {
        return ReadingTypeComparator.ignoring(
                ReadingTypeComparator.Attribute.Multiplier
        ).compare(validatingReadingType, referenceReadingType) == 0;
    }

    @Override
    protected void setEstimatableValue(Estimatable estimatable, ReferenceReading referenceReading) {
        BigDecimal referenceValue = referenceReading.getReferenceValue()
                .scaleByPowerOfTen(-validatingReadingType.getMultiplier().getMultiplier())
                .scaleByPowerOfTen(checkReadingType.getMultiplier().getMultiplier());
        estimatable.setEstimation(referenceValue);
    }

    @Override
    public void validateProperties(Map<String, Object> properties) {
        MetrologyPurpose checkPurpose = Optional.ofNullable(properties.get(CHECK_PURPOSE))
                .map(o -> (MetrologyPurpose) o)
                .orElse(null);
        ReadingTypeValueFactory.ReadingTypeReference checkReadingTypeRef = Optional.ofNullable(properties.get(CHECK_READING_TYPE))
                .map(o -> (ReadingTypeValueFactory.ReadingTypeReference) o)
                .orElse(null);
        UsagePointValueFactory.UsagePointReference checkUsagePointReference = Optional.ofNullable(properties.get(CHECK_USAGE_POINT))
                .map(o -> (UsagePointValueFactory.UsagePointReference) o)
                .orElse(null);
        validateCheckUsagePointHasCheckPurpose(checkUsagePointReference, checkPurpose, checkReadingTypeRef);
    }

    private void validateCheckUsagePointHasCheckPurpose(UsagePointValueFactory.UsagePointReference checkUsagePointReference, MetrologyPurpose checkPurpose, ReadingTypeValueFactory.ReadingTypeReference checkReadingTypeRef) {
        if (checkUsagePointReference != null && checkPurpose != null && checkReadingTypeRef != null) {
            Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMC = checkUsagePointReference.getUsagePoint()
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
                throw new LocalizedFieldValidationException(MessageSeeds.REFERENCE_VALIDATE_PROPS_NO_PURPOSE_ON_USAGE_POINT, "properties." + CHECK_PURPOSE);
            } else {
                Channel channel = null;
                Optional<ChannelsContainer> channelsContainerWithCheckChannel = effectiveMC.get()
                        .getChannelsContainer(contract);
                if (channelsContainerWithCheckChannel.isPresent()) {
                    Optional<Channel> checkChannel = channelsContainerWithCheckChannel.get().getChannel(checkReadingTypeRef.getReadingType());
                    if (checkChannel.isPresent()) {
                        channel = checkChannel.get();
                    }
                }
                if (channel == null) {
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
                buildReferenceUsagePointPropertySpec(),
                buildCheckPurposePropertySpec(),
                buildReferenceReadingTypePropertySpec()
        );
    }

    @Override
    String getMessage(ReferenceReadingQuality referenceReadingQuality, EstimationBlock estimationBlock) {
        String message;
        switch (referenceReadingQuality) {
            case NO_MC:
                message = getThesaurus().getFormat(MessageSeeds.REFERENCE_ESTIMATOR_FAIL_EFFECTIVE_MC_NOT_FOUND)
                        .format(blockToString(estimationBlock), getThesaurus().getFormat(TranslationKeys.ESTIMATOR_NAME)
                                .format(), estimationBlock.getReadingType()
                                .getFullAliasName(), validatingUsagePoint.getName());
                break;
            case NO_PURPOSE_ON_UP:
                message = getThesaurus().getFormat(MessageSeeds.REFERENCE_ESTIMATOR_FAIL_PURPOSE_DOES_NOT_EXIST_ON_UP)
                        .format(currentUsagePointName, currentMetrologyPurpose.getName(), currentReadingType.getFullAliasName(), getThesaurus().getFormat(TranslationKeys.ESTIMATOR_NAME)
                                .format(), validatingUsagePoint.getName());
                break;
            case NO_CHECK_CHANNEL:
                message = getThesaurus().getFormat(MessageSeeds.REFERENCE_ESTIMATOR_FAIL_NO_OUTPUTS_ON_PURPOSE_WITH_READING_TYPE)
                        .format(blockToString(estimationBlock), getThesaurus().getFormat(TranslationKeys.ESTIMATOR_NAME)
                                .format(), estimationBlock.getReadingType()
                                .getFullAliasName(), validatingUsagePoint.getName());
                break;
            case REFERENCE_DATA_MISSING:
            case REFERENCE_DATA_SUSPECT:
                message = getThesaurus().getFormat(MessageSeeds.REFERENCE_ESTIMATOR_FAIL_DATA_SUSPECT_OR_MISSING)
                        .format(blockToString(estimationBlock), getThesaurus().getFormat(TranslationKeys.ESTIMATOR_NAME)
                                .format(), validatingUsagePoint.getName(), checkPurpose.getName(), estimationBlock.getReadingType()
                                .getFullAliasName());
                break;
            default:
                message = getThesaurus().getFormat(MessageSeeds.ESTIMATOR_FAIL_INTERNAL_ERROR)
                        .format(blockToString(estimationBlock), getThesaurus().getFormat(TranslationKeys.ESTIMATOR_NAME)
                                .format(), validatingUsagePoint.getName(), checkPurpose.getName(), estimationBlock.getReadingType()
                                .getFullAliasName());
        }
        return message;
    }

    private PropertySpec buildReferenceUsagePointPropertySpec() {
        return getPropertySpecService()
                .specForValuesOf(new UsagePointValueFactory(meteringService))
                .named(TranslationKeys.CHECK_USAGE_POINT)
                .describedAs(TranslationKeys.CHECK_USAGE_POINT_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
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
        ESTIMATOR_NAME(ReferenceSubstitutionEstimator.class.getName(), "Reference substitution [STD]"),

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
