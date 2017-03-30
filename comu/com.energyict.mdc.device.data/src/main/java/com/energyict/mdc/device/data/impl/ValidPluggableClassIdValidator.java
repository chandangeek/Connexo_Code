/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidPluggableClassIdValidator implements ConstraintValidator<ValidPluggableClassId, Long> {

    @Override
    public void initialize(ValidPluggableClassId validPluggableClassId) {
        // nothing to do
    }

    @Override
    public boolean isValid(Long pluggableClassId, ConstraintValidatorContext constraintValidatorContext) {
        return pluggableClassId > 0;
    }


}