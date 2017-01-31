/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.MissingRequiredProperty;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.elster.jupiter.validation.ValidationResult.NOT_VALIDATED;
import static com.elster.jupiter.validation.ValidationResult.SUSPECT;
import static com.elster.jupiter.validation.ValidationResult.VALID;

class ThresholdValidator extends AbstractValidator {

    static final String MIN = "minimum";
    static final String MAX = "maximum";
    private static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM);

    private Quantity minimum;
    private Quantity maximum;
    private ReadingType readingType;

    ThresholdValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    ThresholdValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
    }

    private Quantity getRequiredQuantity(Map<String, Object> properties, String key, ReadingType readingType) {
        BigDecimal quantity = (BigDecimal) properties.get(key);
        if (quantity == null) {
            throw new MissingRequiredProperty(getThesaurus(), key);
        }
        return readingType.getUnit().getUnit().amount(quantity, readingType.getMultiplier().getMultiplier());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder
            .add(getPropertySpecService()
                    .bigDecimalSpec()
                    .named(MIN, TranslationKeys.THRESHOLD_VALIDATOR_MIN)
                    .fromThesaurus(this.getThesaurus())
                    .markRequired()
                    .setDefaultValue(BigDecimal.ZERO)
                    .finish());
        builder
            .add(getPropertySpecService()
                    .bigDecimalSpec()
                    .named(MAX, TranslationKeys.THRESHOLD_VALIDATOR_MAX)
                    .fromThesaurus(this.getThesaurus())
                    .markRequired()
                    .setDefaultValue(BigDecimal.ZERO)
                    .finish());
        return builder.build();
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        this.readingType = readingType;
        this.minimum = getRequiredQuantity(properties, MIN, readingType);
        this.maximum = getRequiredQuantity(properties, MAX, readingType);
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        return validateBaseReadingRecord(intervalReadingRecord);
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        return validateBaseReadingRecord(readingRecord);
    }

    private ValidationResult validateBaseReadingRecord(BaseReadingRecord baseReadingRecord) {
        return validateQuantity(baseReadingRecord.getQuantity(readingType));
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.THRESHOLD_VALIDATOR.getDefaultFormat();
    }

    @Override
    public List<String> getRequiredProperties() {
        return Arrays.asList(MIN, MAX);
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return QUALITY_CODE_SYSTEMS;
    }

    private ValidationResult validateQuantity(Quantity toValidate) {
        if (toValidate == null) {
            return NOT_VALIDATED;
        }
        return isWithinBounds(toValidate) ? VALID : SUSPECT;
    }

    private boolean isWithinBounds(Quantity toValidate) {
        return minimum.compareTo(toValidate) <= 0 && maximum.compareTo(toValidate) >= 0;
    }
}
