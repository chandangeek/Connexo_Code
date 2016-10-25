package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueUsagePointLifeCycleNameValidator implements ConstraintValidator<Unique, UsagePointLifeCycle> {

    private final UsagePointLifeCycleServiceImpl service;

    @Inject
    public UniqueUsagePointLifeCycleNameValidator(UsagePointLifeCycleService service) {
        super();
        this.service = (UsagePointLifeCycleServiceImpl) service;
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