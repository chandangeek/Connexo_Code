/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

/**
 * Validates whether or not the OverFlow value is a required field
 */
public class ChannelOverflowValueValidator implements ConstraintValidator<ChannelOverflowValueValidation, ChannelSpecImpl> {
    @Override
    public void initialize(ChannelOverflowValueValidation channelOverflowValueValidation) {

    }

    @Override
    public boolean isValid(ChannelSpecImpl channelSpec, ConstraintValidatorContext constraintValidatorContext) {
        if(channelSpec.getReadingType().isCumulative() && !channelSpec.getOverflow().isPresent()){
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
                    .addPropertyNode("overflow")
                    .addConstraintViolation();
            return false;
        }
        if(channelSpec.getOverflow().isPresent() && channelSpec.getOverflow().get().compareTo(BigDecimal.ZERO) < 1){
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CHANNEL_SPEC_INVALID_OVERFLOW_VALUE + "}")
                    .addPropertyNode("overflow")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
