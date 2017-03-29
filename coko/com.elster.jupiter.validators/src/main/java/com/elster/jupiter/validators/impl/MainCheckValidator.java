/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.NonOrBigDecimalValueFactory;
import com.elster.jupiter.properties.NonOrBigDecimalValueProperty;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.TwoValuesAbsoluteDifference;
import com.elster.jupiter.properties.TwoValuesDifferenceValueFactory;
import com.elster.jupiter.properties.TwoValuesPercentDifference;
import com.elster.jupiter.validation.ValidationResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dantonov on 28.03.2017.
 */
public class MainCheckValidator extends AbstractValidator {

    static final String CHECK_PURPOSE = "checkPurpose";
    static final String MAX_ABSOLUTE_DIFF = "maximumAbsoluteDifference";
    static final String PASS_IF_NO_REF_DATA = "passIfNoRefData";
    static final String MIN_THRESHOLD = "minThreshold";

    private static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDM);

    public MainCheckValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    public MainCheckValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
    }

    @Override
    public List<String> getRequiredProperties() {
        return Arrays.asList(CHECK_PURPOSE, MAX_ABSOLUTE_DIFF, MIN_THRESHOLD);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder
                .add(getPropertySpecService()
                        .specForValuesOf(new TwoValuesDifferenceValueFactory())
                        .named(MAX_ABSOLUTE_DIFF, TranslationKeys.MAIN_CHECK_VALIDATOR_MAX_ABSOLUTE_DIFF)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(new TwoValuesAbsoluteDifference() {{
                            value = new BigDecimal(0);
                        }})
                        .finish());
        builder
                .add(getPropertySpecService()
                        .specForValuesOf(new NonOrBigDecimalValueFactory())
                        .named(MIN_THRESHOLD, TranslationKeys.MAIN_CHECK_VALIDATOR_MIN_THRESHOLD)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(new NonOrBigDecimalValueProperty())
                        .finish());
        builder
                .add(getPropertySpecService()
                        .stringSpec()
                        .named(CHECK_PURPOSE, TranslationKeys.MAIN_CHECK_VALIDATOR_CHECK_PURPOSE)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue("possible1")
                        .addValues(Arrays.asList("possible1", "possible2"))
                        .markExhaustive(PropertySelectionMode.COMBOBOX)
                        .finish());
        builder
                .add(getPropertySpecService()
                        .booleanSpec()
                        .named(PASS_IF_NO_REF_DATA, TranslationKeys.MAIN_CHECK_VALIDATOR_PASS_IF_NO_REF_DATA)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(false)
                        .finish());
        return builder.build();
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {

    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        return null;
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        return null;
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.MAIN_CHECK_VALIDATOR.getDefaultFormat();
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return QUALITY_CODE_SYSTEMS;
    }
}
