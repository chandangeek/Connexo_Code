/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link UniqueRuleSetUsage} constraint.
 */
public class UniqueRuleSetUsageValidator implements ConstraintValidator<UniqueRuleSetUsage, MetrologyContractEstimationRuleSetUsage> {

    private final UsagePointConfigurationService usagePointConfigurationService;

    @Inject
    public UniqueRuleSetUsageValidator(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Override
    public void initialize(UniqueRuleSetUsage constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(MetrologyContractEstimationRuleSetUsage value, ConstraintValidatorContext context) {
        boolean valid = true;   // Optimistic approach ;-)

        if (usagePointConfigurationService.getEstimationRuleSets(value.getMetrologyContract())
                .stream()
                .anyMatch(ers -> ers.equals(value.getEstimationRuleSet()))) {
            return false;
        }
        return valid;
    }

}