/*
 *  Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;


import com.energyict.mdc.common.impl.MessageSeeds;
import com.energyict.obis.ObisCode;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;



public class ValidStringObisCodeValidator implements ConstraintValidator<ValidObisCode, ObisCode> {

    private String message;

    @Inject
    public ValidStringObisCodeValidator() {
    }

    @Override
    public void initialize(ValidObisCode validObisCode) {
        this.message = validObisCode.message();
    }

    @Override
    public boolean isValid(ObisCode obisCode, ConstraintValidatorContext context) {
        if(obisCode != null && obisCode.isInvalid()){
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate("{" + MessageSeeds.INVALID_OBIS_CODE.getKey() + "}")
                .addPropertyNode("obisCode")
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
