/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.cbo.*;
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
        if (lifeCycle.isPresent() && lifeCycle.get().getId() != usagePointLifeCycle.getId() || nameIsTheSameAsOneOfDefaultTranslations(usagePointLifeCycle)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("name")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean nameIsTheSameAsOneOfDefaultTranslations(UsagePointLifeCycle newUsagePointLifeCycle) {
        Optional<UsagePointLifeCycle> usagePointLifeCycle = this.service.getUsagePointLifeCycles().stream()
                .filter(lifeCycle -> lifeCycle.getName().equals(UsagePointLifeCycleConfigurationService.LIFE_CYCLE_KEY))
                .findFirst();
        return usagePointLifeCycle.isPresent() && !usagePointLifeCycle.get().isObsolete() &&
                usagePointLifeCycle.get().getId() != newUsagePointLifeCycle.getId()
                && this.service.getAllTranslationsForKey(TranslationKeys.LIFE_CYCLE_NAME.getKey())
                .values()
                .contains(newUsagePointLifeCycle.getName());
    }
}