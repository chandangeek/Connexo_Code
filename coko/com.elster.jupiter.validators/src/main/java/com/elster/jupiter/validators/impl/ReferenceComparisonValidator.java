/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeComparator;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validators.MissingRequiredProperty;
import com.elster.jupiter.validators.impl.properties.ReadingTypeReference;
import com.elster.jupiter.validators.impl.properties.ReadingTypeValueFactory;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The validator compares delta values on the validated output channel on a usage point to another output channel
 * from a different usage point with a similar reading type (allowed flexibility in the multiplier).
 * If compared values with identical time and date are not equal or the difference exceeds the configured parameter a suspect is created.
 * If the minimum threshold is configured the check for the interval is skipped and the validation moves to the next interval.
 */
public class ReferenceComparisonValidator extends MainCheckAbstractValidator {

    static final String CHECK_USAGE_POINT = "checkUsagePoint";
    static final String CHECK_READING_TYPE = "checkReadingType";

    private MeteringService meteringService;

    private UsagePoint referenceUsagePoint;
    private ReadingType referenceReadinType;

    public ReferenceComparisonValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService, MeteringService meteringService) {
        super(thesaurus, propertySpecService, metrologyConfigurationService, validationService);
        this.meteringService = meteringService;
    }

    public ReferenceComparisonValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService, MeteringService meteringService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, metrologyConfigurationService, validationService, properties);
        this.meteringService = meteringService;
    }

    @Override
    public void validateProperties(Map<String, Object> properties) {
        // FIXME:

        // The chosen check usage point doesn't have the selected purpose
        // The chosen reading type is not available on the selected purpose of the usage point
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                buildMaxAbsoluteDiffPropertySpec(),
                buildPassIfNoRefDataPropertySpec(),
                buildUseValidatedDataPropertySpec(),
                buildMinThresholdPropertySpec(),
                buildReferenceUsagePointPropertySpec(),
                buildCheckPurposePropertySpec(),
                buildReferenceReadingTypePropertySpec()
        );
    }

    @Override
    public List<PropertySpec> getPropertySpecs(ValidationPropertyDefinitionLevel level) {
        return ValidationPropertyDefinitionLevel.VALIDATION_RULE == level ? getConfigurationPropertySpecs() : getOverridenPropertySpecs();
    }

    private List<PropertySpec> getConfigurationPropertySpecs() {
        return Arrays.asList(
                buildMaxAbsoluteDiffPropertySpec(),
                buildPassIfNoRefDataPropertySpec(),
                buildUseValidatedDataPropertySpec(),
                buildMinThresholdPropertySpec()
        );
    }

    private List<PropertySpec> getOverridenPropertySpecs() {
        return Arrays.asList(
                buildReferenceUsagePointPropertySpec(),
                buildCheckPurposePropertySpec(),
                buildReferenceReadingTypePropertySpec(),
                buildMaxAbsoluteDiffPropertySpec(),
                buildMinThresholdPropertySpec()
        );
    }

    private PropertySpec buildReferenceUsagePointPropertySpec() {
        return getPropertySpecService()
                .referenceSpec(UsagePoint.class)
                .named(CHECK_USAGE_POINT, TranslationKeys.CHECK_USAGE_POINT)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
    }

    UsagePoint getCheckUsagePointProperty() {
        UsagePoint value = (UsagePoint) properties.get(CHECK_USAGE_POINT);
        if (value == null) {
            throw new MissingRequiredProperty(getThesaurus(), CHECK_USAGE_POINT);
        }
        return value;
    }

    private PropertySpec buildReferenceReadingTypePropertySpec() {
        return getPropertySpecService()
                .specForValuesOf(new ReadingTypeValueFactory(meteringService, ReadingTypeValueFactory.Mode.ONLY_REGULAR))
                .named(CHECK_READING_TYPE, TranslationKeys.CHECK_READING_TYPE)
                .fromThesaurus(getThesaurus())
                .markRequired()
                .finish();
    }

    private ReadingTypeReference getReferenceReadingTypeProperty() {
        return (ReadingTypeReference) super.properties.get(CHECK_READING_TYPE);
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        super.init(channel,readingType,interval);

        referenceUsagePoint = getCheckUsagePointProperty();
        referenceReadinType = getReferenceReadingTypeProperty().getReadingType();

        try {
            validateReferenceReadingType(readingType,referenceReadinType);
            validateReferenceUsagePoint();
            initValidatingPurpose();
            initUsagePointName(channel);
            initCheckData(referenceUsagePoint, referenceReadinType);
        }catch (InitCancelException e){
            preparedValidationResult = e.getValidationResult();
        }
    }

    private void validateReferenceUsagePoint() throws InitCancelException{
        // TODO: verify check usage point exists
    }

    @Override
    protected ComparingValues calculateComparingValues(IntervalReadingRecord mainReading, IntervalReadingRecord checkReading) {
        BigDecimal mainValue = mainReading.getValue();
        BigDecimal referenceValue = checkReading.getValue()
        .scaleByPowerOfTen(checkReading.getReadingType().getMultiplier().getMultiplier())
                .scaleByPowerOfTen(-mainReading.getReadingType().getMultiplier().getMultiplier());
        return new ComparingValues(mainValue, referenceValue);
    }

    private void validateReferenceReadingType(ReadingType validatingReadingType, ReadingType referenceReadingType) throws InitCancelException{
        if (!areReadingTypesComparable(validatingReadingType,referenceReadingType)){
            LoggingContext.get()
                    .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.REFERENCE_MISC_REFERENCE_READING_TYPE_NOT_SUTABLE)
                            .format(rangeToString(failedValidatonInterval), getDisplayName(), readingType, validatingUsagePointName));
            throw new InitCancelException(ValidationResult.NOT_VALIDATED);
        }
    }

    private boolean areReadingTypesComparable(ReadingType validatingReadingType, ReadingType referenceReadingType) {
        return ReadingTypeComparator.ignoring(
                ReadingTypeComparator.Attribute.Multiplier
        ).compare(validatingReadingType, referenceReadingType) == 0;
    }

    @Override
    public String getDefaultFormat() {
        return "Reference comparison";
    }

    @Override
    String getClassName() {
        return ReferenceComparisonValidator.class.getName();
    }

    @Override
    TranslationKey[] getAdditionalExtraTranslationKeys() {
        return TranslationKeys.values();
    }

    enum TranslationKeys implements TranslationKey {
        CHECK_USAGE_POINT(ReferenceComparisonValidator.CHECK_USAGE_POINT, "Check usage point"),
        CHECK_READING_TYPE(ReferenceComparisonValidator.CHECK_READING_TYPE, "Check reading type");

        private final String key;
        private final String defaultFormat;

        TranslationKeys(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return this.defaultFormat;
        }
    }

    @Override
    void logFailure(InitCancelProps props) {
        // FIXME: verify messages
        InitCancelReason reason = props.reason;
        switch (reason) {
            case NO_REFERENCE_PURPOSE_FOUND_ON_REFERENCE_USAGE_POINT:
                LoggingContext.get()
                        .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.REFERENCE_MISC_NO_PURPOSE)
                                .format(rangeToString(failedValidatonInterval), getDisplayName(), props.readingType, validatingUsagePointName));
                break;
            case REFERENCE_PURPOSE_HAS_NOT_BEEN_EVER_ACTIVATED:
                LoggingContext.get()
                        .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.REFERENCE_MISC_PURPOSE_NEVER_ACTIVATED)
                                .format(rangeToString(failedValidatonInterval), getDisplayName(), props.readingType, validatingUsagePointName));

                break;
            case REFERENCE_OUTPUT_DOES_NOT_EXIST:
                LoggingContext.get()
                        .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.REFERENCE_MISC_NO_CHECK_OUTPUT)
                                .format(rangeToString(failedValidatonInterval),
                                        getDisplayName(),
                                        props.readingType,
                                        validatingUsagePointName));
                break;
        }
    }
}
