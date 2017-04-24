/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.TwoValuesDifference;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * The 'Main/Check comparison' validator allows to compare output values from one channel to another "check" channel on the same usage point.<br>
 * The validator compares delta values on the validated output channel on a usage point to another output channel with the same reading type
 * but on a different purpose. If compared values with identical time and date are not equal or the difference exceeds the configured parameter a suspect is created.
 * If the minimum threshold is configured the check for the interval is skipped and the validation moves to the next interval.
 */
public class MainCheckValidator extends MainCheckAbstractValidator {

    // validator parameters


    public MainCheckValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService) {
        super(thesaurus, propertySpecService, metrologyConfigurationService, validationService);
    }

    public MainCheckValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService) {
        super(thesaurus, propertySpecService, metrologyConfigurationService, validationService, properties);
    }

    @Override
    public List<String> getRequiredProperties() {
        return Arrays.asList(CHECK_PURPOSE, MAX_ABSOLUTE_DIFF, MIN_THRESHOLD);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                buildCheckPurposePropertySpec(),
                buildMaxAbsoluteDiffPropertySpec(),
                buildPassIfNoRefDataPropertySpec(),
                buildUseValidatedDataPropertySpec(),
                buildMinThresholdPropertySpec()
        );
    }

    @Override
    public List<PropertySpec> getPropertySpecs(ValidationPropertyDefinitionLevel level) {
        return ValidationPropertyDefinitionLevel.VALIDATION_RULE == level ? getPropertySpecs() : getOverridenPropertySpecs();
    }

    private List<PropertySpec> getOverridenPropertySpecs() {
        return Arrays.asList(
                buildMaxAbsoluteDiffPropertySpec(),
                buildMinThresholdPropertySpec()
        );
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        super.init(channel, readingType, interval);

        // find 'check' channel and save readings + prepare mapping with readings from 'main' channel

        // 2. find 'check' channel
        try {
            initValidatingPurpose();
            initUsagePointName(channel);
            initCheckData(validatingUsagePoint, readingType);
        } catch (InitCancelException e) {

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


    private ValidationResult validate(IntervalReadingRecord mainReading, IntervalReadingRecord checkReading) {

        Instant timeStamp = mainReading.getTimeStamp();

        // [RULE CHECK] If no data is available on the check channel:
        if (checkReading == null) {
            // show log
            LoggingContext.get()
                    .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.MAIN_CHECK_MISC_CHECK_OUTPUT_MISSING_OR_NOT_VALID)
                            .format(rangeToString(failedValidatonInterval), getDisplayName(), validatingUsagePointName, readingType
                                    .getFullAliasName()));

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
                                .format(rangeToString(failedValidatonInterval), getDisplayName(), validatingUsagePointName, readingType
                                        .getFullAliasName()));
                // [RULE ACTION] Stop the validation at the timestamp where the timestamp with the last validated reference data was found for the channel if Use validated data is checked
                prepareValidationResult(ValidationResult.NOT_VALIDATED, timeStamp);
                return ValidationResult.NOT_VALIDATED;
            }   // else:
            // [RULE ACTION] Continue validation if Use validated data is unchecked
            // So, next checks will be applied

        }

        BigDecimal mainValue = mainReading.getValue();
        BigDecimal checkValue = checkReading.getValue();

        if (!minThreshold.isNone()) {
            if (mainValue.compareTo(minThreshold.getValue()) <= 0 && checkValue.compareTo(minThreshold.getValue()) <= 0) {
                // [RULE FLOW ACTION] the check for the interval is marked valid and the validation moves to the next interval.
                return ValidationResult.VALID;
            }
        }

        BigDecimal differenceValue;


        if (TwoValuesDifference.Type.ABSOLUTE == maxAbsoluteDifference.getType()) {
            differenceValue = maxAbsoluteDifference.getValue();
        } else if (TwoValuesDifference.Type.RELATIVE == maxAbsoluteDifference.getType()) {
            differenceValue = mainValue.multiply(maxAbsoluteDifference.getValue()).multiply(new BigDecimal(0.01));
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
    public String getDefaultFormat() {
        return "Main/check comparison";
    }

    @Override
    String getClassName() {
        return MainCheckValidator.class.getName();
    }

    @Override
    void logInitCancelFailure(InitCancelProps props) {
        // FIXME: verify messages
        InitCancelReason reason = props.reason;
        switch (reason) {
            case NO_REFERENCE_PURPOSE_FOUND_ON_REFERENCE_USAGE_POINT:
                LoggingContext.get()
                        .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.MAIN_CHECK_MISC_NO_PURPOSE)
                                .format(rangeToString(failedValidatonInterval), getDisplayName(), props.readingType, validatingUsagePointName));
                break;
            case REFERENCE_PURPOSE_HAS_NOT_BEEN_EVER_ACTIVATED:
                LoggingContext.get()
                        .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.MAIN_CHECK_MISC_PURPOSE_NEVER_ACTIVATED)
                                .format(rangeToString(failedValidatonInterval), getDisplayName(), props.readingType, validatingUsagePointName));

                break;
            case REFERENCE_OUTPUT_DOES_NOT_EXIST:
                LoggingContext.get()
                        .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.MAIN_CHECK_MISC_NO_CHECK_OUTPUT)
                                .format(rangeToString(failedValidatonInterval),
                                        getDisplayName(),
                                        props.readingType,
                                        validatingUsagePointName));
                break;
        }
    }
}
