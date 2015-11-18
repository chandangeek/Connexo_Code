package com.energyict.mdc.device.config.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Copyrights EnergyICT
 * Date: 18.11.15
 * Time: 14:51
 */
public class ChannelSpecMultiplierConfigurationValidator implements ConstraintValidator<ValidChannelSpecMultiplierConfiguration, ChannelSpecImpl> {
    @Override
    public void initialize(ValidChannelSpecMultiplierConfiguration validChannelSpecMultiplierConfiguration) {

    }

    @Override
    public boolean isValid(ChannelSpecImpl channelSpec, ConstraintValidatorContext constraintValidatorContext) {
        if (channelSpec.isUseMultiplier() && !channelSpec.getCalculatedReadingType().isPresent()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CALCULATED_READINGTYPE_CANNOT_BE_EMPTY + "}").addConstraintViolation();
            return false;
        }
        return true;
    }
}
