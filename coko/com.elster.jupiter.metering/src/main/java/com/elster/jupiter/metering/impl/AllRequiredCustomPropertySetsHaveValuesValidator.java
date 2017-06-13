/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;

import com.google.common.collect.Range;
import com.google.inject.Inject;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AllRequiredCustomPropertySetsHaveValuesValidator implements ConstraintValidator<AllRequiredCustomPropertySetsHaveValues, UsagePointImpl> {

    @Inject
    private CustomPropertySetService customPropertySetService;

    @Override
    public void initialize(AllRequiredCustomPropertySetsHaveValues constraintAnnotation) {
        // nothing to do
    }

    @Override
    public boolean isValid(UsagePointImpl usagePoint, ConstraintValidatorContext context) {
        if (Stream.concat(
                usagePoint.getServiceCategory().getCustomPropertySets().stream()
                        .filter(c -> c.getCustomPropertySet().isRequired())
                        .map(c -> new CustomPropertySetValuesProviderImpl(c, usagePoint, Range.greaterThan(usagePoint.getInstallationTime()))),
                usagePoint.getAllEffectiveMetrologyConfigurations().stream()
                        .map(mc -> new MetrologyConfigurationCustomPropertySetValuesValidator(mc, usagePoint)))
                .anyMatch(validator -> !validator.isValuesValid())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + PrivateMessageSeeds.Constants.REQUIRED_CPS_MISSING + "}")
                    .addPropertyNode("customPropertySets")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    /**
     * The purpose of this validator is to verify CustomPropertySetValues are valid on range.
     * Values are valid on range if there is an effective value on each time in range
     */
    interface CustomPropertySetValuesRangeValidator {
        boolean isValuesValid();
    }

    /**
     * Implementation of {@link CustomPropertySetValuesRangeValidator} to check if {@link CustomPropertySet} valid on range
     */
    class CustomPropertySetValuesProviderImpl implements CustomPropertySetValuesRangeValidator {

        private CustomPropertySet customPropertySet;
        private UsagePoint usagePoint;
        private Range<Instant> effectivelyRange;

        /**
         * @param customPropertySet {@link CustomPropertySet} to be validated
         * @param usagePoint {@link UsagePoint} as businessObject for persisted custom property set
         * @param effectivelyRange {@link Range} to validate values of {@link CustomPropertySet} on
         */
        CustomPropertySetValuesProviderImpl(RegisteredCustomPropertySet customPropertySet, UsagePoint usagePoint, Range<Instant> effectivelyRange) {
            this.customPropertySet = customPropertySet.getCustomPropertySet();
            this.usagePoint = usagePoint;
            this.effectivelyRange = effectivelyRange;
        }

        @Override
        public boolean isValuesValid() {
            return customPropertySetService.validateCustomPropertySetHasValues(customPropertySet, usagePoint, effectivelyRange);
        }
    }

    /**
     * Implementation of {@link CustomPropertySetValuesRangeValidator} to check if metrology configuration has valid custom properties set values.
     * Values are considered valid only if all values for all custom property sets related to the metrology configuration are valid on metrology configurations range
     */
    class MetrologyConfigurationCustomPropertySetValuesValidator implements CustomPropertySetValuesRangeValidator {

        private List<CustomPropertySetValuesProviderImpl> propertySetValuesProviders;

        /**
         * @param mc {@link EffectiveMetrologyConfigurationOnUsagePoint} containing custom property sets to be validated
         * @param usagePoint business object for persisted values
         */
        MetrologyConfigurationCustomPropertySetValuesValidator(EffectiveMetrologyConfigurationOnUsagePoint mc, UsagePoint usagePoint) {
            propertySetValuesProviders = mc.getMetrologyConfiguration()
                    .getCustomPropertySets()
                    .stream()
                    .filter(c -> c.getCustomPropertySet().isRequired())
                    .map(c -> new CustomPropertySetValuesProviderImpl(c, usagePoint, mc.getRange()))
                    .collect(Collectors.toList());
        }

        @Override
        public boolean isValuesValid() {
            return propertySetValuesProviders.stream().allMatch(CustomPropertySetValuesProviderImpl::isValuesValid);
        }
    }
}
