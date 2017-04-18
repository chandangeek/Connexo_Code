/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationResult;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DummyValidator extends AbstractValidator {

    private static final String requiredRuleProperty = "required.rule.property";
    private static final String optionalRuleProperty = "optional.rule.property";
    private static final String requiredChannelProperty = "required.channel.property";
    private static final String optionalChannelProperty = "optional.channel.property";
    private static final String requiredProperty = "required.property";
    private static final String optionalProperty = "optional.property";

    public DummyValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public List<String> getRequiredProperties() {
        return Arrays.asList(requiredRuleProperty, requiredProperty);
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {

    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        return ValidationResult.VALID;
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        return ValidationResult.VALID;
    }

    @Override
    public String getDefaultFormat() {
        return "Dummy validator";
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return EnumSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                createRequiredProperty(),
                createOptionalProperty(),
                createRequiredRuleProperty(),
                createOptionalRuleProperty(),
                createRequiredChannelProperty(),
                createOptionalChannelProperty()
        );
    }

    @Override
    public List<PropertySpec> getPropertySpecs(ValidationPropertyDefinitionLevel level) {
        switch (level) {
            case VALIDATION_RULE:
                return Arrays.asList(
                        createRequiredProperty(),
                        createOptionalProperty(),
                        createRequiredRuleProperty(),
                        createOptionalRuleProperty()
                );
            case TARGET_OBJECT:
                return Arrays.asList(
                        createRequiredProperty(),
                        createOptionalProperty(),
                        createRequiredChannelProperty(),
                        createOptionalChannelProperty()
                );
            default:
                return Collections.emptyList();
        }
    }

    private PropertySpec createRequiredProperty() {
        return getPropertySpecService()
                .longSpec()
                .named(requiredProperty, "Required property").describedAs("")
                .markRequired()
                .finish();
    }

    private PropertySpec createOptionalProperty() {
        return getPropertySpecService()
                .longSpec()
                .named(optionalProperty, "Optional property").describedAs("")
                .finish();
    }

    private PropertySpec createRequiredRuleProperty() {
        return getPropertySpecService()
                .longSpec()
                .named(requiredRuleProperty, "Required rule property").describedAs("")
                .markRequired()
                .finish();
    }

    private PropertySpec createOptionalRuleProperty() {
        return getPropertySpecService()
                .longSpec()
                .named(optionalRuleProperty, "Optional rule property").describedAs("")
                .finish();
    }

    private PropertySpec createRequiredChannelProperty() {
        return getPropertySpecService()
                .longSpec()
                .named(requiredChannelProperty, "Required channel property").describedAs("")
                .markRequired()
                .finish();
    }

    private PropertySpec createOptionalChannelProperty() {
        return getPropertySpecService()
                .longSpec()
                .named(optionalChannelProperty, "Optional channel property").describedAs("")
                .finish();
    }

    @Override
    public void validateProperties(Map<String, Object> properties) {
        if (properties.containsKey(DummyValidator.optionalChannelProperty)) {
            long optionalChannelProperty = (long) properties.get(DummyValidator.optionalChannelProperty);
            if (optionalChannelProperty < 10) {
                throw new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_VALIDATOR, "properties." + DummyValidator.optionalChannelProperty);
            }
        }
        if (properties.containsKey(DummyValidator.optionalRuleProperty)) {
            long optionalChannelProperty = (long) properties.get(DummyValidator.optionalRuleProperty);
            if (optionalChannelProperty < 10) {
                throw new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_VALIDATOR, "properties." + DummyValidator.optionalRuleProperty);
            }
        }
    }

    @Override
    public List<TranslationKey> getExtraTranslationKeys() {
        return Collections.emptyList();
    }
}
