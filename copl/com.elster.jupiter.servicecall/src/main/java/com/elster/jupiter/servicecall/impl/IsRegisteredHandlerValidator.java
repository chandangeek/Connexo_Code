package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.servicecall.ServiceCallService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by bvn on 2/25/16.
 */
public class IsRegisteredHandlerValidator implements ConstraintValidator<IsRegisteredHandler, String> {

    private final ServiceCallService serviceCallService;

    @Inject
    public IsRegisteredHandlerValidator(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public void initialize(IsRegisteredHandler isRegisteredHandler) {

    }

    @Override
    public boolean isValid(String serviceCallHandlerName, ConstraintValidatorContext constraintValidatorContext) {
        return serviceCallService.findHandler(serviceCallHandlerName).isPresent();
    }
}
