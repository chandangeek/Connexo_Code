package com.elster.jupiter.metering.impl;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;

public class UniqueUsagePointNameValidator implements ConstraintValidator<UniqueName, UsagePoint> {

    private String message;
    private MeteringService meteringService;

    @Inject
    public UniqueUsagePointNameValidator(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(UsagePoint usagePoint, ConstraintValidatorContext context) {
        return usagePoint == null || !checkExisting(usagePoint, context);
    }

    private boolean checkExisting(UsagePoint usagePoint, ConstraintValidatorContext context) {
    	
    	Condition condition = Operator.EQUAL.compare("name", usagePoint.getName());
        List<UsagePoint> candidates = meteringService.getUsagePointQuery().select(condition);
        Optional<UsagePoint> found = Optional.empty();
        if (candidates.size()==1) {
        	found = Optional.of(candidates.get(0));
        } else if (candidates.size()>1) {
        	throw new IllegalStateException();
        }
        
        if (found.isPresent() && areDifferentWithSameName(usagePoint, found.get())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean areDifferentWithSameName(UsagePoint usagePoint, UsagePoint existingUsagePoint) {
        return existingUsagePoint.getName().equals(usagePoint.getName()) && (existingUsagePoint.getId() != usagePoint.getId());
    }

}