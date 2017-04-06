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
import com.elster.jupiter.properties.TwoValuesPercentDifference;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validators.MissingRequiredProperty;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    // {0} - period, {1} - name of the validator, {2} - reading type, {3} - failure reason
    private static final String VALIDATOR_FAILED_MESSAGE_PATTERN = "Failed to validate period %s using method \"%s\" on %s since %s";


    private MetrologyConfigurationService metrologyConfigurationService;
    private ValidationService validationService;

    private Map<Instant, IntervalReadingRecord> checkReadingRecords;
    private Map<Instant, ValidationResult> checkReadingRecordValidations;

    // validator parameters
    private String checkChannelPurpose;
    private TwoValuesDifference maxAbsoluteDifference;
    private Boolean passIfNoRefData;
    private Boolean useValidatedData;
    private NonOrBigDecimalValueProperty minThreshold;
    private ReadingType readingType;
    private Range<Instant> interval;

    private Logger logger;

    private String usagePointName;

    private ValidationResult preparedValidationResult;

    // not_validated by threshold
    private List<Instant> notValidatedByThreshold;
    private Instant lastValidatedReading;

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

    private String generateFailMessage(String message, Object... args) {
        // FIXME period to string
        // FIXME reading type to string
        return String.format(VALIDATOR_FAILED_MESSAGE_PATTERN, rangeToString(interval), TranslationKeys.MAIN_CHECK_VALIDATOR
                .getDefaultFormat(), readingType.getFullAliasName(), String.format(message, args));
    }

    private String generateFailMessageWithUsagePoint(String message, Object... args) {
        // FIXME period to string
        // FIXME reading type to string
        return String.format(VALIDATOR_FAILED_MESSAGE_PATTERN, rangeToString(interval), TranslationKeys.MAIN_CHECK_VALIDATOR
                .getDefaultFormat(), usagePointName + "/" + readingType.getFullAliasName(), String.format(message, args));
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

        this.readingType = readingType;
        this.interval = interval;

        //LoggingContext.get().info(getLogger(), "init main check");

        // 1. parse validator parameters
        checkChannelPurpose = (String) properties.get(CHECK_PURPOSE);
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

        notValidatedByThreshold = new ArrayList<>();

        // find 'check' channel and save readings + prepare mapping with readings from 'main' channel

        // 2. find 'check' channel
        Optional<UsagePoint> usagePoint = channel.getChannelsContainer()
                .getUsagePoint();

        if (!usagePoint.isPresent()) {
            LoggingContext.get()
                    .severe(getLogger(), generateFailMessage("main channel has no usage point"));
            preparedValidationResult = ValidationResult.NOT_VALIDATED;
            return;
        }

        usagePointName = usagePoint.get().getName();

        List<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfigurationOnUsagePointList = usagePoint.get()
                .getEffectiveMetrologyConfigurations(interval);

        if (effectiveMetrologyConfigurationOnUsagePointList.size() != 1) {
            LoggingContext.get()
                    .warning(getLogger(), generateFailMessage("usage point must have one effective metrology configuration, but has %s", effectiveMetrologyConfigurationOnUsagePointList
                            .size()));
            preparedValidationResult = ValidationResult.NOT_VALIDATED;
            return;
        }

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = effectiveMetrologyConfigurationOnUsagePointList
                .get(0);

        Optional<MetrologyContract> metrologyContract = effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(contract -> contract.getMetrologyPurpose().getName().equals(checkChannelPurpose))
                .findAny();

        // {RULE FLOW CHECK] specified purpose is not found on the usage point
        if (!metrologyContract.isPresent()) {
            // [RULE FLOW ACTION] Stop validation for the channel independently from Pass if no reference data field value (last check remains as before the validation), an error message appears in the log
            LoggingContext.get()
                    .warning(getLogger(), generateFailMessage("the specified purpose doesn't exist on the %s", usagePoint
                            .get()
                            .getName()));
            preparedValidationResult = ValidationResult.NOT_VALIDATED;
            return;
        }

        boolean checkOutputExistOnPurpose = false;

        Optional<ChannelsContainer> channelsContainerWithCheckChannel = effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract
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
        }

        // {RULE FLOW CHECK] no 'check' output with matching reading type exists on the chosen purpose
        if (!checkOutputExistOnPurpose) {
            // [RULE FLOW ACTION] Stop validation for the channel independently from Pass if no reference data field value (last check remains as before the validation), an error message appears in the log
            LoggingContext.get()
                    .warning(getLogger(), generateFailMessage("'check' output with matching reading type on the specified purpose doesn't exist on %s", usagePoint
                            .get()
                            .getName()));
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

        ValidationResult validationResult =  validate(intervalReadingRecord, checkIntervalReadingRecord);
        if (!validationResult.equals(ValidationResult.NOT_VALIDATED)){
            // remember last validated reading
            lastValidatedReading = intervalReadingRecord.getTimeStamp();
        }
        return validationResult;
    }

    //  "Wed, 15 Feb 2017 00:00 until Thu, 16 Feb 2017 00:00"
    private String rangeToString(Range<Instant> range){

        DateFormat df = new SimpleDateFormat("E, FF MMM yyyy hh:mm", Locale.US);

        Instant lowerBound = null;
        if (range.hasLowerBound()){
            lowerBound = range.lowerEndpoint();
        }

        Instant upperBound = null;
        if (range.hasUpperBound()){
            upperBound= range.upperEndpoint();
        }

        String lower = lowerBound!=null?df.format(new Date(lowerBound.toEpochMilli())):"-\"\\u221E\\t\"";
        String upper = upperBound!=null?df.format(new Date(upperBound.toEpochMilli())):"+\"\\u221E\\t\"";
        return "\"" + lower + " until "+upper+"\"";
    }

    private ValidationResult validate(IntervalReadingRecord mainReading, IntervalReadingRecord checkReading) {

        // [RULE CHECK] If no data is available on the check channel:
        if (checkReading == null) {
            // show log
            LoggingContext.get()
                    .warning(getLogger(), generateFailMessageWithUsagePoint("data from 'check' output is missing or not validated"));

            if (passIfNoRefData) {
                // [RULE ACTION] No further checks are done to the interval (marked as valid) and the rule moves to the next interval if Pass if no reference data is checked
                return ValidationResult.VALID;
            } else {
                // [RULE ACTION]  Stop the validation at the timestamp where the timestamp with the last reference data was found for the channel if Pass if no reference data is not checked
                preparedValidationResult = ValidationResult.NOT_VALIDATED;
                return ValidationResult.NOT_VALIDATED;
            }
        }

        // [RULE FLOW CHECK] Data is available on check output but not validated:
        ValidationResult checkReadingValidationResult = Optional.ofNullable(checkReadingRecordValidations.get(checkReading
                .getTimeStamp())).orElse(ValidationResult.NOT_VALIDATED);
        if (checkReadingValidationResult != ValidationResult.VALID) {
            // show log
            LoggingContext.get()
                    .warning(getLogger(), generateFailMessageWithUsagePoint("data from 'check' output is missing or not validated"));
            if (useValidatedData) {
                // [RULE ACTION] Stop the validation at the timestamp where the timestamp with the last validated reference data was found for the channel if Use validated data is checked
                preparedValidationResult = ValidationResult.NOT_VALIDATED;
                return ValidationResult.NOT_VALIDATED;
            }   // else:
                // [RULE ACTION] Continue validation if Use validated data is unchecked
                // So, next checks will be applied

        }

        BigDecimal mainValue = mainReading.getValue();
        BigDecimal checkValue = checkReading.getValue();

        if (!minThreshold.isNone) {

            // if both main and check values less than min threshold, mark this reading as NOT_VALIDATED*by threshold
            // at the end, if there is a validated reading (valid or suspected) followed by any NOT_VALIDATED*by threshold - mark all NOT_VALIDATED*by threshold as valid


            if (mainValue.compareTo(minThreshold.value) <= 0 && checkValue.compareTo(minThreshold.value) <= 0) {
                // [RULE FLOW ACTION] the check for the interval is skipped and the validation moves to the next interval.
                notValidatedByThreshold.add(mainReading.getTimeStamp());
                return ValidationResult.NOT_VALIDATED;
            }
        }

        BigDecimal differenceValue;

        if (maxAbsoluteDifference instanceof TwoValuesAbsoluteDifference) {
            differenceValue = ((TwoValuesAbsoluteDifference) maxAbsoluteDifference).value;
        } else if (maxAbsoluteDifference instanceof TwoValuesPercentDifference) {
            differenceValue = mainValue.multiply(BigDecimal.valueOf(((TwoValuesPercentDifference) maxAbsoluteDifference).percent*0.01D));
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
    public Map<Instant, ValidationResult> finish() {
        // check NOT_VALIDATED*by threshold readings.

        // mark all NOT_VALIDATED*by threshold readings as VALID if they happened before lastValidatedReading

       return notValidatedByThreshold.stream().filter((c -> c.compareTo(lastValidatedReading) < 0)).collect(Collectors.toMap(Function.identity(),c -> ValidationResult.VALID));

    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.MAIN_CHECK_VALIDATOR.getDefaultFormat();
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
}
