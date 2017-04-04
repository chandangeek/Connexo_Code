/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.NonOrBigDecimalValueFactory;
import com.elster.jupiter.properties.NonOrBigDecimalValueProperty;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.TwoValuesAbsoluteDifference;
import com.elster.jupiter.properties.TwoValuesDifference;
import com.elster.jupiter.properties.TwoValuesDifferenceValueFactory;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.BadMainCheckConfiguration;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dantonov on 28.03.2017.
 */
public class MainCheckValidator extends AbstractValidator {

    static final String CHECK_PURPOSE = "checkPurpose";
    static final String MAX_ABSOLUTE_DIFF = "maximumAbsoluteDifference";
    static final String PASS_IF_NO_REF_DATA = "passIfNoRefData";
    static final String USE_VALIDATED_DATA = "useValidatedData";
    static final String MIN_THRESHOLD = "minThreshold";

    private static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDM);
    private MetrologyConfigurationService metrologyConfigurationService;

    private Map<Instant, IntervalReadingRecord> checkReadingRecords;

    // validator parameters
    private String checkChannelPurpose;
    private TwoValuesDifference maxAbsoluteDifference;
    private Boolean passIfNoRefData;
    private Boolean useValidatedData;
    private NonOrBigDecimalValueProperty minThreshold;


    public MainCheckValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, MetrologyConfigurationService metrologyConfigurationService) {
        super(thesaurus, propertySpecService);
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public MainCheckValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties, MetrologyConfigurationService metrologyConfigurationService) {
        super(thesaurus, propertySpecService, properties);
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Override
    public List<String> getRequiredProperties() {
        return Arrays.asList(CHECK_PURPOSE, MAX_ABSOLUTE_DIFF, MIN_THRESHOLD);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {

        List<String> metrologyPurposes = metrologyConfigurationService.getMetrologyPurposes()
                .stream()
                .map(MetrologyPurpose::getName)
                .collect(Collectors.toList());

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
                        .setDefaultValue(metrologyPurposes.size() != 0 ? metrologyPurposes.get(0) : "")
                        .addValues(metrologyPurposes)
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
        builder
                .add(getPropertySpecService()
                        .booleanSpec()
                        .named(USE_VALIDATED_DATA, TranslationKeys.MAIN_CHECK_VALIDATOR_USE_VALIDATED_DATA)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(false)
                        .finish());

        return builder.build();
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {

        // 1. parse validator parameters
        checkChannelPurpose = (String) properties.get(CHECK_PURPOSE);

        if (checkChannelPurpose == null) {
            throw new MissingRequiredProperty(getThesaurus(), CHECK_PURPOSE);
        }

        // FIXME - types? and save data
        Object maxDiff = properties.get(MAX_ABSOLUTE_DIFF);
        Object minThreshold = properties.get(MIN_THRESHOLD);
        passIfNoRefData = (boolean) properties.get(PASS_IF_NO_REF_DATA);
        useValidatedData = (boolean) properties.get(USE_VALIDATED_DATA);

        // find 'check' channel and save readings + prepare mapping with readings from 'main' channel

        // 2. find 'check' channel
        UsagePoint usagePoint = channel.getChannelsContainer()
                .getUsagePoint()
                .orElseThrow(() -> new BadMainCheckConfiguration(getThesaurus(), MessageSeeds.BAD_MAIN_CHECK_CONFIGURATION_NO_UP_ON_CHANNEL));
        List<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfigurationOnUsagePointList = usagePoint.getEffectiveMetrologyConfigurations(interval);

        if (effectiveMetrologyConfigurationOnUsagePointList.size() != 1) {
            throw new BadMainCheckConfiguration(getThesaurus(), MessageSeeds.BAD_MAIN_CHECK_CONFIGURATION_METRLOGY_CONFIG_COUNT);
        }

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = effectiveMetrologyConfigurationOnUsagePointList
                .get(0);

        MetrologyContract metrologyContract = effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(contract -> contract.getMetrologyPurpose().getName().equals(checkChannelPurpose))
                .findAny()
                .orElseThrow(() -> new BadMainCheckConfiguration(getThesaurus(), MessageSeeds.BAD_MAIN_CHECK_CONFIGURATION_METRLOGY_CONTRACT));

        ChannelsContainer channelsContainerWithCheckChannel = effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract)
                .orElseThrow(() -> new BadMainCheckConfiguration(getThesaurus(), MessageSeeds.BAD_MAIN_CHECK_CONFIGURATION_CHANNELS_CONTAINER));

        Channel checkChannel = channelsContainerWithCheckChannel.getChannel(readingType)
                .orElseThrow(() -> new BadMainCheckConfiguration(getThesaurus(), MessageSeeds.BAD_MAIN_CHECK_CONFIGURATION_CHECK_CHANNEL));

        // 3. prepare map of interval readings from check channel
        List<IntervalReadingRecord> checkChannelIntervalReadings = checkChannel.getIntervalReadings(interval);
        checkReadingRecords = checkChannelIntervalReadings.stream()
                .collect(Collectors.toMap(IntervalReadingRecord::getTimeStamp, Function.identity()));
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {

        IntervalReadingRecord checkIntervalReadingRecord = checkReadingRecords.get(intervalReadingRecord.getTimeStamp());

        return validate(intervalReadingRecord, checkIntervalReadingRecord);
    }

    private ValidationResult validate(IntervalReadingRecord mainReading, IntervalReadingRecord checkReading) {
        return ValidationResult.VALID;
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        // this validator is not planned to be applied for registers
        // So, this method has no logic
        return ValidationResult.NOT_VALIDATED;
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
