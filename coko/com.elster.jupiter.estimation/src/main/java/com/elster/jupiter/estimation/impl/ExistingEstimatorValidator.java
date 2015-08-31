package com.elster.jupiter.estimation.impl;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimatorNotFoundException;

public class ExistingEstimatorValidator implements ConstraintValidator<ExistingEstimator, String> {

    private EstimationService estimationService;

    @Inject
    public ExistingEstimatorValidator(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Override
    public void initialize(ExistingEstimator constraintAnnotation) {
        // nothing atm
    }

    @Override
    public boolean isValid(String implementation, ConstraintValidatorContext context) {
        try {
            estimationService.getEstimator(implementation);
            return true;
        } catch (EstimatorNotFoundException e) {
            context.disableDefaultConstraintViolation();

            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.NO_SUCH_ESTIMATOR.getKey() + "}").addConstraintViolation();
            return false;
        }
    }
}