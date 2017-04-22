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
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.NonOrBigDecimalValueFactory;
import com.elster.jupiter.properties.NonOrBigDecimalValueProperty;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.TwoValuesAbsoluteDifference;
import com.elster.jupiter.properties.TwoValuesDifference;
import com.elster.jupiter.properties.TwoValuesDifferenceValueFactory;
import com.elster.jupiter.properties.TwoValuesPercentDifference;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validators.MissingRequiredProperty;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * The 'Main/Check comparison' validator allows to compare output values from one channel to another "check" channel on the same usage point.<br>
 * The validator compares delta values on the validated output channel on a usage point to another output channel with the same reading type
 * but on a different purpose. If compared values with identical time and date are not equal or the difference exceeds the configured parameter a suspect is created.
 * If the minimum threshold is configured the check for the interval is skipped and the validation moves to the next interval.
 */
public class MainCheckValidator extends AbstractValidator {

    static final String CHECK_PURPOSE = "checkPurpose";
    static final String MAX_ABSOLUTE_DIFF = "maximumAbsoluteDifference";
    static final String PASS_IF_NO_REF_DATA = "passIfNoRefData";
    static final String USE_VALIDATED_DATA = "useValidatedData";
    static final String MIN_THRESHOLD = "minThreshold";

    private static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDM);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DefaultDateTimeFormatters.mediumDate()
            .withShortTime()
            .build()
            .withZone(ZoneId
                    .systemDefault())
            .withLocale(Locale.ENGLISH);

    private MetrologyConfigurationService metrologyConfigurationService;
    private ValidationService validationService;

    private Map<Instant, IntervalReadingRecord> checkReadingRecords;
    private Map<Instant, ValidationResult> checkReadingRecordValidations;

    // validator parameters
    private MetrologyPurpose checkChannelPurpose;
    private TwoValuesDifference maxAbsoluteDifference;
    private Boolean passIfNoRefData;
    private Boolean useValidatedData;
    private NonOrBigDecimalValueProperty minThreshold;
    private ReadingType readingType;
    // interval to log failed validation
    private Range<Instant> failedValidatonInterval;

    private Logger logger;

    private String usagePointName;

    private ValidationResult preparedValidationResult;

    public MainCheckValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService) {
        super(thesaurus, propertySpecService);
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.validationService = validationService;
    }

    public MainCheckValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService) {
        super(thesaurus, propertySpecService, properties);
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.validationService = validationService;
    }

    @Override
    public List<String> getRequiredProperties() {
        return Arrays.asList(CHECK_PURPOSE, MAX_ABSOLUTE_DIFF, MIN_THRESHOLD);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {

        List<MetrologyPurpose> metrologyPurposes = metrologyConfigurationService.getMetrologyPurposes();

        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder
                .add(getPropertySpecService()
                        .referenceSpec(MetrologyPurpose.class)
                        .named(CHECK_PURPOSE, TranslationKeys.CHECK_PURPOSE)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(metrologyPurposes.get(0))
                        .addValues(metrologyPurposes)
                        .markExhaustive(PropertySelectionMode.COMBOBOX)
                        .finish());
        addMaxAbsoluteDiffPropertySpec(builder);
        builder
                .add(getPropertySpecService()
                        .booleanSpec()
                        .named(PASS_IF_NO_REF_DATA, TranslationKeys.PASS_IF_NO_REF_DATA)
                        .fromThesaurus(this.getThesaurus())
                        .finish());
        builder
                .add(getPropertySpecService()
                        .booleanSpec()
                        .named(USE_VALIDATED_DATA, TranslationKeys.USE_VALIDATED_DATA)
                        .fromThesaurus(this.getThesaurus())
                        .finish());
        addMinThresholdPropertySpec(builder);
        return builder.build();
    }

    @Override
    public List<PropertySpec> getPropertySpecs(ValidationPropertyDefinitionLevel level) {
        return ValidationPropertyDefinitionLevel.VALIDATION_RULE == level ? getPropertySpecs() : getOverridenPropertySpecs();
    }

    private List<PropertySpec> getOverridenPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        addOverridenPropertySpecs(builder);
        return builder.build();
    }

    private void addOverridenPropertySpecs(ImmutableList.Builder<PropertySpec> builder) {
        addMaxAbsoluteDiffPropertySpec(builder);
        addMinThresholdPropertySpec(builder);
    }

    private void addMaxAbsoluteDiffPropertySpec(ImmutableList.Builder<PropertySpec> builder) {
        builder
                .add(getPropertySpecService()
                        .specForValuesOf(new TwoValuesDifferenceValueFactory())
                        .named(MAX_ABSOLUTE_DIFF, TranslationKeys.MAX_ABSOLUTE_DIFF)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(new TwoValuesAbsoluteDifference() {{
                            value = new BigDecimal(0);
                        }})
                        .finish());
    }

    private void addMinThresholdPropertySpec(ImmutableList.Builder<PropertySpec> builder) {
        builder
                .add(getPropertySpecService()
                        .specForValuesOf(new NonOrBigDecimalValueFactory())
                        .named(MIN_THRESHOLD, TranslationKeys.MIN_THRESHOLD)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(new NonOrBigDecimalValueProperty())
                        .finish());
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {

        this.readingType = readingType;
        this.failedValidatonInterval = interval;

        //LoggingContext.get().info(getLogger(), "init main check");

        // 1. parse validator parameters
        checkChannelPurpose = (MetrologyPurpose) properties.get(CHECK_PURPOSE);
        if (checkChannelPurpose == null) {
            throw new MissingRequiredProperty(getThesaurus(), CHECK_PURPOSE);
        }
        maxAbsoluteDifference = (TwoValuesDifference) properties.get(MAX_ABSOLUTE_DIFF);
        if (maxAbsoluteDifference == null) {
            throw new MissingRequiredProperty(getThesaurus(), MAX_ABSOLUTE_DIFF);
        }
        minThreshold = (NonOrBigDecimalValueProperty) properties.get(MIN_THRESHOLD);
        if (minThreshold == null) {
            throw new MissingRequiredProperty(getThesaurus(), MIN_THRESHOLD);
        }
        passIfNoRefData = (boolean) properties.get(PASS_IF_NO_REF_DATA);
        if (passIfNoRefData == null) {
            throw new MissingRequiredProperty(getThesaurus(), PASS_IF_NO_REF_DATA);
        }
        useValidatedData = (boolean) properties.get(USE_VALIDATED_DATA);
        if (useValidatedData == null) {
            throw new MissingRequiredProperty(getThesaurus(), USE_VALIDATED_DATA);
        }

        // find 'check' channel and save readings + prepare mapping with readings from 'main' channel

        // 2. find 'check' channel
        Optional<UsagePoint> usagePoint = channel.getChannelsContainer()
                .getUsagePoint();

        if (!usagePoint.isPresent()) {
            LoggingContext.get()
                    .severe(getLogger(), getThesaurus().getFormat(MessageSeeds.MAIN_CHECK_MISC_NO_UP)
                            .format(rangeToString(failedValidatonInterval), getDisplayName(), readingType.getFullAliasName()));
            preparedValidationResult = ValidationResult.NOT_VALIDATED;
            return;
        }

        usagePointName = usagePoint.get().getName();

        List<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMCList = usagePoint.get()
                .getEffectiveMetrologyConfigurations(interval);

        if (effectiveMCList.size() != 1) {
            LoggingContext.get()
                    .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.MAIN_CHECK_MISC_NOT_ONE_EMC)
                            .format(rangeToString(failedValidatonInterval), getDisplayName(), readingType.getFullAliasName(), effectiveMCList
                                    .size()));
            preparedValidationResult = ValidationResult.NOT_VALIDATED;
            return;
        }

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = effectiveMCList.get(0);

        Optional<MetrologyContract> metrologyContract = effectiveMC.getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(contract -> contract.getMetrologyPurpose().equals(checkChannelPurpose))
                .findAny();

        // {RULE FLOW CHECK] specified purpose is not found on the usage point
        if (!metrologyContract.isPresent()) {
            // [RULE FLOW ACTION] Stop validation for the channel independently from Pass if no reference data field value (last check remains as before the validation), an error message appears in the log
            LoggingContext.get()
                    .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.MAIN_CHECK_MISC_NO_PURPOSE)
                            .format(rangeToString(failedValidatonInterval), getDisplayName(), readingType.getFullAliasName(), usagePointName));
            preparedValidationResult = ValidationResult.NOT_VALIDATED;
            return;
        }

        boolean checkOutputExistOnPurpose = false;

        Optional<ChannelsContainer> channelsContainerWithCheckChannel = effectiveMC.getChannelsContainer(metrologyContract
                .get());
        if (channelsContainerWithCheckChannel.isPresent()) {
            Optional<Channel> checkChannel = channelsContainerWithCheckChannel.get().getChannel(readingType);
            if (checkChannel.isPresent()) {
                checkOutputExistOnPurpose = true;
                // 3. prepare map of interval readings from check channel
                List<IntervalReadingRecord> checkChannelIntervalReadings = checkChannel.get()
                        .getIntervalReadings(interval);
                checkReadingRecords = checkChannelIntervalReadings.stream()
                        .collect(Collectors.toMap(IntervalReadingRecord::getTimeStamp, Function.identity()));

                // 4. get validation statuses for check channel

                ValidationEvaluator evaluator = validationService.getEvaluator();
                checkReadingRecordValidations = evaluator.getValidationStatus(QUALITY_CODE_SYSTEMS, checkChannel.get(), checkChannelIntervalReadings)
                        .stream()
                        .collect(Collectors.toMap(DataValidationStatus::getReadingTimestamp, DataValidationStatus::getValidationResult));
            }
        } else {
            // this means that purpose is not active on a usagepoint
            LoggingContext.get()
                    .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.MAIN_CHECK_MISC_PURPOSE_NEVER_ACTIVATED)
                            .format(rangeToString(failedValidatonInterval), getDisplayName(), readingType.getFullAliasName(), usagePoint
                                    .get()
                                    .getName()));
            preparedValidationResult = ValidationResult.NOT_VALIDATED;
            return;
        }

        // {RULE FLOW CHECK] no 'check' output with matching reading type exists on the chosen purpose
        if (!checkOutputExistOnPurpose) {
            // [RULE FLOW ACTION] Stop validation for the channel independently from Pass if no reference data field value (last check remains as before the validation), an error message appears in the log
            LoggingContext.get()
                    .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.MAIN_CHECK_MISC_NO_CHECK_OUTPUT)
                            .format(rangeToString(failedValidatonInterval),
                                    getDisplayName(),
                                    readingType.getFullAliasName(),
                                    usagePointName));
            preparedValidationResult = ValidationResult.NOT_VALIDATED;
        }
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {

        // verify predefined behaviour
        if (preparedValidationResult != null) {
            return preparedValidationResult;
        }

        IntervalReadingRecord checkIntervalReadingRecord = checkReadingRecords.get(intervalReadingRecord.getTimeStamp());

        return validate(intervalReadingRecord, checkIntervalReadingRecord);
    }

    private void prepareValidationResult(ValidationResult validationResult, Instant timeStamp) {
        preparedValidationResult = validationResult;

        failedValidatonInterval = Range.range(timeStamp, failedValidatonInterval.lowerBoundType(), failedValidatonInterval
                .upperEndpoint(), failedValidatonInterval.upperBoundType());

    }

    //  "Wed, 15 Feb 2017 00:00 until Thu, 16 Feb 2017 00:00"
    private String rangeToString(Range<Instant> range) {
        Instant lowerBound = null;
        if (range.hasLowerBound()) {
            lowerBound = range.lowerEndpoint();
        }

        Instant upperBound = null;
        if (range.hasUpperBound()) {
            upperBound = range.upperEndpoint();
        }

        String lower = lowerBound != null ? DATE_TIME_FORMATTER.format(lowerBound) : "-\"\\u221E\\t\"";
        String upper = upperBound != null ? DATE_TIME_FORMATTER.format(upperBound) : "+\"\\u221E\\t\"";
        return "\"" + lower + " until " + upper + "\"";
    }

    private ValidationResult validate(IntervalReadingRecord mainReading, IntervalReadingRecord checkReading) {

        Instant timeStamp = mainReading.getTimeStamp();

        // [RULE CHECK] If no data is available on the check channel:
        if (checkReading == null) {
            // show log
            LoggingContext.get()
                    .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.MAIN_CHECK_MISC_CHECK_OUTPUT_MISSING_OR_NOT_VALID)
                            .format(rangeToString(failedValidatonInterval), getDisplayName(), usagePointName, readingType.getFullAliasName()));

            if (passIfNoRefData) {
                // [RULE ACTION] No further checks are done to the interval (marked as valid) and the rule moves to the next interval if Pass if no reference data is checked
                return ValidationResult.VALID;
            } else {
                // [RULE ACTION]  Stop the validation at the timestamp where the timestamp with the last reference data was found for the channel if Pass if no reference data is not checked
                prepareValidationResult(ValidationResult.NOT_VALIDATED, timeStamp);
                return ValidationResult.NOT_VALIDATED;
            }
        }

        // [RULE FLOW CHECK] Data is available on check output but not validated:
        ValidationResult checkReadingValidationResult = checkReadingRecordValidations.getOrDefault(checkReading
                .getTimeStamp(), ValidationResult.NOT_VALIDATED);
        if (checkReadingValidationResult != ValidationResult.VALID) {
            // show log
            if (useValidatedData) {
                LoggingContext.get()
                        .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.MAIN_CHECK_MISC_CHECK_OUTPUT_MISSING_OR_NOT_VALID)
                                .format(rangeToString(failedValidatonInterval), getDisplayName(), usagePointName, readingType.getFullAliasName()));
                // [RULE ACTION] Stop the validation at the timestamp where the timestamp with the last validated reference data was found for the channel if Use validated data is checked
                prepareValidationResult(ValidationResult.NOT_VALIDATED, timeStamp);
                return ValidationResult.NOT_VALIDATED;
            }   // else:
            // [RULE ACTION] Continue validation if Use validated data is unchecked
            // So, next checks will be applied

        }

        BigDecimal mainValue = mainReading.getValue();
        BigDecimal checkValue = checkReading.getValue();

        if (!minThreshold.isNone) {
            if (mainValue.compareTo(minThreshold.value) <= 0 && checkValue.compareTo(minThreshold.value) <= 0) {
                // [RULE FLOW ACTION] the check for the interval is marked valid and the validation moves to the next interval.
                return ValidationResult.VALID;
            }
        }

        BigDecimal differenceValue;

        if (maxAbsoluteDifference instanceof TwoValuesAbsoluteDifference) {
            differenceValue = ((TwoValuesAbsoluteDifference) maxAbsoluteDifference).value;
        } else if (maxAbsoluteDifference instanceof TwoValuesPercentDifference) {
            differenceValue = mainValue.multiply(BigDecimal.valueOf(((TwoValuesPercentDifference) maxAbsoluteDifference).percent * 0.01D));
        } else {
            return ValidationResult.NOT_VALIDATED;
        }

        if (mainValue.subtract(checkValue).abs().compareTo(differenceValue) > 0) {
            return ValidationResult.SUSPECT;
        } else {
            return ValidationResult.VALID;
        }
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        // this validator is not planned to be applied for registers
        // So, this method has no logic
        return ValidationResult.NOT_VALIDATED;
    }

    @Override
    public String getDefaultFormat() {
        return "Main/check comparison";
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return QUALITY_CODE_SYSTEMS;
    }

    private Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
        return logger;
    }

    @Override
    public List<TranslationKey> getExtraTranslationKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    enum TranslationKeys implements TranslationKey {
        CHECK_PURPOSE(MainCheckValidator.CHECK_PURPOSE, "Check purpose"),
        MAX_ABSOLUTE_DIFF(MainCheckValidator.MAX_ABSOLUTE_DIFF, "Maximum absolute difference"),
        PASS_IF_NO_REF_DATA(MainCheckValidator.PASS_IF_NO_REF_DATA, "Pass if no reference data"),
        USE_VALIDATED_DATA(MainCheckValidator.USE_VALIDATED_DATA, "Use validated data"),
        MIN_THRESHOLD(MainCheckValidator.MIN_THRESHOLD, "Minimum threshold");

        private final String key;
        private final String defaultFormat;

        TranslationKeys(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return MainCheckValidator.class.getName() + "." + key;
        }

        @Override
        public String getDefaultFormat() {
            return this.defaultFormat;
        }
    }
}
