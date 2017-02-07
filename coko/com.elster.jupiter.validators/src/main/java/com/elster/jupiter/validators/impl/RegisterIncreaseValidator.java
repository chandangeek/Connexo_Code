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
import com.elster.jupiter.validation.ValidationResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * WARNING: The following implementation does not check the "OVERFLOW" flag on a register.
 * Should be done when the API which allows this check of the "OVERFLOW" flag will be available.
 */
class RegisterIncreaseValidator extends AbstractValidator {

    static final String FAIL_EQUAL_DATA = "failEqualData";
    private static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM);

    private Channel channel;
    private boolean failEqualData = false;

    RegisterIncreaseValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    RegisterIncreaseValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        this.channel = channel;
        failEqualData = (boolean) properties.get(FAIL_EQUAL_DATA);
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        List<BaseReadingRecord> records = channel.getReadingsBefore(readingRecord.getTimeStamp(), 1);
        if ( records.isEmpty() ) {
            //no previous readings for this register, nothing to compare with...
            return ValidationResult.VALID;
        }
        BigDecimal previous = records.get(0).getValue();
        BigDecimal current = readingRecord.getValue();

        if (previous != null && current != null) {
            int comparisonResult = previous.compareTo(current);

            if (comparisonResult > 0 || comparisonResult == 0 && failEqualData) {
                return ValidationResult.SUSPECT;
            }
        }
        return ValidationResult.VALID;
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        // In order to prevent unexpected validation fails we always return VALID
        // because this validator is only for registers
        return ValidationResult.VALID;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(
            getPropertySpecService()
                .booleanSpec()
                .named(FAIL_EQUAL_DATA, TranslationKeys.REGISTER_INCREASE_VALIDATOR_FAIL_EQUAL_DATA)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .finish());
        return builder.build();
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.REGISTER_INCREASE_VALIDATOR.getDefaultFormat();
    }

    @Override
    public List<String> getRequiredProperties() {
        return Collections.singletonList(FAIL_EQUAL_DATA);
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return QUALITY_CODE_SYSTEMS;
    }
}
