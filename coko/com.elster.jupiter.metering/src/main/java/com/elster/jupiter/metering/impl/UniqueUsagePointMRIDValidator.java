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

public class UniqueUsagePointMRIDValidator implements ConstraintValidator<UniqueMRID, UsagePoint> {

    private String message;
    private MeteringService meteringService;

    @Inject
    public UniqueUsagePointMRIDValidator(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public void initialize(UniqueMRID constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(UsagePoint usagePoint, ConstraintValidatorContext context) {
        return usagePoint == null || !checkExisting(usagePoint, context);
    }

    private boolean checkExisting(UsagePoint usagePoint, ConstraintValidatorContext context) {
    	Condition condition = Operator.EQUAL.compare("mRID", usagePoint.getMRID());
        List<UsagePoint> candidates = meteringService.getUsagePointQuery().select(condition);
        Optional<UsagePoint> found = Optional.empty();
        if (candidates.size()==1) {
        	found = Optional.of(candidates.get(0));
        } else if (candidates.size()>1) {
        	throw new IllegalStateException();
        }
        
        if (found.isPresent() && areDifferentWithSameMRID(usagePoint, found.get())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("mRID").addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean areDifferentWithSameMRID(UsagePoint usagePoint, UsagePoint existingUsagePoint) {
        return existingUsagePoint.getMRID().equals(usagePoint.getMRID()) && (existingUsagePoint.getId() != usagePoint.getId());
    }

}