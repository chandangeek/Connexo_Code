/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

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

        checkChannelPurpose = getCheckPurposeProperty(true);

        // 2. find 'check' channel
        try {
            initValidatingPurpose();
            initUsagePointName(channel);
            initCheckData(validatingUsagePoint, readingType);
        } catch (InitCancelException e) {
            preparedValidationResult = e.getValidationResult();
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
    void logFailure(InitCancelProps props) {
        // FIXME: verify messages
        InitCancelReason reason = props.reason;
        switch (reason) {
            case NO_REFERENCE_PURPOSE_FOUND_ON_REFERENCE_USAGE_POINT:
                LoggingContext.get()
                        .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.MAIN_CHECK_MISC_NO_PURPOSE)
                                .format(rangeToString(failedValidatonInterval), getDisplayName(), props.readingType.getFullAliasName(), validatingUsagePointName));
                break;
            case REFERENCE_PURPOSE_HAS_NOT_BEEN_EVER_ACTIVATED:
                LoggingContext.get()
                        .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.MAIN_CHECK_MISC_PURPOSE_NEVER_ACTIVATED)
                                .format(rangeToString(failedValidatonInterval), getDisplayName(), props.readingType.getFullAliasName(), validatingUsagePointName));

                break;
            case REFERENCE_OUTPUT_DOES_NOT_EXIST:
                LoggingContext.get()
                        .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.MAIN_CHECK_MISC_NO_CHECK_OUTPUT)
                                .format(rangeToString(failedValidatonInterval),
                                        getDisplayName(),
                                        props.readingType.getFullAliasName(),
                                        validatingUsagePointName));
                break;
            case REFERENCE_OUPUT_MISSING_OR_NOT_VALID:
                LoggingContext.get()
                        .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.MAIN_CHECK_MISC_CHECK_OUTPUT_MISSING_OR_NOT_VALID)
                                .format(rangeToString(failedValidatonInterval), getDisplayName(), validatingUsagePointName, props.readingType
                                        .getFullAliasName()));
                break;
        }
    }
}
