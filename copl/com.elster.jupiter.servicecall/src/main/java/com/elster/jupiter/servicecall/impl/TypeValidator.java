package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.servicecall.ServiceCallType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by bvn on 2/25/16.
 */
public class TypeValidator implements ConstraintValidator<IsValidType, RegisteredCustomPropertySet> {
    @Override
    public void initialize(IsValidType isValidType) {

    }

    @Override
    public boolean isValid(RegisteredCustomPropertySet customPropertySet, ConstraintValidatorContext constraintValidatorContext) {
        return customPropertySet.getCustomPropertySet().getDomainClass().isAssignableFrom(ServiceCallType.class);
    }
}
