/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.metering.ReadingType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ChannelSpecMultiplierConfigurationValidator extends AbstractMultiplierConfigurationValidator implements ConstraintValidator<ValidChannelSpecMultiplierConfiguration, ChannelSpecImpl> {

    @Override
    public void initialize(ValidChannelSpecMultiplierConfiguration validChannelSpecMultiplierConfiguration) {

    }

    @Override
    public boolean isValid(ChannelSpecImpl channelSpec, ConstraintValidatorContext constraintValidatorContext) {
        if (channelSpec.isUseMultiplier()) {
            ReadingType channelSpecReadingType = channelSpec.getReadingType();
            if (readingTypeCanNotBeMultiplied(constraintValidatorContext, channelSpecReadingType)){
                return false;
            }
            if (calculatedReadingTypeIsNotPresent(constraintValidatorContext, channelSpec.getCalculatedReadingType())){
                return false;
            }
            if (invalidCalculatedReadingType(constraintValidatorContext, getReadingTypeToCompare(channelSpecReadingType), channelSpec.getCalculatedReadingType().get())){
                return false;
            }
        }
        return true;
    }

    private ReadingType getReadingTypeToCompare(ReadingType channelSpecReadingType) {
        return channelSpecReadingType.getCalculatedReadingType().orElse(channelSpecReadingType);
    }

    @Override
    String getReadingTypeFieldName() {
        return ChannelSpecImpl.ChannelSpecFields.CHANNEL_TYPE.fieldName() + "readingType";

    }
}
