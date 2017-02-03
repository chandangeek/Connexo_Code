/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueUsagePointLifeCycleNameValidator implements ConstraintValidator<Unique, UsagePointLifeCycle> {

    private final UsagePointLifeCycleConfigurationServiceImpl service;

    @Inject
    public UniqueUsagePointLifeCycleNameValidator(UsagePointLifeCycleConfigurationService service) {
        super();
        this.service = (UsagePointLifeCycleConfigurationServiceImpl) service;
    }

    @Override
    public void initialize(Unique constraintAnnotation) {
    }

    @Override
    public boolean isValid(UsagePointLifeCycle usagePointLifeCycle, ConstraintValidatorContext context) {
        Optional<UsagePointLifeCycle> lifeCycle = this.service.findUsagePointLifeCycleByName(usagePointLifeCycle.getName());
        if (lifeCycle.isPresent() && lifeCycle.get().getId() != usagePointLifeCycle.getId()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("name")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}